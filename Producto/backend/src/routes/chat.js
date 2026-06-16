const express = require('express');
const { executeQuery, oracledb } = require('../config/database');
const { authenticateToken: authMiddleware } = require('../middleware/authMiddleware');

const router = express.Router();

// GET /api/chat/conversations — conversaciones del usuario autenticado
router.get('/conversations', authMiddleware, async (req, res) => {
  try {
    const userId = req.user.id;
    const result = await executeQuery(
      `SELECT c.ID, c.DRIVER_ID, c.OWNER_ID, c.PARKING_ID,
              c.LAST_MESSAGE_AT, c.CREATED_AT,
              d.NAME as DRIVER_NAME, d.PHOTO_URL as DRIVER_PHOTO,
              o.NAME as OWNER_NAME,  o.PHOTO_URL as OWNER_PHOTO,
              p.NAME as PARKING_NAME,
              (SELECT CONTENT FROM MESSAGES WHERE CONVERSATION_ID = c.ID
               ORDER BY CREATED_AT DESC FETCH FIRST 1 ROW ONLY) as LAST_MESSAGE,
              (SELECT COUNT(*) FROM MESSAGES
               WHERE CONVERSATION_ID = c.ID AND SENDER_ID != :userId AND IS_READ = 0) as UNREAD_COUNT
       FROM CONVERSATIONS c
       JOIN APP_USERS d ON d.ID = c.DRIVER_ID
       JOIN APP_USERS o ON o.ID = c.OWNER_ID
       LEFT JOIN PARKINGS p ON p.ID = c.PARKING_ID
       WHERE c.DRIVER_ID = :userId OR c.OWNER_ID = :userId
       ORDER BY c.LAST_MESSAGE_AT DESC`,
      { userId }
    );
    res.json(result.rows.map(c => ({
      id: c.ID,
      driverId: c.DRIVER_ID,
      ownerId: c.OWNER_ID,
      parkingId: c.PARKING_ID || null,
      lastMessageAt: c.LAST_MESSAGE_AT || null,
      driverName: c.DRIVER_NAME,
      driverPhoto: c.DRIVER_PHOTO || null,
      ownerName: c.OWNER_NAME,
      ownerPhoto: c.OWNER_PHOTO || null,
      parkingName: c.PARKING_NAME || null,
      lastMessage: c.LAST_MESSAGE || null,
      unreadCount: c.UNREAD_COUNT || 0
    })));
  } catch (error) {
    console.error('Error obteniendo conversaciones:', error.message);
    res.status(500).json({ error: 'Error al obtener conversaciones' });
  }
});

// POST /api/chat/conversations — iniciar conversación (conductor con propietario de un parking)
router.post('/conversations', authMiddleware, async (req, res) => {
  try {
    const { parkingId } = req.body;
    const driverId = req.user.id;

    if (!parkingId) {
      return res.status(400).json({ error: 'parkingId es requerido' });
    }

    const parkingResult = await executeQuery(
      'SELECT OWNER_ID, NAME FROM PARKINGS WHERE ID = :parkingId',
      { parkingId }
    );
    if (parkingResult.rows.length === 0) {
      return res.status(404).json({ error: 'Estacionamiento no encontrado' });
    }

    const ownerId = parkingResult.rows[0].OWNER_ID;
    const parkingName = parkingResult.rows[0].NAME;

    if (driverId === ownerId) {
      return res.status(400).json({ error: 'No puedes chatear contigo mismo' });
    }

    const existing = await executeQuery(
      `SELECT ID FROM CONVERSATIONS
       WHERE DRIVER_ID = :driverId AND OWNER_ID = :ownerId AND PARKING_ID = :parkingId`,
      { driverId, ownerId, parkingId }
    );

    let conversationId;
    if (existing.rows.length > 0) {
      conversationId = existing.rows[0].ID;
    } else {
      const result = await executeQuery(
        `INSERT INTO CONVERSATIONS (DRIVER_ID, OWNER_ID, PARKING_ID)
         VALUES (:driverId, :ownerId, :parkingId)
         RETURNING ID INTO :id`,
        {
          driverId, ownerId, parkingId,
          id: { dir: oracledb.BIND_OUT, type: oracledb.NUMBER }
        },
        { autoCommit: true }
      );
      conversationId = result.outBinds.id[0];
    }

    res.status(201).json({ id: conversationId, parkingName, ownerId, driverId });
  } catch (error) {
    console.error('Error iniciando conversación:', error.message);
    res.status(500).json({ error: 'Error al iniciar conversación' });
  }
});

