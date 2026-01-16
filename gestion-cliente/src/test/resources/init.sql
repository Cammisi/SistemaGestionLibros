

-- 2. TABLAS (Usando BIGSERIAL para coincidir con Java Long)

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(250),
    telefono VARCHAR(50),
    localidad VARCHAR(100),
    intereses_personales TEXT,
    fecha_alta DATE DEFAULT CURRENT_DATE
    );

CREATE TABLE familiares (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    anio_nacimiento INT,
    relacion VARCHAR(50),
    intereses TEXT,
	cliente_id BIGINT,
	CONSTRAINT fk_familiares_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
    );

CREATE TABLE libros (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100),
    tematica VARCHAR(100),
    cant_volumenes INT DEFAULT 1,
    precio_base DECIMAL(10, 2),
    stock INT DEFAULT 0
    );

CREATE TABLE ventas (
    id BIGSERIAL PRIMARY KEY,
    fecha_venta DATE DEFAULT CURRENT_DATE,
    nro_factura VARCHAR(50) NOT NULL UNIQUE,
    monto_total DECIMAL(10, 2) NOT NULL,
    cantidad_cuotas INT DEFAULT 1,
    estado VARCHAR(20),
	cliente_id BIGINT, 
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
    );

CREATE TABLE detalle_ventas (
	id BIGSERIAL PRIMARY KEY,
    cantidad INT DEFAULT 1,
    precio_al_momento DECIMAL(10, 2),
    venta_id BIGINT,
    libro_id BIGINT,
    CONSTRAINT fk_detalle_venta FOREIGN KEY (venta_id) REFERENCES ventas(id),
    CONSTRAINT fk_detalle_libro FOREIGN KEY (libro_id) REFERENCES libros(id)
    );

CREATE TABLE cuotas (
    id BIGSERIAL PRIMARY KEY,
    numero_cuota INT NOT NULL,
    monto_cuota DECIMAL(10, 2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago_real DATE,
    nro_recibo_pago VARCHAR(50),
    estado VARCHAR(20),
    venta_id BIGINT,
    CONSTRAINT fk_cuota_venta FOREIGN KEY (venta_id) REFERENCES ventas(id)
    );

CREATE TABLE pedidos_especiales (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(200),
    estado VARCHAR(20),
	cliente_id BIGINT,
	CONSTRAINT fk_pedidos_especiales_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
    );

INSERT INTO clientes (nombre, apellido, dni, fecha_alta) 
VALUES ('Juan', 'Perez', '12345678', CURRENT_DATE);

INSERT INTO libros (isbn, titulo, precio_base, stock) 
VALUES ('978-1', 'El Señor de los Anillos', 5000.00, 10);

INSERT INTO libros (isbn, titulo, precio_base, stock) 
VALUES ('978-2', 'Clean Code', 12000.50, 5);
