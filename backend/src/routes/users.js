const express = require('express');
const multer = require('multer');
const path = require('path');
const { executeQuery } = require('../config/database');
const { authenticateToken } = require('../middleware/authMiddleware');

const router = express.Router();

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, path.join(__dirname, '../../uploads'));
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, `user_${req.user.id}_${Date.now()}${ext}`);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    const allowed = /jpeg|jpg|png|webp/;
    if (allowed.test(path.extname(file.originalname).toLowerCase()) && allowed.test(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Solo se permiten imagenes JPG, PNG o WEBP'));
    }
  }
});

// GET /api/users/profile
router.get('/profile', authenticateToken, async (req, res) => {
  try {
    const result = await executeQuery(
      'SELECT ID, NAME, EMAIL, USER_TYPE, VEHICLE_BRAND, VEHICLE_PLATE, PHOTO_URL, ADDRESS, COMMUNE, REGION FROM APP_USERS WHERE ID = :id',
      { id: req.user.id }
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }

    const u = result.rows[0];
    res.json({
      id: u.ID,
      name: u.NAME,
      email: u.EMAIL,
      userType: u.USER_TYPE,
      vehicleBrand: u.VEHICLE_BRAND || null,
      vehiclePlate: u.VEHICLE_PLATE || null,
      photoUrl: u.PHOTO_URL || null,
      address: u.ADDRESS || null,
      commune: u.COMMUNE || null,
      region: u.REGION || null
    });
  } catch (error) {
    console.error('Error al obtener perfil:', error.message);
    res.status(500).json({ error: 'Error al obtener perfil' });
  }
});

// PUT /api/users/profile
router.put('/profile', authenticateToken, async (req, res) => {
  try {
    const { name, vehicleBrand, vehiclePlate, address, commune, region } = req.body;

    if (!name || name.trim().length === 0) {
      return res.status(400).json({ error: 'El nombre es requerido' });
    }

    await executeQuery(
      `UPDATE APP_USERS
       SET NAME = :name, VEHICLE_BRAND = :vehicleBrand, VEHICLE_PLATE = :vehiclePlate,
           ADDRESS = :address, COMMUNE = :commune, REGION = :region, UPDATED_AT = SYSTIMESTAMP
       WHERE ID = :id`,
      {
        name: name.trim(),
        vehicleBrand: vehicleBrand || null,
        vehiclePlate: vehiclePlate || null,
        address: address || null,
        commune: commune || null,
        region: region || null,
        id: req.user.id
      },
      { autoCommit: true }
    );

    const result = await executeQuery(
      'SELECT ID, NAME, EMAIL, USER_TYPE, VEHICLE_BRAND, VEHICLE_PLATE, PHOTO_URL, ADDRESS, COMMUNE, REGION FROM APP_USERS WHERE ID = :id',
      { id: req.user.id }
    );

    const u = result.rows[0];
    res.json({
      id: u.ID,
      name: u.NAME,
      email: u.EMAIL,
      userType: u.USER_TYPE,
      vehicleBrand: u.VEHICLE_BRAND || null,
      vehiclePlate: u.VEHICLE_PLATE || null,
      photoUrl: u.PHOTO_URL || null,
      address: u.ADDRESS || null,
      commune: u.COMMUNE || null,
      region: u.REGION || null
    });
  } catch (error) {
    console.error('Error al actualizar perfil:', error.message);
    res.status(500).json({ error: 'Error al actualizar perfil' });
  }
});

// POST /api/users/photo
router.post('/photo', authenticateToken, upload.single('photo'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No se proporciono ninguna imagen' });
    }

    const photoUrl = `/uploads/${req.file.filename}`;

    await executeQuery(
      'UPDATE APP_USERS SET PHOTO_URL = :photoUrl, UPDATED_AT = SYSTIMESTAMP WHERE ID = :id',
      { photoUrl, id: req.user.id },
      { autoCommit: true }
    );

    res.json({ photoUrl });
  } catch (error) {
    console.error('Error al subir foto:', error.message);
    res.status(500).json({ error: 'Error al subir foto de perfil' });
  }
});

module.exports = router;
