const express = require('express');
const { executeQuery, oracledb } = require('../config/database');
const { authenticateToken } = require('../middleware/authMiddleware');
const { createPayment, getPaymentStatus } = require('../services/flowService');

const router = express.Router();

const getServerUrl = () => process.env.SERVER_URL || `http://192.168.100.218:${process.env.PORT || 3000}`;

// POST /api/payments/flow/create - inicia un pago Flow y crea la reserva
router.post('/flow/create', authenticateToken, async (req, res) => {
  try {
    const { parkingId, hours } = req.body;
    const driverId = req.user.id;
    const userEmail = req.user.email;

    if (!parkingId || !hours || hours < 1 || hours > 24) {
      return res.status(400).json({ error: 'parkingId y hours (1-24) son requeridos' });
    }

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

    const amount = Math.round(parseFloat(parking.PRICE_PER_HOUR) * parseInt(hours));

    // Crear reserva con estado PENDING_PAYMENT
    const bookingResult = await executeQuery(
      `INSERT INTO BOOKINGS (PARKING_ID, DRIVER_ID, AMOUNT, STATUS, HOURS, PAYMENT_METHOD, CREATED_AT)
       VALUES (:parkingId, :driverId, :amount, 'PENDING_PAYMENT', :hours, 'FLOW', SYSDATE)
       RETURNING ID INTO :id`,
      {
        parkingId,
        driverId,
        amount,
        hours: parseInt(hours),
        id: { dir: oracledb.BIND_OUT, type: oracledb.NUMBER }
      },
      { autoCommit: true }
    );

    const bookingId = bookingResult.outBinds.id[0];
    const serverUrl = getServerUrl();

    const returnUrl = `${serverUrl}/api/payments/flow/return?bookingId=${bookingId}`;

    // Crear pago en Flow
    const { paymentUrl, token } = await createPayment({
      commerceOrder: `BOOKING-${bookingId}`,
      subject: `Reserva ${parking.NAME} - ${hours}h`,
      amount,
      email: userEmail,
      urlConfirmation: `${serverUrl}/api/payments/flow/confirm`,
      urlReturn: returnUrl
    });

    // Guardar token de Flow en la reserva
    await executeQuery(
      `UPDATE BOOKINGS SET PAYMENT_TOKEN = :token WHERE ID = :bookingId`,
      { token: token.substring(0, 500), bookingId },
      { autoCommit: true }
    );

    res.json({ bookingId, paymentUrl, amount });

  } catch (error) {
    console.error('Error al crear pago Flow:', error.message);
    if (error.message.includes('no configurada')) {
      return res.status(503).json({ error: 'Sistema de pago no configurado. Contacta al administrador.' });
    }
    res.status(500).json({ error: 'Error al crear el pago' });
  }
});

// POST /api/payments/flow/confirm - webhook llamado por los servidores de Flow
router.post('/flow/confirm', async (req, res) => {
  try {
    const { token } = req.body;
    if (!token) return res.status(400).send('Token requerido');

    await processFlowPayment(token);
    res.status(200).send('OK');

  } catch (error) {
    console.error('Error en webhook Flow:', error.message);
    res.status(500).send('Error');
  }
});

// GET /api/payments/flow/return - redirección del navegador tras el pago
router.get('/flow/return', async (req, res) => {
  const { token, bookingId } = req.query;
  let isSuccess = false;

  try {
    if (token) {
      const result = await processFlowPayment(token);
      isSuccess = result === 'COMPLETED';
    }
  } catch (error) {
    console.error('Error al verificar pago en retorno:', error.message);
  }

  res.send(`<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${isSuccess ? 'Pago confirmado' : 'Pago no completado'}</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
           display: flex; justify-content: center; align-items: center;
           min-height: 100vh; background: #f0f4f8; }
    .card { background: white; border-radius: 20px; padding: 40px 32px;
            text-align: center; max-width: 340px; width: 90%;
            box-shadow: 0 8px 32px rgba(0,0,0,0.12); }
    .icon { font-size: 72px; margin-bottom: 20px; }
    h2 { font-size: 22px; margin-bottom: 12px;
         color: ${isSuccess ? '#2e7d32' : '#c62828'}; }
    p { color: #666; font-size: 15px; line-height: 1.5; }
    .booking { margin-top: 16px; background: #f5f5f5; border-radius: 12px;
               padding: 12px; font-size: 14px; color: #444; }
  </style>
</head>
<body>
  <div class="card">
    <div class="icon">${isSuccess ? '✅' : '❌'}</div>
    <h2>${isSuccess ? '¡Pago confirmado!' : 'Pago no completado'}</h2>
    <p>${isSuccess
      ? 'Tu reserva ha sido confirmada exitosamente.'
      : 'El pago no pudo procesarse. Vuelve a la app e intenta nuevamente.'}</p>
    ${bookingId ? `<div class="booking">Reserva #${bookingId}</div>` : ''}
    <p style="margin-top:20px; font-size:13px; color:#999;">
      Cierra esta ventana y vuelve a la app.
    </p>
  </div>
</body>
</html>`);
});

async function processFlowPayment(token) {
  const paymentStatus = await getPaymentStatus(token);
  // Flow status: 1=pending, 2=paid, 3=rejected, 4=cancelled
  const newStatus = paymentStatus.status === 2 ? 'COMPLETED' : 'FAILED';

  const bookingResult = await executeQuery(
    `SELECT ID, PARKING_ID FROM BOOKINGS WHERE PAYMENT_TOKEN = :token`,
    { token }
  );

  if (bookingResult.rows.length === 0) return newStatus;

  const booking = bookingResult.rows[0];

  // Solo actualizar si todavía está pendiente
  await executeQuery(
    `UPDATE BOOKINGS SET STATUS = :status WHERE ID = :id AND STATUS = 'PENDING_PAYMENT'`,
    { status: newStatus, id: booking.ID },
    { autoCommit: true }
  );

  if (newStatus === 'COMPLETED') {
    await executeQuery(
      `UPDATE PARKINGS SET AVAILABLE_SPOTS = AVAILABLE_SPOTS - 1
       WHERE ID = :parkingId AND AVAILABLE_SPOTS > 0`,
      { parkingId: booking.PARKING_ID },
      { autoCommit: true }
    );
  }

  return newStatus;
}

module.exports = router;
