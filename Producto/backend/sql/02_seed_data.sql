-- =========================================================
-- MyParking - Datos iniciales de estacionamientos en Chile
-- Ejecutar DESPUES de 01_create_tables.sql
-- Coordenadas reales de Santiago de Chile
-- =========================================================

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Estacionamiento San Borja',
        'Estacionamiento subterráneo en el corazón de Santiago, acceso directo al metro y a la Alameda. Seguridad 24/7.',
        'Av. Libertador B. O''Higgins 3322, Santiago Centro',
        'https://images.unsplash.com/photo-1506521781263-d8422e82f27a?w=600',
        -33.4475, -70.6527, 1200, 45, 120, 4.5, 238);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Cochera Providencia',
        'Amplio estacionamiento cubierto con servicio de valet y vigilancia permanente en el corazón de Providencia.',
        'Av. Providencia 1234, Providencia',
        'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600',
        -33.4312, -70.6126, 1500, 12, 80, 4.2, 156);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Parking Costanera Center',
        'Moderno estacionamiento en el mall más grande de Chile, con fácil acceso y múltiples niveles cubiertos.',
        'Av. Andrés Bello 2425, Providencia',
        'https://images.unsplash.com/photo-1573348722427-f1d6819fdf98?w=600',
        -33.4177, -70.6065, 1800, 87, 200, 4.7, 412);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Cochera Bellavista',
        'Estacionamiento seguro en el barrio Bellavista, ideal para visitar restaurantes, cerro San Cristóbal y vida nocturna.',
        'Pío Nono 340, Bellavista, Santiago',
        'https://images.unsplash.com/photo-1545127398-14699f92334b?w=600',
        -33.4278, -70.6389, 1000, 5, 60, 4.0, 89);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Parking Vitacura Premium',
        'Estacionamiento premium en el exclusivo barrio de Vitacura, cerca de galerías de arte y restaurantes de lujo.',
        'Av. Vitacura 2939, Vitacura',
        'https://images.unsplash.com/photo-1611293388250-580b08c4a145?w=600',
        -33.3982, -70.5988, 2500, 30, 100, 4.8, 325);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Garaje Ñuñoa',
        'Cochera familiar cerca de la Plaza Ñuñoa con múltiples servicios, ideal para estadías largas y eventos.',
        'Irarrázaval 3520, Ñuñoa',
        'https://images.unsplash.com/photo-1592838064575-70ed626d3a0e?w=600',
        -33.4568, -70.6041, 1000, 22, 70, 4.3, 178);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Estacionamiento El Golf',
        'Estacionamiento ejecutivo en el barrio financiero de Las Condes, perfecto para reuniones de negocios.',
        'Isidora Goyenechea 2920, Las Condes',
        'https://images.unsplash.com/photo-1470224114660-3f6686c562eb?w=600',
        -33.4134, -70.5937, 2000, 18, 90, 4.1, 203);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Cochera Lastarria',
        'Estacionamiento en el histórico barrio Lastarria, cerca de museos, galerías y la mejor vida cultural de Santiago.',
        'Merced 360, Lastarria, Santiago',
        'https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=600',
        -33.4378, -70.6395, 1200, 33, 75, 4.4, 142);

COMMIT;
