const express = require('express');
const { executeQuery } = require('../config/database');
const { authenticateToken } = require('../middleware/authMiddleware');

const router = express.Router();

function formatParking(row) {
  return {
    id: row.ID,
    name: row.NAME,
    description: row.DESCRIPTION || '',
    address: row.ADDRESS || '',
    imageUrl: row.IMAGE_URL || '',
    latitude: row.LATITUDE || 0,
    longitude: row.LONGITUDE || 0,
    pricePerHour: row.PRICE_PER_HOUR || 0,
    availableSpots: row.AVAILABLE_SPOTS || 0,
    totalSpots: row.TOTAL_SPOTS || 0,
    rating: row.RATING || 0,
    reviewCount: row.REVIEW_COUNT || 0,
    isSaved: row.IS_SAVED === 1 || row.IS_SAVED === true,
    isRecentlyViewed: row.IS_RECENTLY_VIEWED === 1 || row.IS_RECENTLY_VIEWED === true
  };
}

// GET /api/parkings
router.get('/', async (req, res) => {
  try {
    const userId = req.headers['x-user-id'] ? parseInt(req.headers['x-user-id']) : null;

    let result;
    if (userId) {
      result = await executeQuery(
        `SELECT p.*,
           CASE WHEN sp.PARKING_ID IS NOT NULL THEN 1 ELSE 0 END AS IS_SAVED,
           CASE WHEN rv.PARKING_ID IS NOT NULL THEN 1 ELSE 0 END AS IS_RECENTLY_VIEWED
         FROM PARKINGS p
         LEFT JOIN SAVED_PARKINGS sp ON p.ID = sp.PARKING_ID AND sp.USER_ID = :userId
         LEFT JOIN RECENTLY_VIEWED rv ON p.ID = rv.PARKING_ID AND rv.USER_ID = :userId2
         ORDER BY p.RATING DESC`,
        { userId, userId2: userId }
      );
    } else {
      result = await executeQuery(
        `SELECT p.*, 0 AS IS_SAVED, 0 AS IS_RECENTLY_VIEWED
         FROM PARKINGS p ORDER BY p.RATING DESC`
      );
    }

    res.json(result.rows.map(formatParking));
  } catch (error) {
    console.error('Error al obtener estacionamientos:', error.message);
    res.status(500).json({ error: 'Error al obtener estacionamientos' });
  }
});

// GET /api/parkings/saved (requiere auth) - DEBE IR ANTES DE /:id
router.get('/saved', authenticateToken, async (req, res) => {
  try {
    const result = await executeQuery(
      `SELECT p.*, 1 AS IS_SAVED, 0 AS IS_RECENTLY_VIEWED
       FROM PARKINGS p
       INNER JOIN SAVED_PARKINGS sp ON p.ID = sp.PARKING_ID AND sp.USER_ID = :userId
       ORDER BY sp.SAVED_AT DESC`,
      { userId: req.user.id }
    );
    res.json(result.rows.map(formatParking));
  } catch (error) {
    console.error('Error al obtener guardados:', error.message);
    res.status(500).json({ error: 'Error al obtener estacionamientos guardados' });
  }
});

// GET /api/parkings/recent (requiere auth) - DEBE IR ANTES DE /:id
router.get('/recent', authenticateToken, async (req, res) => {
  try {
    const result = await executeQuery(
      `SELECT p.*,
         CASE WHEN sp.PARKING_ID IS NOT NULL THEN 1 ELSE 0 END AS IS_SAVED,
         1 AS IS_RECENTLY_VIEWED
       FROM PARKINGS p
       INNER JOIN RECENTLY_VIEWED rv ON p.ID = rv.PARKING_ID AND rv.USER_ID = :userId
       LEFT JOIN SAVED_PARKINGS sp ON p.ID = sp.PARKING_ID AND sp.USER_ID = :userId2
       ORDER BY rv.VIEWED_AT DESC
       FETCH FIRST 10 ROWS ONLY`,
      { userId: req.user.id, userId2: req.user.id }
    );
    res.json(result.rows.map(formatParking));
  } catch (error) {
    console.error('Error al obtener recientes:', error.message);
    res.status(500).json({ error: 'Error al obtener estacionamientos recientes' });
  }
});

