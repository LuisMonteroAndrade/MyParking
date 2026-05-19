const oracledb = require('oracledb');
const path = require('path');

// Directorio del wallet de Oracle ADB (colocar los archivos del wallet aqui)
const WALLET_LOCATION = path.join(__dirname, '../../wallet');

let pool;

async function initializePool() {
  // Thin mode: no requiere Oracle Instant Client instalado
  oracledb.autoCommit = true;
  oracledb.outFormat = oracledb.OUT_FORMAT_OBJECT;

  pool = await oracledb.createPool({
    user: process.env.DB_USER || 'ADMIN',
    password: process.env.DB_PASSWORD,
    connectString: process.env.DB_CONNECT_STRING,
    walletLocation: WALLET_LOCATION,
    walletPassword: process.env.WALLET_PASSWORD,
    poolMin: 1,
    poolMax: 5,
    poolIncrement: 1,
    poolTimeout: 300,
    queueTimeout: 60000
  });

  return pool;
}

async function executeQuery(sql, params = [], options = {}) {
  let connection;
  try {
    connection = await pool.getConnection();
    const result = await connection.execute(sql, params, {
      outFormat: oracledb.OUT_FORMAT_OBJECT,
      ...options
    });
    return result;
  } finally {
    if (connection) {
      await connection.close();
    }
  }
}

async function closePool() {
  if (pool) {
    await pool.close(10);
    pool = null;
  }
}

module.exports = { initializePool, executeQuery, closePool, oracledb };
