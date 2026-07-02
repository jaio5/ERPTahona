-- V17: Recepciones de mercancía
CREATE TABLE IF NOT EXISTS recepciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50),
    fecha DATE NOT NULL,
    proveedor_id BIGINT NOT NULL,
    almacen_id BIGINT,
    pedido_compra_id BIGINT,
    estado VARCHAR(30) DEFAULT 'PENDIENTE',
    observaciones VARCHAR(1000),
    fecha_creacion DATETIME,
    CONSTRAINT fk_recepcion_prov FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    CONSTRAINT fk_recepcion_alm FOREIGN KEY (almacen_id) REFERENCES almacenes(id),
    CONSTRAINT fk_recepcion_ped FOREIGN KEY (pedido_compra_id) REFERENCES pedidos_compra(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS recepcion_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recepcion_id BIGINT NOT NULL,
    articulo_id BIGINT NOT NULL,
    cantidad_recibida DECIMAL(10,2),
    cantidad_pedida DECIMAL(10,2),
    codigo_lote VARCHAR(100),
    fecha_caducidad DATE,
    precio_unitario DECIMAL(10,2),
    observaciones VARCHAR(500),
    CONSTRAINT fk_rec_linea_rec FOREIGN KEY (recepcion_id) REFERENCES recepciones(id),
    CONSTRAINT fk_rec_linea_art FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
