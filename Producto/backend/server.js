require('dotenv').config();
const { initializePool, closePool } = require('./src/config/database');
const app = require('./src/app');
const os = require('os');

const PORT = process.env.PORT || 3000;

function getLocalIp() {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  return 'localhost';
}

async function start() {
  try {
    console.log('Conectando a Oracle ADB...');
    await initializePool();
    console.log('[OK] Conexion a Oracle establecida');

    app.listen(PORT, '0.0.0.0', () => {
      const localIp = getLocalIp();
      console.log('\n=== MiEstacionamiento Backend ===');
      console.log(`Local:    http://localhost:${PORT}`);
      console.log(`Red WiFi: http://${localIp}:${PORT}`);
      console.log('\n--- Conexion desde telefono Android ---');
      console.log('Opcion 1 (USB + ADB):');
      console.log(`  Ejecutar: adb reverse tcp:${PORT} tcp:${PORT}`);
      console.log(`  URL en app: http://10.0.2.2:${PORT}/api/`);
      console.log('Opcion 2 (WiFi misma red):');
      console.log(`  URL en app: http://${localIp}:${PORT}/api/`);
      console.log('\nEndpoints disponibles:');
      console.log(`  POST /api/auth/login`);
      console.log(`  POST /api/auth/register`);
      console.log(`  GET  /api/parkings`);
      console.log(`  GET  /api/parkings/search?q=...`);
      console.log(`  GET  /api/health`);
    });
  } catch (error) {
    console.error('Error al iniciar el servidor:', error.message);
    if (error.message.includes('wallet')) {
      console.error('\nVerifica que los archivos del wallet esten en: backend/wallet/');
      console.error('Archivos necesarios: cwallet.sso, ewallet.p12, tnsnames.ora, sqlnet.ora');
    }
    process.exit(1);
  }
}

process.on('SIGINT', async () => {
  console.log('\nCerrando servidor...');
  await closePool();
  process.exit(0);
});

start();
