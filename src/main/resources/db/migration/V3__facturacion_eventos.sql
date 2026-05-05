CREATE TABLE IF NOT EXISTS facturacion_eventos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fecha DATETIME NOT NULL,
  ambito VARCHAR(50) NOT NULL,
  tipo_evento VARCHAR(50) NOT NULL,
  referencia VARCHAR(100),
  hash_anterior VARCHAR(128),
  hash_actual VARCHAR(128) NOT NULL,
  payload_hash VARCHAR(128),
  metadata JSON,
  INDEX idx_fact_eventos_fecha (fecha),
  INDEX idx_fact_eventos_ambito (ambito),
  INDEX idx_fact_eventos_tipo (tipo_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
