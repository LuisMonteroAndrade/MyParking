const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { executeQuery, oracledb } = require('../config/database');
const { authenticateToken } = require('../middleware/authMiddleware');

const router = express.Router();

// Configuración de multer para imágenes de estacionamiento
const uploadDir = path.join(__dirname, '../../uploads');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    cb(null, `parking_${Date.now()}_${Math.random().toString(36).substr(2, 6)}${ext}`);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 10 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    const allowed = ['.jpg', '.jpeg', '.png'];
    const ext = path.extname(file.originalname).toLowerCase();
    if (allowed.includes(ext)) cb(null, true);
    else cb(new Error('Solo se permiten imágenes JPG, JPEG y PNG'));
  }
});

const uploadMiddleware = (req, res, next) => {
  upload.single('image')(req, res, (err) => {
    if (err) return res.status(400).json({ error: err.message });
    next();
  });
};

function requireOwner(req, res, next) {
  if (req.user.userType !== 'OWNER') {
    return res.status(403).json({ error: 'Acceso restringido a propietarios' });
  }
  next();
}

router.use(authenticateToken, requireOwner);

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
    isActive: row.IS_ACTIVE === 1 || row.IS_ACTIVE === undefined || row.IS_ACTIVE === null,
    ownerId: row.OWNER_ID,
    createdAt: row.CREATED_AT ? new Date(row.CREATED_AT).toISOString() : null
  };
}

// GET /api/owner/parkings
router.get('/parkings', async (req, res) => {
  try {
    const result = await executeQuery(
      `SELECT * FROM PARKINGS WHERE OWNER_ID = :ownerId ORDER BY CREATED_AT DESC`,
      { ownerId: req.user.id }
    );
    res.json(result.rows.map(formatParking));
  } catch (error) {
    console.error('Error al obtener mis estacionamientos:', error.message);
    res.status(500).json({ error: 'Error al obtener estacionamientos' });
  }
});

// POST /api/owner/parkings
router.post('/parkings', uploadMiddleware, async (req, res) => {
  try {
    const { name, description, address, pricePerHour, availableSpots, totalSpots, latitude, longitude } = req.body;

    if (!name || !address || pricePerHour === undefined || pricePerHour === null) {
      return res.status(400).json({ error: 'Nombre, direccion y precio son requeridos' });
    }
    if (!req.file) {
      return res.status(400).json({ error: 'La imagen del estacionamiento es obligatoria' });
    }

    const imageUrl = `${process.env.SERVER_URL}/uploads/${req.file.filename}`;

    const result = await executeQuery(
      `INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE,
        PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, OWNER_ID, IS_ACTIVE)
       VALUES (:name, :description, :address, :imageUrl, :latitude, :longitude,
        :pricePerHour, :availableSpots, :totalSpots, :ownerId, 1)
       RETURNING ID INTO :id`,
      {
        name: name.trim(),
        description: description ? description.trim() : null,
        address: address.trim(),
        imageUrl,
        latitude: parseFloat(latitude) || 0,
        longitude: parseFloat(longitude) || 0,
        pricePerHour: parseFloat(pricePerHour) || 0,
        availableSpots: parseInt(availableSpots) || 0,
        totalSpots: parseInt(totalSpots) || 0,
        ownerId: req.user.id,
        id: { dir: oracledb.BIND_OUT, type: oracledb.NUMBER }
      },
      { autoCommit: true }
    );

    const newId = result.outBinds.id[0];
    const created = await executeQuery('SELECT * FROM PARKINGS WHERE ID = :id', { id: newId });
    res.status(201).json(formatParking(created.rows[0]));
  } catch (error) {
    console.error('Error al crear estacionamiento:', error.message);
    res.status(500).json({ error: 'Error al crear estacionamiento' });
  }
});

