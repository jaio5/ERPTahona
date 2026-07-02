-- V18: Fianzas de clientes
CREATE TABLE IF NOT EXISTS fianzas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    importe DECIMAL(10,2),
    fecha_constitucion DATE,
    fecha_devolucion DATE,
    estado VARCHAR(30) DEFAULT 'ACTIVA',
    concepto VARCHAR(500),
    observaciones VARCHAR(1000),
    fecha_creacion DATETIME,
    CONSTRAINT fk_fianza_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
