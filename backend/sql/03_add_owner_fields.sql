-- =========================================================
-- Migración: Agregar campos de propietario a APP_USERS
-- Ejecutar solo si la tabla ya existe (instalaciones previas)
-- =========================================================

ALTER TABLE APP_USERS ADD (
    ADDRESS  VARCHAR2(300),
    COMMUNE  VARCHAR2(100),
    REGION   VARCHAR2(100)
);

COMMIT;
