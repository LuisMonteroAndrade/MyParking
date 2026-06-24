-- Migración 09: Agregar columna FCM_TOKEN para notificaciones push
ALTER TABLE APP_USERS ADD FCM_TOKEN VARCHAR2(500);
