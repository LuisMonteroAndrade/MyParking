const admin = require('firebase-admin');
const path = require('path');
const { executeQuery } = require('../config/database');

let initialized = false;

function init() {
  if (initialized) return;
  const keyPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (!keyPath) {
    console.warn('[FCM] FIREBASE_SERVICE_ACCOUNT_PATH no configurado – notificaciones push desactivadas.');
    return;
  }
  try {
    const serviceAccount = require(path.resolve(keyPath));
    admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
    initialized = true;
    console.log('[FCM] Firebase Admin SDK inicializado correctamente.');
  } catch (err) {
    console.error('[FCM] Error al inicializar Firebase Admin SDK:', err.message);
  }
}

async function getToken(userId) {
  try {
    const r = await executeQuery('SELECT FCM_TOKEN FROM APP_USERS WHERE ID = :id', { id: userId });
    return r.rows[0]?.FCM_TOKEN || null;
  } catch {
    return null;
  }
}

async function send(token, data) {
  if (!initialized || !token) return;
  try {
    await admin.messaging().send({
      token,
      data,
      android: { priority: 'high' }
    });
  } catch (err) {
    console.error('[FCM] Error al enviar push:', err.message);
  }
}

// NEW_MESSAGE — notifica al destinatario cuando llega un mensaje
async function notifyNewMessage(conversationId, senderId, messagePreview) {
  try {
    const r = await executeQuery(
      `SELECT c.DRIVER_ID, c.OWNER_ID, u.NAME as SENDER_NAME
       FROM CONVERSATIONS c
       JOIN APP_USERS u ON u.ID = :senderId
       WHERE c.ID = :convId`,
      { senderId, convId: conversationId }
    );
    if (!r.rows[0]) return;
    const { DRIVER_ID, OWNER_ID, SENDER_NAME } = r.rows[0];
    const recipientId = senderId === DRIVER_ID ? OWNER_ID : DRIVER_ID;
    await send(await getToken(recipientId), {
      type: 'NEW_MESSAGE',
      senderName: SENDER_NAME,
      preview: String(messagePreview).substring(0, 100),
      conversationId: String(conversationId)
    });
  } catch (err) {
    console.error('[FCM] notifyNewMessage error:', err.message);
  }
}

// BOOKING_CONFIRMED — notifica al conductor cuando su reserva se confirma
async function notifyBookingConfirmed(driverId, bookingId, parkingName, hours) {
  try {
    await send(await getToken(driverId), {
      type: 'BOOKING_CONFIRMED',
      parkingName,
      hours: String(hours),
      bookingId: String(bookingId)
    });
  } catch (err) {
    console.error('[FCM] notifyBookingConfirmed error:', err.message);
  }
}

// NEW_BOOKING — notifica al propietario cuando alguien reserva su estacionamiento
async function notifyNewBooking(ownerId, driverId, bookingId, parkingName, hours) {
  try {
    const r = await executeQuery('SELECT NAME FROM APP_USERS WHERE ID = :id', { id: driverId });
    const driverName = r.rows[0]?.NAME || 'Un conductor';
    await send(await getToken(ownerId), {
      type: 'NEW_BOOKING',
      driverName,
      parkingName,
      hours: String(hours),
      bookingId: String(bookingId)
    });
  } catch (err) {
    console.error('[FCM] notifyNewBooking error:', err.message);
  }
}

// NEW_REVIEW — notifica al propietario cuando alguien deja una reseña nueva
async function notifyNewReview(ownerId, reviewId, parkingName, rating) {
  try {
    await send(await getToken(ownerId), {
      type: 'NEW_REVIEW',
      parkingName,
      rating: String(rating),
      reviewId: String(reviewId)
    });
  } catch (err) {
    console.error('[FCM] notifyNewReview error:', err.message);
  }
}

module.exports = { init, notifyNewMessage, notifyBookingConfirmed, notifyNewBooking, notifyNewReview };
