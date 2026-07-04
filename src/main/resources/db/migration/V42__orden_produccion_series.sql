-- Numeración de órdenes de producción por secuencia con lock pesimista
-- (mismo patrón que factura_series/albaran_series), en lugar del MAX(numero)
-- bajo synchronized, que solo era correcto con una única instancia de la app.

CREATE TABLE IF NOT EXISTS orden_produccion_series (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  serie VARCHAR(20) NOT NULL,
  ejercicio INT NOT NULL,
  ultimo_numero BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_orden_produccion_series_serie_ejercicio UNIQUE (serie, ejercicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Inicializa la secuencia a partir de las órdenes existentes (numero OP-YYYY-NNNN)
-- para que una actualización no vuelva a comenzar por 1.
INSERT INTO orden_produccion_series (serie, ejercicio, ultimo_numero)
SELECT
  'OP',
  CAST(SUBSTRING(numero, 4, 4) AS UNSIGNED),
  MAX(CAST(SUBSTRING(numero, 9) AS UNSIGNED))
FROM ordenes_produccion
WHERE numero REGEXP '^OP-[0-9]{4}-[0-9]+$'
GROUP BY CAST(SUBSTRING(numero, 4, 4) AS UNSIGNED)
ON DUPLICATE KEY UPDATE
  ultimo_numero = GREATEST(ultimo_numero, VALUES(ultimo_numero));
