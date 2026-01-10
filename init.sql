
-- 1. CREACIÓN DE TIPOS ENUM (Enumerados Nativos de Postgres)
-- Esto garantiza integridad de datos nivel Senior
CREATE TYPE estado_venta AS ENUM ('EN_PROCESO', 'FINALIZADA', 'CANCELADA');
CREATE TYPE estado_cuota AS ENUM ('PENDIENTE', 'PAGADA', 'ATRASADA', 'VENCIDA');
CREATE TYPE estado_pedido AS ENUM ('PENDIENTE_COMPRA', 'COMPRADO', 'ENTREGADO');

-- 2. CREACIÓN DE TABLAS

-- Tabla de Clientes
CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE,
    direccion VARCHAR(200),
    telefono VARCHAR(50),
    localidad VARCHAR(100),
    intereses_personales TEXT,
    fecha_alta DATE DEFAULT CURRENT_DATE
);

-- Tabla de Familiares
CREATE TABLE familiares (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES clientes(id),
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    anio_nacimiento INT, 
    relacion VARCHAR(50), -- Podríamos hacer enum, pero a veces es muy variado (Tío, Abuelo, Padrino)
    intereses TEXT 
);

-- Tabla de Libros
CREATE TABLE libros (
    id SERIAL PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100),
    tematica VARCHAR(100),
    cant_volumenes INT DEFAULT 1,
    precio_base DECIMAL(10, 2),
    stock INT DEFAULT 0
);

-- Tabla de Ventas
CREATE TABLE ventas (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES clientes(id),
    fecha_venta DATE DEFAULT CURRENT_DATE,
    nro_factura VARCHAR(50) UNIQUE,
    monto_total DECIMAL(10, 2) NOT NULL,
    cantidad_cuotas INT DEFAULT 1,
    estado estado_venta DEFAULT 'EN_PROCESO' -- <--- USAMOS EL ENUM AQUÍ
);

-- Detalle de Venta
CREATE TABLE detalle_ventas (
    id SERIAL PRIMARY KEY,
    venta_id INT REFERENCES ventas(id),
    libro_id INT REFERENCES libros(id),
    precio_al_momento DECIMAL(10, 2)
);

-- Tabla de Cuotas
CREATE TABLE cuotas (
    id SERIAL PRIMARY KEY,
    venta_id INT REFERENCES ventas(id),
    numero_cuota INT NOT NULL,
    monto_cuota DECIMAL(10, 2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago_real DATE,
    nro_recibo_pago VARCHAR(50),
    estado estado_cuota DEFAULT 'PENDIENTE' -- <--- USAMOS EL ENUM AQUÍ
);

-- Pedidos Especiales
CREATE TABLE pedidos_especiales (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES clientes(id),
    descripcion VARCHAR(200),
    estado estado_pedido DEFAULT 'PENDIENTE_COMPRA' -- <--- USAMOS EL ENUM AQUÍ
);