// PUT /api/owner/parkings/:id
router.put('/parkings/:id', uploadMiddleware, async (req, res) => {
  try {
    const parkingId = parseInt(req.params.id);
    if (isNaN(parkingId)) return res.status(400).json({ error: 'ID invalido' });

    const { name, description, address, pricePerHour, availableSpots, totalSpots, latitude, longitude, existingImageUrl } = req.body;
    const imageUrl = req.file
      ? `${process.env.SERVER_URL}/uploads/${req.file.filename}`
      : (existingImageUrl || null);

    const check = await executeQuery(
      'SELECT ID FROM PARKINGS WHERE ID = :id AND OWNER_ID = :ownerId',
      { id: parkingId, ownerId: req.user.id }
    );
    if (check.rows.length === 0) {
      return res.status(404).json({ error: 'Estacionamiento no encontrado o no autorizado' });
    }

    await executeQuery(
      `UPDATE PARKINGS SET
        NAME = :name, DESCRIPTION = :description, ADDRESS = :address,
        IMAGE_URL = :imageUrl, LATITUDE = :latitude, LONGITUDE = :longitude,
        PRICE_PER_HOUR = :pricePerHour, AVAILABLE_SPOTS = :availableSpots, TOTAL_SPOTS = :totalSpots
       WHERE ID = :id AND OWNER_ID = :ownerId`,
      {
        name: name ? name.trim() : '',
        description: description ? description.trim() : null,
        address: address ? address.trim() : '',
        imageUrl: imageUrl || null,
        latitude: parseFloat(latitude) || 0,
        longitude: parseFloat(longitude) || 0,
        pricePerHour: parseFloat(pricePerHour) || 0,
        availableSpots: parseInt(availableSpots) || 0,
        totalSpots: parseInt(totalSpots) || 0,
        id: parkingId,
        ownerId: req.user.id
      },
      { autoCommit: true }
    );

    const updated = await executeQuery('SELECT * FROM PARKINGS WHERE ID = :id', { id: parkingId });
    res.json(formatParking(updated.rows[0]));
  } catch (error) {
    console.error('Error al actualizar estacionamiento:', error.message);
    res.status(500).json({ error: 'Error al actualizar estacionamiento' });
  }
});

// DELETE /api/owner/parkings/:id
router.delete('/parkings/:id', async (req, res) => {
  try {
    const parkingId = parseInt(req.params.id);
    if (isNaN(parkingId)) return res.status(400).json({ error: 'ID invalido' });

    const check = await executeQuery(
      'SELECT ID FROM PARKINGS WHERE ID = :id AND OWNER_ID = :ownerId',
      { id: parkingId, ownerId: req.user.id }
    );
    if (check.rows.length === 0) {
      return res.status(404).json({ error: 'Estacionamiento no encontrado o no autorizado' });
    }

    await executeQuery(
      'DELETE FROM PARKINGS WHERE ID = :id AND OWNER_ID = :ownerId',
      { id: parkingId, ownerId: req.user.id },
      { autoCommit: true }
    );

    res.json({ success: true, message: 'Estacionamiento eliminado correctamente' });
  } catch (error) {
    console.error('Error al eliminar estacionamiento:', error.message);
    res.status(500).json({ error: 'Error al eliminar estacionamiento' });
  }
});

// PATCH /api/owner/parkings/:id/status
router.patch('/parkings/:id/status', async (req, res) => {
  try {
    const parkingId = parseInt(req.params.id);
    if (isNaN(parkingId)) return res.status(400).json({ error: 'ID invalido' });

    const result = await executeQuery(
      'SELECT IS_ACTIVE FROM PARKINGS WHERE ID = :id AND OWNER_ID = :ownerId',
      { id: parkingId, ownerId: req.user.id }
    );
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Estacionamiento no encontrado o no autorizado' });
    }

    const currentStatus = result.rows[0].IS_ACTIVE;
    const newStatus = (currentStatus === 1 || currentStatus === null) ? 0 : 1;

    await executeQuery(
      'UPDATE PARKINGS SET IS_ACTIVE = :isActive WHERE ID = :id AND OWNER_ID = :ownerId',
      { isActive: newStatus, id: parkingId, ownerId: req.user.id },
      { autoCommit: true }
    );

    const updated = await executeQuery('SELECT * FROM PARKINGS WHERE ID = :id', { id: parkingId });
    res.json(formatParking(updated.rows[0]));
  } catch (error) {
    console.error('Error al cambiar estado del estacionamiento:', error.message);
    res.status(500).json({ error: 'Error al cambiar estado' });
  }
});

