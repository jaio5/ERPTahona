CREATE TABLE IF NOT EXISTS albaran_series (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  serie VARCHAR(20) NOT NULL,
  ejercicio INT NOT NULL,
  ultimo_numero BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_albaran_series_serie_ejercicio UNIQUE (serie, ejercicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Inicializa la secuencia a partir de documentos existentes para que una
-- actualización no vuelva a comenzar por 1.
INSERT INTO albaran_series (serie, ejercicio, ultimo_numero)
SELECT
  'ALB',
  CAST(SUBSTRING(numero, 5, 4) AS UNSIGNED),
  MAX(CAST(SUBSTRING(numero, 10) AS UNSIGNED))
FROM albaranes_venta
WHERE numero REGEXP '^ALB-[0-9]{4}-[0-9]{6}$'
GROUP BY CAST(SUBSTRING(numero, 5, 4) AS UNSIGNED)
ON DUPLICATE KEY UPDATE
  ultimo_numero = GREATEST(ultimo_numero, VALUES(ultimo_numero));