// GET /api/parkings/search
router.get('/search', async (req, res) => {
  try {
    const { q } = req.query;

    if (!q || q.trim().length < 2) {
      return res.status(400).json({ error: 'La busqueda debe tener al menos 2 caracteres' });
    }

    const term = `%${q.trim().toUpperCase()}%`;
    const result = await executeQuery(
      `SELECT p.*, 0 AS IS_SAVED, 0 AS IS_RECENTLY_VIEWED
       FROM PARKINGS p
       WHERE UPPER(p.NAME) LIKE :term OR UPPER(p.ADDRESS) LIKE :term2
       ORDER BY p.RATING DESC`,
      { term, term2: term }
    );

    res.json(result.rows.map(formatParking));
  } catch (error) {
    console.error('Error en busqueda:', error.message);
    res.status(500).json({ error: 'Error al buscar estacionamientos' });
  }
});

// GET /api/parkings/:id
router.get('/:id', async (req, res) => {
  try {
    const parkingId = parseInt(req.params.id);
    if (isNaN(parkingId)) {
      return res.status(400).json({ error: 'ID invalido' });
    }

    const userId = req.headers['x-user-id'] ? parseInt(req.headers['x-user-id']) : null;
    let result;

    if (userId) {
      result = await executeQuery(
        `SELECT p.*,
           CASE WHEN sp.PARKING_ID IS NOT NULL THEN 1 ELSE 0 END AS IS_SAVED,
           CASE WHEN rv.PARKING_ID IS NOT NULL THEN 1 ELSE 0 END AS IS_RECENTLY_VIEWED
         FROM PARKINGS p
         LEFT JOIN SAVED_PARKINGS sp ON p.ID = sp.PARKING_ID AND sp.USER_ID = :userId
         LEFT JOIN RECENTLY_VIEWED rv ON p.ID = rv.PARKING_ID AND rv.USER_ID = :userId2
         WHERE p.ID = :id`,
        { id: parkingId, userId, userId2: userId }
      );
    } else {
      result = await executeQuery(
        `SELECT p.*, 0 AS IS_SAVED, 0 AS IS_RECENTLY_VIEWED FROM PARKINGS p WHERE p.ID = :id`,
        { id: parkingId }
      );
    }

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Estacionamiento no encontrado' });
    }

    res.json(formatParking(result.rows[0]));
  } catch (error) {
    console.error('Error al obtener estacionamiento:', error.message);
    res.status(500).json({ error: 'Error al obtener estacionamiento' });
  }
});

// POST /api/parkings/:id/save - Guardar/quitar de favoritos
router.post('/:id/save', authenticateToken, async (req, res) => {
  try {
    const parkingId = parseInt(req.params.id);
    const userId = req.user.id;

    const existing = await executeQuery(
      'SELECT 1 FROM SAVED_PARKINGS WHERE USER_ID = :userId AND PARKING_ID = :parkingId',
      { userId, parkingId }
    );

    if (existing.rows.length > 0) {
      await executeQuery(
        'DELETE FROM SAVED_PARKINGS WHERE USER_ID = :userId AND PARKING_ID = :parkingId',
        { userId, parkingId },
        { autoCommit: true }
      );
      res.json({ isSaved: false });
    } else {
      await executeQuery(
        'INSERT INTO SAVED_PARKINGS (USER_ID, PARKING_ID) VALUES (:userId, :parkingId)',
        { userId, parkingId },
        { autoCommit: true }
      );
      res.json({ isSaved: true });
    }
  } catch (error) {
    console.error('Error al guardar estacionamiento:', error.message);
    res.status(500).json({ error: 'Error al actualizar favoritos' });
  }
});

// POST /api/parkings/:id/view - Marcar como visto recientemente
router.post('/:id/view', authenticateToken, async (req, res) => {
  try {
    const parkingId = parseInt(req.params.id);
    const userId = req.user.id;

    const existing = await executeQuery(
      'SELECT 1 FROM RECENTLY_VIEWED WHERE USER_ID = :userId AND PARKING_ID = :parkingId',
      { userId, parkingId }
    );

    if (existing.rows.length > 0) {
      await executeQuery(
        'UPDATE RECENTLY_VIEWED SET VIEWED_AT = SYSTIMESTAMP WHERE USER_ID = :userId AND PARKING_ID = :parkingId',
        { userId, parkingId },
        { autoCommit: true }
      );
    } else {
      await executeQuery(
        'INSERT INTO RECENTLY_VIEWED (USER_ID, PARKING_ID) VALUES (:userId, :parkingId)',
        { userId, parkingId },
        { autoCommit: true }
      );
    }

    res.json({ success: true });
  } catch (error) {
    console.error('Error al registrar visualizacion:', error.message);
    res.status(500).json({ error: 'Error al registrar visualizacion' });
  }
});

module.exports = router;