// GET /api/chat/conversations/:id/messages — mensajes de una conversación
router.get('/conversations/:id/messages', authMiddleware, async (req, res) => {
  try {
    const conversationId = parseInt(req.params.id);
    const userId = req.user.id;

    const convCheck = await executeQuery(
      'SELECT ID FROM CONVERSATIONS WHERE ID = :id AND (DRIVER_ID = :userId OR OWNER_ID = :userId)',
      { id: conversationId, userId }
    );
    if (convCheck.rows.length === 0) {
      return res.status(403).json({ error: 'No tienes acceso a esta conversación' });
    }

    const result = await executeQuery(
      `SELECT m.ID, m.SENDER_ID, m.CONTENT, m.IS_READ, m.CREATED_AT,
              u.NAME as SENDER_NAME
       FROM MESSAGES m
       JOIN APP_USERS u ON u.ID = m.SENDER_ID
       WHERE m.CONVERSATION_ID = :conversationId
       ORDER BY m.CREATED_AT ASC`,
      { conversationId }
    );

    // Marcar mensajes del otro usuario como leídos
    await executeQuery(
      `UPDATE MESSAGES SET IS_READ = 1
       WHERE CONVERSATION_ID = :conversationId AND SENDER_ID != :userId AND IS_READ = 0`,
      { conversationId, userId },
      { autoCommit: true }
    );

    res.json(result.rows.map(m => ({
      id: m.ID,
      senderId: m.SENDER_ID,
      senderName: m.SENDER_NAME,
      content: m.CONTENT,
      isRead: m.IS_READ === 1,
      createdAt: m.CREATED_AT
    })));
  } catch (error) {
    console.error('Error obteniendo mensajes:', error.message);
    res.status(500).json({ error: 'Error al obtener mensajes' });
  }
});

// POST /api/chat/conversations/:id/messages — enviar mensaje
router.post('/conversations/:id/messages', authMiddleware, async (req, res) => {
  try {
    const conversationId = parseInt(req.params.id);
    const { content } = req.body;
    const senderId = req.user.id;

    if (!content || content.trim() === '') {
      return res.status(400).json({ error: 'El mensaje no puede estar vacío' });
    }

    const convCheck = await executeQuery(
      'SELECT ID FROM CONVERSATIONS WHERE ID = :id AND (DRIVER_ID = :senderId OR OWNER_ID = :senderId)',
      { id: conversationId, senderId }
    );
    if (convCheck.rows.length === 0) {
      return res.status(403).json({ error: 'No tienes acceso a esta conversación' });
    }

    const result = await executeQuery(
      `INSERT INTO MESSAGES (CONVERSATION_ID, SENDER_ID, CONTENT)
       VALUES (:conversationId, :senderId, :content)
       RETURNING ID INTO :id`,
      {
        conversationId, senderId, content: content.trim(),
        id: { dir: oracledb.BIND_OUT, type: oracledb.NUMBER }
      },
      { autoCommit: true }
    );

    await executeQuery(
      'UPDATE CONVERSATIONS SET LAST_MESSAGE_AT = CURRENT_TIMESTAMP WHERE ID = :conversationId',
      { conversationId },
      { autoCommit: true }
    );

    res.status(201).json({
      id: result.outBinds.id[0],
      conversationId,
      senderId,
      content: content.trim(),
      isRead: false,
      createdAt: new Date().toISOString()
    });
  } catch (error) {
    console.error('Error enviando mensaje:', error.message);
    res.status(500).json({ error: 'Error al enviar mensaje' });
  }
});

module.exports = router;
