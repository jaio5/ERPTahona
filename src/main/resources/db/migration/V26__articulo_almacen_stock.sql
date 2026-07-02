-- Stock desglosado por almacén
CREATE TABLE IF NOT EXISTS articulo_almacen (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    articulo_id BIGINT NOT NULL,
    almacen_id  BIGINT NOT NULL,
    stock       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    UNIQUE KEY uq_articulo_almacen (articulo_id, almacen_id),
    CONSTRAINT fk_aa_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    CONSTRAINT fk_aa_almacen  FOREIGN KEY (almacen_id)  REFERENCES almacenes(id)
);

-- Migrar stock existente al primer almacén (si existe)
INSERT INTO articulo_almacen (articulo_id, almacen_id, stock)
SELECT a.id, al.id, COALESCE(a.stock, 0)
FROM articulos a
CROSS JOIN (SELECT id FROM almacenes ORDER BY id LIMIT 1) al
WHERE a.stock IS NOT NULL AND a.stock > 0
  AND EXISTS (SELECT 1 FROM almacenes)
ON DUPLICATE KEY UPDATE stock = VALUES(stock);
