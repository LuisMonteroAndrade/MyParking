const express = require('express');
const { executeQuery, oracledb } = require('../config/database');
const { authenticateToken } = require('../middleware/authMiddleware');

const router = express.Router();

router.use(authenticateToken);

// POST /api/bookings
router.post('/', async (req, res) => {
  try {
    const { parkingId, hours, paymentToken, paymentMethod } = req.body;
    const driverId = req.user.id;

    if (!parkingId || !hours || hours < 1 || hours > 24) {
      return res.status(400).json({ error: 'parkingId y hours (1-24) son requeridos' });
    }

    // Verificar que el estacionamiento existe y está activo
    const parkingResult = await executeQuery(
      `SELECT ID, PRICE_PER_HOUR, AVAILABLE_SPOTS, IS_ACTIVE, NAME
       FROM PARKINGS WHERE ID = :parkingId`,
      { parkingId }
    );

    if (parkingResult.rows.length === 0) {
      return res.status(404).json({ error: 'Estacionamiento no encontrado' });
    }

    const parking = parkingResult.rows[0];

    if (!parking.IS_ACTIVE || parking.IS_ACTIVE === 0) {
      return res.status(400).json({ error: 'Este estacionamiento no está disponible' });
    }

    if (parking.AVAILABLE_SPOTS <= 0) {
      return res.status(400).json({ error: 'No hay lugares disponibles' });
    }

    const amount = parseFloat(parking.PRICE_PER_HOUR) * parseInt(hours);
    const method = paymentMethod || 'PENDING';
    const status = (method === 'GOOGLE_PAY' && paymentToken) ? 'COMPLETED' : 'PENDING';

    // Crear reserva
    const bookingResult = await executeQuery(
      `INSERT INTO BOOKINGS (PARKING_ID, DRIVER_ID, AMOUNT, STATUS, HOURS, PAYMENT_METHOD, PAYMENT_TOKEN, CREATED_AT)
       VALUES (:parkingId, :driverId, :amount, :status, :hours, :paymentMethod, :paymentToken, SYSDATE)
       RETURNING ID INTO :id`,
      {
        parkingId,
        driverId,
        amount,
        status,
        hours: parseInt(hours),
        paymentMethod: method,
        paymentToken: paymentToken ? paymentToken.substring(0, 500) : null,
        id: { dir: oracledb.BIND_OUT, type: oracledb.NUMBER }
      },
      { autoCommit: false }
    );

    const newBookingId = bookingResult.outBinds.id[0];

    // Reducir los espacios disponibles
    await executeQuery(
      `UPDATE PARKINGS SET AVAILABLE_SPOTS = AVAILABLE_SPOTS - 1
       WHERE ID = :parkingId AND AVAILABLE_SPOTS > 0`,
      { parkingId },
      { autoCommit: true }
    );

    // Obtener la reserva creada
    const created = await executeQuery(
      `SELECT b.ID, b.PARKING_ID, b.DRIVER_ID, b.AMOUNT, b.STATUS, b.HOURS, b.CREATED_AT
       FROM BOOKINGS b WHERE b.ID = :id`,
      { id: newBookingId }
    );

    const row = created.rows[0];
    res.status(201).json({
      id: row.ID,
      parkingId: row.PARKING_ID,
      driverId: row.DRIVER_ID,
      amount: Number(row.AMOUNT),
      status: row.STATUS,
      hours: Number(row.HOURS),
      createdAt: row.CREATED_AT ? new Date(row.CREATED_AT).toISOString() : null
    });
  } catch (error) {
    console.error('Error al crear reserva:', error.message);
    res.status(500).json({ error: 'Error al procesar la reserva' });
  }
});

// GET /api/bookings/my - historial de reservas del conductor
router.get('/my', async (req, res) => {
  try {
    const driverId = req.user.id;

    const result = await executeQuery(
      `SELECT b.ID, b.PARKING_ID, p.NAME AS PARKING_NAME, p.ADDRESS,
              b.AMOUNT, b.STATUS, b.HOURS, b.PAYMENT_METHOD, b.CREATED_AT
       FROM BOOKINGS b
       INNER JOIN PARKINGS p ON b.PARKING_ID = p.ID
       WHERE b.DRIVER_ID = :driverId
       ORDER BY b.CREATED_AT DESC
       FETCH FIRST 20 ROWS ONLY`,
      { driverId }
    );

    const bookings = result.rows.map(row => ({
      id: row.ID,
      parkingId: row.PARKING_ID,
      parkingName: row.PARKING_NAME,
      address: row.ADDRESS,
      amount: Number(row.AMOUNT),
      status: row.STATUS,
      hours: Number(row.HOURS),
      paymentMethod: row.PAYMENT_METHOD,
      createdAt: row.CREATED_AT ? new Date(row.CREATED_AT).toISOString() : null
    }));

    res.json(bookings);
  } catch (error) {
    console.error('Error al obtener reservas:', error.message);
    res.status(500).json({ error: 'Error al obtener historial de reservas' });
  }
});

module.exports = router;