// GET /api/owner/dashboard
router.get('/dashboard', async (req, res) => {
  try {
    const ownerId = req.user.id;

    const parkingsResult = await executeQuery(
      `SELECT COUNT(*) AS TOTAL,
              SUM(CASE WHEN IS_ACTIVE = 1 THEN 1 ELSE 0 END) AS ACTIVE
       FROM PARKINGS WHERE OWNER_ID = :ownerId`,
      { ownerId }
    );
    const totalParkings = Number(parkingsResult.rows[0].TOTAL) || 0;
    const activeParkings = Number(parkingsResult.rows[0].ACTIVE) || 0;

    let monthRevenue = 0, monthBookings = 0;
    try {
      const monthResult = await executeQuery(
        `SELECT NVL(SUM(b.AMOUNT), 0) AS MONTH_REVENUE, COUNT(b.ID) AS MONTH_BOOKINGS
         FROM BOOKINGS b
         INNER JOIN PARKINGS p ON b.PARKING_ID = p.ID
         WHERE p.OWNER_ID = :ownerId
           AND b.STATUS = 'COMPLETED'
           AND TRUNC(b.CREATED_AT, 'MM') = TRUNC(SYSDATE, 'MM')`,
        { ownerId }
      );
      if (monthResult.rows.length > 0) {
        monthRevenue = Number(monthResult.rows[0].MONTH_REVENUE) || 0;
        monthBookings = Number(monthResult.rows[0].MONTH_BOOKINGS) || 0;
      }
    } catch (e) { /* tabla aún no creada */ }

    let recentBookings = [];
    try {
      const recentResult = await executeQuery(
        `SELECT b.ID, p.NAME AS PARKING_NAME, u.NAME AS DRIVER_NAME,
                b.AMOUNT, b.STATUS, b.CREATED_AT, b.HOURS
         FROM BOOKINGS b
         INNER JOIN PARKINGS p ON b.PARKING_ID = p.ID
         INNER JOIN APP_USERS u ON b.DRIVER_ID = u.ID
         WHERE p.OWNER_ID = :ownerId
         ORDER BY b.CREATED_AT DESC
         FETCH FIRST 5 ROWS ONLY`,
        { ownerId }
      );
      recentBookings = recentResult.rows.map(row => ({
        id: row.ID,
        parkingName: row.PARKING_NAME,
        driverName: row.DRIVER_NAME,
        amount: Number(row.AMOUNT) || 0,
        status: row.STATUS,
        createdAt: row.CREATED_AT ? new Date(row.CREATED_AT).toISOString() : null,
        hours: Number(row.HOURS) || 1
      }));
    } catch (e) { /* tabla aún no creada */ }

    res.json({ activeParkings, totalParkings, monthRevenue, monthBookings, recentBookings });
  } catch (error) {
    console.error('Error al obtener dashboard:', error.message);
    res.status(500).json({ error: 'Error al obtener datos del dashboard' });
  }
});

// GET /api/owner/stats
router.get('/stats', async (req, res) => {
  try {
    const ownerId = req.user.id;

    // Totales globales
    let totalRevenue = 0, totalBookings = 0, uniqueDrivers = 0;
    try {
      const totalsResult = await executeQuery(
        `SELECT NVL(SUM(b.AMOUNT), 0) AS TOTAL_REVENUE,
                COUNT(b.ID) AS TOTAL_BOOKINGS,
                COUNT(DISTINCT b.DRIVER_ID) AS UNIQUE_DRIVERS
         FROM PARKINGS p
         LEFT JOIN BOOKINGS b ON p.ID = b.PARKING_ID AND b.STATUS = 'COMPLETED'
         WHERE p.OWNER_ID = :ownerId`,
        { ownerId }
      );
      if (totalsResult.rows.length > 0) {
        totalRevenue = Number(totalsResult.rows[0].TOTAL_REVENUE) || 0;
        totalBookings = Number(totalsResult.rows[0].TOTAL_BOOKINGS) || 0;
        uniqueDrivers = Number(totalsResult.rows[0].UNIQUE_DRIVERS) || 0;
      }
    } catch (e) {
      console.warn('Tabla BOOKINGS no disponible:', e.message);
    }

    // Ultimos 6 meses con etiquetas en espanol
    const esMonths = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    const now = new Date();
    const monthKeys = [];
    const monthLabels = [];
    for (let i = 5; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      monthKeys.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
      monthLabels.push(esMonths[d.getMonth()]);
    }

    const monthlyRevenue = new Array(6).fill(0);
    const monthlyBookings = new Array(6).fill(0);

    try {
      const monthlyResult = await executeQuery(
        `SELECT TO_CHAR(b.CREATED_AT, 'YYYY-MM') AS MONTH,
                SUM(b.AMOUNT) AS REVENUE,
                COUNT(*) AS BOOKINGS
         FROM BOOKINGS b
         INNER JOIN PARKINGS p ON b.PARKING_ID = p.ID
         WHERE p.OWNER_ID = :ownerId
           AND b.STATUS = 'COMPLETED'
           AND b.CREATED_AT >= ADD_MONTHS(SYSDATE, -6)
         GROUP BY TO_CHAR(b.CREATED_AT, 'YYYY-MM')
         ORDER BY MONTH ASC`,
        { ownerId }
      );

      monthlyResult.rows.forEach(row => {
        const idx = monthKeys.indexOf(row.MONTH);
        if (idx !== -1) {
          monthlyRevenue[idx] = Number(row.REVENUE) || 0;
          monthlyBookings[idx] = Number(row.BOOKINGS) || 0;
        }
      });
    } catch (e) {
      console.warn('Error al consultar datos mensuales:', e.message);
    }

    res.json({ totalRevenue, totalBookings, uniqueDrivers, monthlyRevenue, monthlyBookings, monthLabels });
  } catch (error) {
    console.error('Error al obtener estadisticas:', error.message);
    res.status(500).json({ error: 'Error al obtener estadisticas' });
  }
});

module.exports = router;
