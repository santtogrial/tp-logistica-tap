-- Usuarios de prueba (contraseñas en el README)
INSERT INTO usuario (nombre, email, password_hash, rol) VALUES
    ('Administrador',  'admin@logistica.com',    '$2a$10$8yMtxDnf6igZayxDaHaQu.GRzzclu.BWfnyOa8MHgtRPZ8XhAxm56', 'ADMIN'),
    ('Laura Gómez',    'operador@logistica.com', '$2a$10$bJHhTjpurlnYv8trCZk4TOFN7Q1wTl/f1vKvLz6r.gXQFfN/I9Nhi', 'OPERADOR'),
    ('Martín Sosa',    'chofer1@logistica.com',  '$2a$10$7FHuw9a1mH8hMb5.YYibhuuBYXMqzduHdPoL0PJFv6z7Cv3M8jAea', 'CHOFER'),
    ('Diego Fernández','chofer2@logistica.com',  '$2a$10$7FHuw9a1mH8hMb5.YYibhuuBYXMqzduHdPoL0PJFv6z7Cv3M8jAea', 'CHOFER'),
    ('Pablo Ruiz',     'chofer3@logistica.com',  '$2a$10$7FHuw9a1mH8hMb5.YYibhuuBYXMqzduHdPoL0PJFv6z7Cv3M8jAea', 'CHOFER');

INSERT INTO deposito (nombre, direccion, lat, lng) VALUES
    ('Depósito Palermo',  'Av. Juan B. Justo 1500, CABA',  -34.5885, -58.4320),
    ('Depósito Barracas', 'Av. Montes de Oca 1000, CABA',  -34.6380, -58.3790);

INSERT INTO producto (sku, nombre) VALUES
    ('AGU-6X15', 'Agua mineral 6 x 1,5 L'),
    ('YER-1KG',  'Yerba mate 1 kg'),
    ('ACE-15L',  'Aceite de girasol 1,5 L'),
    ('DDL-1KG',  'Dulce de leche 1 kg'),
    ('HAR-1KG',  'Harina 000 1 kg'),
    ('DET-750',  'Detergente 750 ml');

-- El stock del depósito 1 ya descuenta las órdenes de ejemplo cargadas más abajo
INSERT INTO stock_deposito (deposito_id, producto_id, cantidad) VALUES
    (1, 1, 36), (1, 2, 47), (1, 3, 30), (1, 4, 22), (1, 5, 60), (1, 6, 3),
    (2, 1, 20), (2, 2, 30), (2, 3, 15), (2, 4, 10), (2, 5, 40), (2, 6, 25);

INSERT INTO destinatario (nombre, telefono, direccion, lat, lng) VALUES
    ('Almacén Don Luis',       '11 4551-2030', 'Av. Cabildo 2100, Belgrano',        -34.5600, -58.4560),
    ('Kiosco La Esquina',      '11 4701-8890', 'Av. del Libertador 7400, Núñez',    -34.5450, -58.4610),
    ('Despensa Colegiales',    '11 4553-1122', 'Av. Federico Lacroze 2500',         -34.5770, -58.4480),
    ('Mercadito Almagro',      '11 4862-4455', 'Av. Corrientes 4000, Almagro',      -34.6030, -58.4200),
    ('Autoservicio Caballito', '11 4903-7766', 'Av. Rivadavia 5100, Caballito',     -34.6180, -58.4370),
    ('Dietética Recoleta',     '11 4801-3344', 'Av. Santa Fe 2500, Recoleta',       -34.5960, -58.4020),
    ('Minimercado Urquiza',    '11 4521-9988', 'Av. Triunvirato 4500, V. Urquiza',  -34.5730, -58.4870),
    ('Almacén San Telmo',      '11 4300-5566', 'Defensa 1000, San Telmo',           -34.6200, -58.3720);

INSERT INTO vehiculo (patente, modelo, deposito_base_id, chofer_id, estado) VALUES
    ('AB123CD', 'Fiat Fiorino',     1, 3,    'DISPONIBLE'),
    ('AC456EF', 'Renault Kangoo',   1, 4,    'DISPONIBLE'),
    ('AD789GH', 'Peugeot Partner',  2, 5,    'DISPONIBLE'),
    ('AE012IJ', 'Fiat Fiorino',     1, NULL, 'EN_TALLER');

-- Órdenes de ejemplo listas para planificar desde el depósito Palermo
INSERT INTO orden (codigo_seguimiento, deposito_id, destinatario_id, estado, intentos_fallidos, creada_en) VALUES
    ('LG-7Q4K2M9X', 1, 1, 'CREADA', 0, NOW()),
    ('LG-3H8N5R2T', 1, 2, 'CREADA', 0, NOW()),
    ('LG-9B6W1P4Z', 1, 4, 'CREADA', 0, NOW()),
    ('LG-5D2J7F8C', 1, 5, 'CREADA', 0, NOW());

INSERT INTO item_orden (orden_id, producto_id, cantidad) VALUES
    (1, 1, 2), (1, 2, 1),
    (2, 4, 3),
    (3, 1, 2), (3, 5, 4),
    (4, 2, 2), (4, 3, 2);

INSERT INTO evento_orden (orden_id, usuario_id, estado_anterior, estado_nuevo, fecha, observacion) VALUES
    (1, 2, NULL, 'CREADA', NOW(), 'Orden creada'),
    (2, 2, NULL, 'CREADA', NOW(), 'Orden creada'),
    (3, 2, NULL, 'CREADA', NOW(), 'Orden creada'),
    (4, 2, NULL, 'CREADA', NOW(), 'Orden creada');
