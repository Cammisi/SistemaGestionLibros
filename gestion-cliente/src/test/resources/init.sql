-- 1. TIPOS ENUM (Vitales para que no falle Hibernate)
DROP TYPE IF EXISTS estado_venta CASCADE;
DROP TYPE IF EXISTS estado_cuota CASCADE;
DROP TYPE IF EXISTS estado_pedido CASCADE;

CREATE TYPE estado_venta AS ENUM ('EN_PROCESO', 'FINALIZADA', 'CANCELADA');
CREATE TYPE estado_cuota AS ENUM ('PENDIENTE', 'PAGADA', 'ATRASADA', 'VENCIDA');
CREATE TYPE estado_pedido AS ENUM ('PENDIENTE_COMPRA', 'COMPRADO', 'ENTREGADO');

-- 2. TABLAS (Usando BIGSERIAL para coincidir con Java Long)

CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE,
    direccion VARCHAR(200),
    telefono VARCHAR(50),
    localidad VARCHAR(100),
    intereses_personales TEXT,
    fecha_alta DATE DEFAULT CURRENT_DATE
    );

CREATE TABLE IF NOT EXISTS familiares (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT REFERENCES clientes(id),
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    anio_nacimiento INT,
    relacion VARCHAR(50),
    intereses TEXT
    );

CREATE TABLE IF NOT EXISTS libros (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100),
    tematica VARCHAR(100),
    cant_volumenes INT DEFAULT 1,
    precio_base DECIMAL(10, 2),
    stock INT DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS ventas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT REFERENCES clientes(id),
    fecha_venta DATE DEFAULT CURRENT_DATE,
    nro_factura VARCHAR(50) UNIQUE,
    monto_total DECIMAL(10, 2) NOT NULL,
    cantidad_cuotas INT DEFAULT 1,
    estado estado_venta DEFAULT 'EN_PROCESO'
    );

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT REFERENCES ventas(id),
    libro_id BIGINT REFERENCES libros(id),
    precio_al_momento DECIMAL(10, 2),
    cantidad INT DEFAULT 1
    );

CREATE TABLE IF NOT EXISTS cuotas (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT REFERENCES ventas(id),
    numero_cuota INT NOT NULL,
    monto_cuota DECIMAL(10, 2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago_real DATE,
    nro_recibo_pago VARCHAR(50),
    estado estado_cuota DEFAULT 'PENDIENTE'
    );

CREATE TABLE IF NOT EXISTS pedidos_especiales (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT REFERENCES clientes(id),
    descripcion VARCHAR(200),
    estado estado_pedido DEFAULT 'PENDIENTE_COMPRA'
    );