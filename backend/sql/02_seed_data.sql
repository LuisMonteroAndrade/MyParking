-- =========================================================
-- MiEstacionamiento - Datos iniciales de estacionamientos
-- Ejecutar DESPUES de 01_create_tables.sql
-- =========================================================

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Parking Central Plaza',
        'Estacionamiento en el corazon de la ciudad con seguridad 24/7 y multiples servicios para tu comodidad.',
        'Av. Corrientes 1234, Buenos Aires',
        'https://images.unsplash.com/photo-1506521781263-d8422e82f27a?w=600',
        -34.6037, -58.3816, 350, 45, 120, 4.5, 238);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Garaje San Martin',
        'Amplio estacionamiento cubierto con servicio de valet parking y vigilancia permanente.',
        'Calle San Martin 567, Buenos Aires',
        'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600',
        -34.6118, -58.3960, 280, 12, 80, 4.2, 156);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Parking Puerto Madero',
        'Moderno estacionamiento frente al rio con facil acceso a Puerto Madero y restaurantes.',
        'Dique 4, Puerto Madero, Buenos Aires',
        'https://images.unsplash.com/photo-1573348722427-f1d6819fdf98?w=600',
        -34.6152, -58.3632, 450, 87, 200, 4.7, 412);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Cochera Palermo',
        'Estacionamiento seguro en el barrio de Palermo con vigilancia y camaras de seguridad.',
        'Av. Santa Fe 3600, Palermo, Buenos Aires',
        'https://images.unsplash.com/photo-1545127398-14699f92334b?w=600',
        -34.5913, -58.4130, 320, 5, 60, 4.0, 89);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Parking Recoleta Premium',
        'Estacionamiento premium en el exclusivo barrio de Recoleta, cerca de museos y galerias.',
        'Av. Alvear 1800, Recoleta, Buenos Aires',
        'https://images.unsplash.com/photo-1611293388250-580b08c4a145?w=600',
        -34.5877, -58.3927, 500, 30, 100, 4.8, 325);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Garaje Belgrano',
        'Cochera familiar con multiples servicios adicionales, ideal para estadias largas.',
        'Cabildo 2200, Belgrano, Buenos Aires',
        'https://images.unsplash.com/photo-1592838064575-70ed626d3a0e?w=600',
        -34.5598, -58.4572, 250, 22, 70, 4.3, 178);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Estacionamiento Microcentro',
        'Ubicado en el microcentro porteno, perfecto para visitas de negocios y tramites.',
        'Florida 800, Microcentro, Buenos Aires',
        'https://images.unsplash.com/photo-1470224114660-3f6686c562eb?w=600',
        -34.6043, -58.3741, 400, 18, 90, 4.1, 203);

INSERT INTO PARKINGS (NAME, DESCRIPTION, ADDRESS, IMAGE_URL, LATITUDE, LONGITUDE, PRICE_PER_HOUR, AVAILABLE_SPOTS, TOTAL_SPOTS, RATING, REVIEW_COUNT)
VALUES ('Cochera San Telmo',
        'Estacionamiento en el historico barrio de San Telmo, cerca de la feria y museos.',
        'Defensa 1100, San Telmo, Buenos Aires',
        'https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=600',
        -34.6218, -58.3700, 300, 33, 75, 4.4, 142);

COMMIT;
