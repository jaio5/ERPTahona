-- V15: Tarifas por cliente
CREATE TABLE IF NOT EXISTS tarifas_cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    articulo_id BIGINT NOT NULL,
    precio_especial DECIMAL(10,2),
    descuento DECIMAL(5,2),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME,
    UNIQUE KEY uk_tarifa_cliente_articulo (cliente_id, articulo_id),
    CONSTRAINT fk_tarifas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_tarifas_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
