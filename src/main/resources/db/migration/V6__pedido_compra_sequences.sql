CREATE TABLE IF NOT EXISTS pedido_compra_series (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  serie VARCHAR(20) NOT NULL,
  ejercicio INT NOT NULL,
  ultimo_numero BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_pedido_compra_series_serie_ejercicio UNIQUE (serie, ejercicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
