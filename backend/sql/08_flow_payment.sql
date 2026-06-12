-- =========================================================
-- MiEstacionamiento - Soporte para pagos Flow
-- Ejecutar en SQL Worksheet de Oracle ADB como ADMIN
-- =========================================================

-- 1. Agregar columnas de pago (si no existen ya)
ALTER TABLE BOOKINGS ADD PAYMENT_METHOD VARCHAR2(30) DEFAULT 'FLOW';
ALTER TABLE BOOKINGS ADD PAYMENT_TOKEN  VARCHAR2(500);

-- 2. Ampliar el CHECK constraint del STATUS para incluir los nuevos estados
--    El nombre SYS_C0024500 es el que reportó el error ORA-02290
ALTER TABLE BOOKINGS DROP CONSTRAINT SYS_C0024500;
ALTER TABLE BOOKINGS ADD CONSTRAINT CHK_BOOKINGS_STATUS
    CHECK (STATUS IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'PENDING_PAYMENT', 'FAILED'));

COMMIT;
