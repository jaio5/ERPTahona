-- V24: Tablas para presupuestos de venta y sus líneas

CREATE TABLE IF NOT EXISTS presupuestos (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    version        BIGINT NOT NULL DEFAULT 0,
    numero         VARCHAR(100) NOT NULL,
    fecha          DATE NOT NULL,
    fecha_validez  DATE,
    cliente_id     BIGINT,
    total          DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado         VARCHAR(20) DEFAULT 'BORRADOR',
    factura_id     BIGINT,
    observaciones  LONGTEXT,
    fecha_creacion     DATETIME,
    fecha_modificacion DATETIME,
    usuario_creacion   VARCHAR(255),
    CONSTRAINT uq_presupuestos_numero UNIQUE (numero),
    CONSTRAINT fk_presupuestos_cliente  FOREIGN KEY (cliente_id)  REFERENCES clientes(id),
    CONSTRAINT fk_presupuestos_factura  FOREIGN KEY (factura_id)  REFERENCES facturas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS presupuestos_lineas (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    presupuesto_id  BIGINT NOT NULL,
    articulo_id     BIGINT,
    descripcion     VARCHAR(500),
    cantidad        DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,4) NOT NULL,
    descuento       DECIMAL(5,2),
    tipo_iva        DECIMAL(5,2),
    importe         DECIMAL(12,2) NOT NULL,
    orden           INT,
    CONSTRAINT fk_presupuesto_lineas_pres FOREIGN KEY (presupuesto_id) REFERENCES presupuestos(id) ON DELETE CASCADE,
    CONSTRAINT fk_presupuesto_lineas_art  FOREIGN KEY (articulo_id)    REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
