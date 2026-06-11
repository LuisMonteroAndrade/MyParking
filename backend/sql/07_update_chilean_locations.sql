-- =========================================================
-- MyParking - Actualización a ubicaciones chilenas reales
-- Ejecutar en SQL Worksheet de Oracle Cloud
-- Reemplaza los datos de Buenos Aires por Santiago de Chile
-- =========================================================

UPDATE PARKINGS SET
  NAME = 'Estacionamiento San Borja',
  DESCRIPTION = 'Estacionamiento subterráneo en el corazón de Santiago, acceso directo al metro y a la Alameda. Seguridad 24/7.',
  ADDRESS = 'Av. Libertador B. O''Higgins 3322, Santiago Centro',
  LATITUDE = -33.4475,
  LONGITUDE = -70.6527,
  PRICE_PER_HOUR = 1200
WHERE NAME = 'Parking Central Plaza';

UPDATE PARKINGS SET
  NAME = 'Cochera Providencia',
  DESCRIPTION = 'Amplio estacionamiento cubierto con servicio de valet y vigilancia permanente en el corazón de Providencia.',
  ADDRESS = 'Av. Providencia 1234, Providencia',
  LATITUDE = -33.4312,
  LONGITUDE = -70.6126,
  PRICE_PER_HOUR = 1500
WHERE NAME = 'Garaje San Martin';

UPDATE PARKINGS SET
  NAME = 'Parking Costanera Center',
  DESCRIPTION = 'Moderno estacionamiento en el mall más grande de Chile, con fácil acceso y múltiples niveles cubiertos.',
  ADDRESS = 'Av. Andrés Bello 2425, Providencia',
  LATITUDE = -33.4177,
  LONGITUDE = -70.6065,
  PRICE_PER_HOUR = 1800
WHERE NAME = 'Parking Puerto Madero';

UPDATE PARKINGS SET
  NAME = 'Cochera Bellavista',
  DESCRIPTION = 'Estacionamiento seguro en el barrio Bellavista, ideal para visitar restaurantes, cerro San Cristóbal y vida nocturna.',
  ADDRESS = 'Pío Nono 340, Bellavista, Santiago',
  LATITUDE = -33.4278,
  LONGITUDE = -70.6389,
  PRICE_PER_HOUR = 1000
WHERE NAME = 'Cochera Palermo';

UPDATE PARKINGS SET
  NAME = 'Parking Vitacura Premium',
  DESCRIPTION = 'Estacionamiento premium en el exclusivo barrio de Vitacura, cerca de galerías de arte y restaurantes de lujo.',
  ADDRESS = 'Av. Vitacura 2939, Vitacura',
  LATITUDE = -33.3982,
  LONGITUDE = -70.5988,
  PRICE_PER_HOUR = 2500
WHERE NAME = 'Parking Recoleta Premium';

UPDATE PARKINGS SET
  NAME = 'Garaje Ñuñoa',
  DESCRIPTION = 'Cochera familiar cerca de la Plaza Ñuñoa con múltiples servicios, ideal para estadías largas y eventos.',
  ADDRESS = 'Irarrázaval 3520, Ñuñoa',
  LATITUDE = -33.4568,
  LONGITUDE = -70.6041,
  PRICE_PER_HOUR = 1000
WHERE NAME = 'Garaje Belgrano';

UPDATE PARKINGS SET
  NAME = 'Estacionamiento El Golf',
  DESCRIPTION = 'Estacionamiento ejecutivo en el barrio financiero de Las Condes, perfecto para reuniones de negocios.',
  ADDRESS = 'Isidora Goyenechea 2920, Las Condes',
  LATITUDE = -33.4134,
  LONGITUDE = -70.5937,
  PRICE_PER_HOUR = 2000
WHERE NAME = 'Estacionamiento Microcentro';

UPDATE PARKINGS SET
  NAME = 'Cochera Lastarria',
  DESCRIPTION = 'Estacionamiento en el histórico barrio Lastarria, cerca de museos, galerías y la mejor vida cultural de Santiago.',
  ADDRESS = 'Merced 360, Lastarria, Santiago',
  LATITUDE = -33.4378,
  LONGITUDE = -70.6395,
  PRICE_PER_HOUR = 1200
WHERE NAME = 'Cochera San Telmo';

COMMIT;
