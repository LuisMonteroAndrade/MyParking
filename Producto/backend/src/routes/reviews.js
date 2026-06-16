const express = require('express');
const { executeQuery, oracledb } = require('../config/database');
const { authenticateToken: authMiddleware } = require('../middleware/authMiddleware');

const router = express.Router();

// GET /api/reviews/parking/:id — reseñas de un estacionamiento (público)
router.get('/parking/:id', async (req, res) => {
  try {
    const parkingId = parseInt(req.params.id);
    const result = await executeQuery(
      `SELECT r.ID, r.RATING, r.REVIEW_COMMENT, r.OWNER_RESPONSE, r.OWNER_RESPONSE_AT,
              r.CREATED_AT, r.USER_ID,
              u.NAME as USER_NAME, u.PHOTO_URL as USER_PHOTO
       FROM REVIEWS r
       JOIN APP_USERS u ON u.ID = r.USER_ID
       WHERE r.PARKING_ID = :parkingId
       ORDER BY r.CREATED_AT DESC`,
      { parkingId }
    );
    res.json(result.rows.map(r => ({
      id: r.ID,
      rating: r.RATING,
      comment: r.REVIEW_COMMENT || null,
      ownerResponse: r.OWNER_RESPONSE || null,
      ownerResponseAt: r.OWNER_RESPONSE_AT || null,
      createdAt: r.CREATED_AT,
      userId: r.USER_ID,
      userName: r.USER_NAME,
      userPhoto: r.USER_PHOTO || null
    })));
  } catch (error) {
    console.error('Error obteniendo reseñas:', error.message);
    res.status(500).json({ error: 'Error al obtener reseñas' });
  }
});

// POST /api/reviews — crear o actualizar reseña (solo conductores autenticados)
router.post('/', authMiddleware, async (req, res) => {
  try {
    const { parkingId, rating, comment } = req.body;
    const userId = req.user.id;

    if (!parkingId || !rating) {
      return res.status(400).json({ error: 'parkingId y rating son requeridos' });
    }
    if (rating < 1 || rating > 5) {
      return res.status(400).json({ error: 'El rating debe estar entre 1 y 5' });
    }

    const parkingCheck = await executeQuery(
      'SELECT ID FROM PARKINGS WHERE ID = :id',
      { id: parkingId }
    );
    if (parkingCheck.rows.length === 0) {
      return res.status(404).json({ error: 'Estacionamiento no encontrado' });
    }

    const existing = await executeQuery(
      'SELECT ID FROM REVIEWS WHERE PARKING_ID = :parkingId AND USER_ID = :userId',
      { parkingId, userId }
    );

    if (existing.rows.length > 0) {
      await executeQuery(
        `UPDATE REVIEWS SET RATING = :rating, REVIEW_COMMENT = :reviewComment, CREATED_AT = CURRENT_TIMESTAMP
         WHERE PARKING_ID = :parkingId AND USER_ID = :userId`,
        { rating, reviewComment: comment || null, parkingId, userId },
        { autoCommit: true }
      );
    } else {
      await executeQuery(
        `INSERT INTO REVIEWS (PARKING_ID, USER_ID, RATING, REVIEW_COMMENT)
         VALUES (:parkingId, :userId, :rating, :reviewComment)`,
        { parkingId, userId, rating, reviewComment: comment || null },
        { autoCommit: true }
      );
    }

    // Actualizar rating y contador en PARKINGS
    await executeQuery(
      `UPDATE PARKINGS SET
         RATING       = (SELECT ROUND(AVG(RATING), 1) FROM REVIEWS WHERE PARKING_ID = :parkingId),
         REVIEW_COUNT = (SELECT COUNT(*) FROM REVIEWS WHERE PARKING_ID = :parkingId)
       WHERE ID = :parkingId`,
      { parkingId },
      { autoCommit: true }
    );

    const reviewResult = await executeQuery(
      `SELECT r.ID, r.RATING, r.REVIEW_COMMENT, r.CREATED_AT, u.NAME as USER_NAME
       FROM REVIEWS r
       JOIN APP_USERS u ON u.ID = r.USER_ID
       WHERE r.PARKING_ID = :parkingId AND r.USER_ID = :userId`,
      { parkingId, userId }
    );
    const r = reviewResult.rows[0];
    res.status(201).json({
      id: r.ID,
      rating: r.RATING,
      comment: r.REVIEW_COMMENT || null,
      createdAt: r.CREATED_AT,
      userName: r.USER_NAME
    });
  } catch (error) {
    console.error('Error creando reseña:', error.message);
    res.status(500).json({ error: 'Error al crear reseña' });
  }
});

// PUT /api/reviews/:id/response — propietario responde a una reseña
router.put('/:id/response', authMiddleware, async (req, res) => {
  try {
    const reviewId = parseInt(req.params.id);
    const { response } = req.body;
    const ownerId = req.user.id;

    if (!response || response.trim() === '') {
      return res.status(400).json({ error: 'La respuesta no puede estar vacía' });
    }

    const reviewCheck = await executeQuery(
      `SELECT r.ID FROM REVIEWS r
       JOIN PARKINGS p ON p.ID = r.PARKING_ID
       WHERE r.ID = :reviewId AND p.OWNER_ID = :ownerId`,
      { reviewId, ownerId }
    );
    if (reviewCheck.rows.length === 0) {
      return res.status(403).json({ error: 'No tienes permiso para responder esta reseña' });
    }

    await executeQuery(
      `UPDATE REVIEWS SET OWNER_RESPONSE = :response, OWNER_RESPONSE_AT = CURRENT_TIMESTAMP
       WHERE ID = :reviewId`,
      { response: response.trim(), reviewId },
      { autoCommit: true }
    );

    res.json({ message: 'Respuesta publicada correctamente' });
  } catch (error) {
    console.error('Error respondiendo reseña:', error.message);
    res.status(500).json({ error: 'Error al responder reseña' });
  }
});

// GET /api/reviews/my-parkings — todas las reseñas de los estacionamientos del propietario
router.get('/my-parkings', authMiddleware, async (req, res) => {
  try {
    const ownerId = req.user.id;
    const result = await executeQuery(
      `SELECT r.ID, r.RATING, r.REVIEW_COMMENT, r.OWNER_RESPONSE, r.OWNER_RESPONSE_AT,
              r.CREATED_AT, r.USER_ID, r.PARKING_ID,
              u.NAME as USER_NAME, u.PHOTO_URL as USER_PHOTO,
              p.NAME as PARKING_NAME
       FROM REVIEWS r
       JOIN APP_USERS u ON u.ID = r.USER_ID
       JOIN PARKINGS p  ON p.ID = r.PARKING_ID
       WHERE p.OWNER_ID = :ownerId
       ORDER BY r.CREATED_AT DESC`,
      { ownerId }
    );
    res.json(result.rows.map(r => ({
      id: r.ID,
      rating: r.RATING,
      comment: r.REVIEW_COMMENT || null,
      ownerResponse: r.OWNER_RESPONSE || null,
      ownerResponseAt: r.OWNER_RESPONSE_AT || null,
      createdAt: r.CREATED_AT,
      userId: r.USER_ID,
      parkingId: r.PARKING_ID,
      userName: r.USER_NAME,
      userPhoto: r.USER_PHOTO || null,
      parkingName: r.PARKING_NAME
    })));
  } catch (error) {
    console.error('Error obteniendo reseñas del propietario:', error.message);
    res.status(500).json({ error: 'Error al obtener reseñas' });
  }
});

module.exports = router;
