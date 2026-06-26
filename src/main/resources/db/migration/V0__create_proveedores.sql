-- Tabla base requerida por recepciones (V17). En instalaciones existentes
-- puede haber sido creada previamente por Hibernate; IF NOT EXISTS mantiene
-- la migración compatible.
CREATE TABLE IF NOT EXISTS proveedores (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(100) NOT NULL,
  nombre VARCHAR(255) NOT NULL,
  nombre_comercial VARCHAR(255),
  cif VARCHAR(20),
  direccion VARCHAR(255),
  codigo_postal VARCHAR(10),
  poblacion VARCHAR(100),
  provincia VARCHAR(100),
  pais VARCHAR(100) DEFAULT 'España',
  telefono VARCHAR(20),
  telefono2 VARCHAR(20),
  fax VARCHAR(20),
  email VARCHAR(255),
  web VARCHAR(255),
  persona_contacto VARCHAR(255),
  iban VARCHAR(34),
  swift VARCHAR(11),
  dias_pago INT,
  forma_pago VARCHAR(50),
  descuento DECIMAL(5,2),
  limite_credito DECIMAL(10,2),
  activo BOOLEAN DEFAULT TRUE,
  notas TEXT,
  fecha_creacion DATETIME,
  fecha_modificacion DATETIME,
  fecha_ultima_compra DATETIME,
  total_compras DECIMAL(12,2) DEFAULT 0.00,
  total_facturas_pendientes DECIMAL(12,2) DEFAULT 0.00,
  CONSTRAINT uq_proveedores_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedidos_compra (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  numero VARCHAR(100) NOT NULL,
  fecha DATE NOT NULL,
  fecha_esperada_entrega DATE,
  proveedor_id BIGINT NOT NULL,
  total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  estado VARCHAR(20) DEFAULT 'BORRADOR',
  observaciones TEXT,
  fecha_creacion DATETIME,
  fecha_modificacion DATETIME,
  CONSTRAINT uq_pedidos_compra_numero UNIQUE (numero),
  CONSTRAINT fk_pedidos_compra_proveedor
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
