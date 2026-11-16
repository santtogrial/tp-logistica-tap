CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol           VARCHAR(20)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE deposito (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(100)     NOT NULL,
    direccion VARCHAR(200)     NOT NULL,
    lat       DOUBLE PRECISION NOT NULL,
    lng       DOUBLE PRECISION NOT NULL
);

CREATE TABLE producto (
    id     BIGSERIAL PRIMARY KEY,
    sku    VARCHAR(30)  NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL
);

CREATE TABLE stock_deposito (
    id          BIGSERIAL PRIMARY KEY,
    deposito_id BIGINT  NOT NULL REFERENCES deposito (id),
    producto_id BIGINT  NOT NULL REFERENCES producto (id),
    cantidad    INTEGER NOT NULL CHECK (cantidad >= 0),
    version     INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_deposito_producto UNIQUE (deposito_id, producto_id)
);

CREATE TABLE destinatario (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(100)     NOT NULL,
    telefono  VARCHAR(30),
    direccion VARCHAR(200)     NOT NULL,
    lat       DOUBLE PRECISION NOT NULL,
    lng       DOUBLE PRECISION NOT NULL
);

CREATE TABLE orden (
    id                 BIGSERIAL PRIMARY KEY,
    codigo_seguimiento VARCHAR(20) NOT NULL UNIQUE,
    deposito_id        BIGINT      NOT NULL REFERENCES deposito (id),
    destinatario_id    BIGINT      NOT NULL REFERENCES destinatario (id),
    estado             VARCHAR(20) NOT NULL,
    intentos_fallidos  INTEGER     NOT NULL DEFAULT 0,
    creada_en          TIMESTAMP   NOT NULL
);

CREATE INDEX idx_orden_estado_deposito ON orden (estado, deposito_id);

CREATE TABLE item_orden (
    id          BIGSERIAL PRIMARY KEY,
    orden_id    BIGINT  NOT NULL REFERENCES orden (id),
    producto_id BIGINT  NOT NULL REFERENCES producto (id),
    cantidad    INTEGER NOT NULL CHECK (cantidad > 0)
);

CREATE TABLE evento_orden (
    id              BIGSERIAL PRIMARY KEY,
    orden_id        BIGINT      NOT NULL REFERENCES orden (id),
    usuario_id      BIGINT REFERENCES usuario (id),
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20) NOT NULL,
    fecha           TIMESTAMP   NOT NULL,
    observacion     VARCHAR(255)
);

CREATE TABLE vehiculo (
    id               BIGSERIAL PRIMARY KEY,
    patente          VARCHAR(10) NOT NULL UNIQUE,
    modelo           VARCHAR(60) NOT NULL,
    deposito_base_id BIGINT      NOT NULL REFERENCES deposito (id),
    chofer_id        BIGINT UNIQUE REFERENCES usuario (id),
    estado           VARCHAR(20) NOT NULL
);

CREATE TABLE planificacion (
    id          BIGSERIAL PRIMARY KEY,
    deposito_id BIGINT      NOT NULL REFERENCES deposito (id),
    estado      VARCHAR(20) NOT NULL,
    creada_en   TIMESTAMP   NOT NULL
);

CREATE TABLE ruta (
    id               BIGSERIAL PRIMARY KEY,
    planificacion_id BIGINT           NOT NULL REFERENCES planificacion (id),
    vehiculo_id      BIGINT           NOT NULL REFERENCES vehiculo (id),
    estado           VARCHAR(20)      NOT NULL,
    distancia_km     DOUBLE PRECISION NOT NULL,
    duracion_min     INTEGER          NOT NULL,
    iniciada_en      TIMESTAMP,
    finalizada_en    TIMESTAMP
);

CREATE INDEX idx_ruta_vehiculo_estado ON ruta (vehiculo_id, estado);

CREATE TABLE parada (
    id           BIGSERIAL PRIMARY KEY,
    ruta_id      BIGINT  NOT NULL REFERENCES ruta (id),
    orden_id     BIGINT  NOT NULL REFERENCES orden (id),
    secuencia    INTEGER NOT NULL,
    eta_min      INTEGER NOT NULL,
    llegada_real TIMESTAMP,
    resultado    VARCHAR(20),
    motivo       VARCHAR(255),
    CONSTRAINT uk_parada_ruta_secuencia UNIQUE (ruta_id, secuencia)
);
