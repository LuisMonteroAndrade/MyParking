const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { executeQuery, oracledb } = require('../config/database');

const router = express.Router();

// POST /api/auth/register
router.post('/register', async (req, res) => {
  try {
    const { name, email, password, userType, vehicleBrand, vehiclePlate, photoUrl, address, commune, region } = req.body;

    if (!name || !email || !password || !userType) {
      return res.status(400).json({ error: 'Nombre, email, contrasena y tipo de usuario son requeridos' });
    }

    if (!['DRIVER', 'OWNER'].includes(userType)) {
      return res.status(400).json({ error: 'Tipo de usuario debe ser DRIVER u OWNER' });
    }

    const existing = await executeQuery(
      'SELECT ID FROM APP_USERS WHERE EMAIL = :email',
      { email: email.toLowerCase().trim() }
    );

    if (existing.rows.length > 0) {
      return res.status(409).json({ error: 'El email ya esta registrado' });
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const result = await executeQuery(
      `INSERT INTO APP_USERS (NAME, EMAIL, PASSWORD, USER_TYPE, VEHICLE_BRAND, VEHICLE_PLATE, PHOTO_URL, ADDRESS, COMMUNE, REGION)
       VALUES (:name, :email, :password, :userType, :vehicleBrand, :vehiclePlate, :photoUrl, :address, :commune, :region)
       RETURNING ID INTO :id`,
      {
        name: name.trim(),
        email: email.toLowerCase().trim(),
        password: hashedPassword,
        userType,
        vehicleBrand: vehicleBrand || null,
        vehiclePlate: vehiclePlate || null,
        photoUrl: photoUrl || null,
        address: address || null,
        commune: commune || null,
        region: region || null,
        id: { dir: oracledb.BIND_OUT, type: oracledb.NUMBER }
      },
      { autoCommit: true }
    );

    const userId = result.outBinds.id[0];

    const token = jwt.sign(
      { id: userId, email: email.toLowerCase().trim(), userType },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );

    res.status(201).json({
      token,
      user: {
        id: userId,
        name: name.trim(),
        email: email.toLowerCase().trim(),
        userType,
        vehicleBrand: vehicleBrand || null,
        vehiclePlate: vehiclePlate || null,
        photoUrl: photoUrl || null,
        address: address || null,
        commune: commune || null,
        region: region || null
      }
    });
  } catch (error) {
    console.error('Error en registro:', error.message);
    res.status(500).json({ error: 'Error al registrar usuario' });
  }
});

// POST /api/auth/login
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email y contrasena son requeridos' });
    }

    const result = await executeQuery(
      'SELECT * FROM APP_USERS WHERE EMAIL = :email',
      { email: email.toLowerCase().trim() }
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Email o contrasena incorrectos' });
    }

    const user = result.rows[0];
    const passwordMatch = await bcrypt.compare(password, user.PASSWORD);

    if (!passwordMatch) {
      return res.status(401).json({ error: 'Email o contrasena incorrectos' });
    }

    const token = jwt.sign(
      { id: user.ID, email: user.EMAIL, userType: user.USER_TYPE },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );

    res.json({
      token,
      user: {
        id: user.ID,
        name: user.NAME,
        email: user.EMAIL,
        userType: user.USER_TYPE,
        vehicleBrand: user.VEHICLE_BRAND || null,
        vehiclePlate: user.VEHICLE_PLATE || null,
        photoUrl: user.PHOTO_URL || null,
        address: user.ADDRESS || null,
        commune: user.COMMUNE || null,
        region: user.REGION || null
      }
    });
  } catch (error) {
    console.error('Error en login:', error.message);
    res.status(500).json({ error: 'Error al iniciar sesion' });
  }
});

module.exports = router;
