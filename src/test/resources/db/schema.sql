CREATE TABLE propietarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tarifas (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL
        CONSTRAINT chk_tarifa_tipo
        CHECK (tipo IN ('DIURNA', 'NOCTURNA')),
    bajada_bandera DECIMAL(10, 2) NOT NULL,
    valor_ficha DECIMAL(10, 2) NOT NULL,
    fecha_desde DATE NOT NULL,
    fecha_hasta DATE
);

CREATE TABLE feriados (
    id BIGSERIAL PRIMARY KEY,
    fecha DATE NOT NULL UNIQUE,
    descripcion VARCHAR(100)
);

CREATE TABLE vehiculos (
    id BIGSERIAL PRIMARY KEY,
    patente VARCHAR(10) NOT NULL UNIQUE,
    licencia VARCHAR(20) NOT NULL UNIQUE,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    anio INTEGER NOT NULL,
    nro_reloj_fullmar VARCHAR(50) NOT NULL UNIQUE,
    propietario_id BIGINT,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vehiculo_propietario
        FOREIGN KEY (propietario_id)
        REFERENCES propietarios(id)
        ON DELETE SET NULL
);

CREATE TABLE choferes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    telefono_contacto VARCHAR(20) NOT NULL UNIQUE,
    habilitado_amt BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'DESCONECTADO'
        CONSTRAINT chk_chofer_estado
        CHECK (estado IN ('LIBRE', 'OCUPADO', 'DESCONECTADO')),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE asignaciones_chofer_vehiculo (
    id BIGSERIAL PRIMARY KEY,
    chofer_id BIGINT NOT NULL,
    vehiculo_id BIGINT NOT NULL,
    fecha_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_fin TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_asignacion_chofer
        FOREIGN KEY (chofer_id)
        REFERENCES choferes(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_asignacion_vehiculo
        FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos(id)
        ON DELETE CASCADE
);

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    whatsapp_id VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100),
    fecha_registro TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE viajes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    chofer_id BIGINT,

    direccion_origen VARCHAR(255) NOT NULL,
    latitud_origen DOUBLE PRECISION NOT NULL,
    longitud_origen DOUBLE PRECISION NOT NULL,

    direccion_destino VARCHAR(255) NOT NULL,
    latitud_destino DOUBLE PRECISION NOT NULL,
    longitud_destino DOUBLE PRECISION NOT NULL,

    estado VARCHAR(20) NOT NULL DEFAULT 'SOLICITADO'
        CONSTRAINT chk_viaje_estado
        CHECK (estado IN (
            'SOLICITADO',
            'ACEPTADO',
            'EN_CURSO',
            'FINALIZADO',
            'CANCELADO'
        )),

    tipo_tarifa VARCHAR(20) NOT NULL
        CONSTRAINT chk_viaje_tipo_tarifa
        CHECK (tipo_tarifa IN ('DIURNA', 'NOCTURNA')),

    costo_estimado DECIMAL(10, 2),
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_viaje_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_viaje_chofer
        FOREIGN KEY (chofer_id)
        REFERENCES choferes(id)
        ON DELETE SET NULL
);

CREATE TABLE ofertas_viaje (
    id BIGSERIAL PRIMARY KEY,
    viaje_id BIGINT NOT NULL,
    chofer_id BIGINT NOT NULL,

    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
        CONSTRAINT chk_oferta_estado
        CHECK (estado IN (
            'PENDIENTE',
            'ACEPTADA',
            'RECHAZADA',
            'VENCIDA',
            'CANCELADA'
        )),

    fecha_envio TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_expiracion TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_oferta_viaje
        FOREIGN KEY (viaje_id)
        REFERENCES viajes(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_oferta_chofer
        FOREIGN KEY (chofer_id)
        REFERENCES choferes(id)
        ON DELETE CASCADE
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    nombre_completo VARCHAR(120),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,

    PRIMARY KEY (usuario_id, rol_id),

    CONSTRAINT fk_user_role_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_role_rol
        FOREIGN KEY (rol_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_viajes_estado
    ON viajes(estado);

CREATE INDEX idx_viajes_fecha
    ON viajes(fecha_creacion);