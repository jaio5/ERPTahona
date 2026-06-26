-- V16: Control de mermas
CREATE TABLE IF NOT EXISTS mermas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    articulo_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(200),
    tipo VARCHAR(50),
    responsable VARCHAR(100),
    observaciones VARCHAR(1000),
    fecha_creacion DATETIME,
    CONSTRAINT fk_mermas_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
