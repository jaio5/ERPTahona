-- V25: Tablas para pedidos de venta y sus líneas

CREATE TABLE IF NOT EXISTS pedidos (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero        VARCHAR(50) NOT NULL,
    fecha         DATE,
    cliente_id    BIGINT,
    estado        VARCHAR(50),
    total         DECIMAL(12,2),
    observaciones VARCHAR(500),
    CONSTRAINT uq_pedidos_numero UNIQUE (numero),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedido_lineas (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id    BIGINT,
    articulo_id  BIGINT,
    descripcion  VARCHAR(255),
    cantidad     DECIMAL(10,2),
    precio       DECIMAL(10,2),
    descuento    DECIMAL(5,2) DEFAULT 0.00,
    iva          DECIMAL(5,2) DEFAULT 0.00,
    CONSTRAINT fk_pedido_lineas_pedido  FOREIGN KEY (pedido_id)   REFERENCES pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_lineas_art     FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
