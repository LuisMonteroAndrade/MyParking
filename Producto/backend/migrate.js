require('dotenv').config();
const { initializePool, executeQuery, closePool } = require('./src/config/database');

async function runMigration() {
  console.log('Conectando a Oracle ADB...');
  await initializePool();

  try {
    // Verificar si las columnas ya existen
    const check = await executeQuery(
      `SELECT COLUMN_NAME FROM USER_TAB_COLUMNS
       WHERE TABLE_NAME = 'APP_USERS'
         AND COLUMN_NAME IN ('ADDRESS', 'COMMUNE', 'REGION')`
    );

    const existing = check.rows.map(r => r.COLUMN_NAME);
    console.log('Columnas ya existentes:', existing.length > 0 ? existing.join(', ') : 'ninguna');

    const toAdd = [];
    if (!existing.includes('ADDRESS')) toAdd.push('ADDRESS  VARCHAR2(300)');
    if (!existing.includes('COMMUNE')) toAdd.push('COMMUNE  VARCHAR2(100)');
    if (!existing.includes('REGION'))  toAdd.push('REGION   VARCHAR2(100)');

    if (toAdd.length === 0) {
      console.log('Las columnas ADDRESS, COMMUNE y REGION ya existen. No se requiere migración.');
      return;
    }

    console.log(`Agregando columnas: ${toAdd.map(c => c.split(' ')[0]).join(', ')}...`);
    await executeQuery(
      `ALTER TABLE APP_USERS ADD (${toAdd.join(', ')})`,
      [],
      { autoCommit: true }
    );

    console.log('Migración completada exitosamente.');
  } finally {
    await closePool();
  }
}

runMigration().catch(err => {
  console.error('Error en la migración:', err.message);
  process.exit(1);
});
