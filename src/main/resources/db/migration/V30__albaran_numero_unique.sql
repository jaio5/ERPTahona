-- V30: Restricción de unicidad en albaranes_venta.numero.
-- Evita duplicados bajo concurrencia (el check en Java tiene race condition).
--
-- Paso 1: redirigir referencias de albaranes duplicados al superviviente (MAX id por numero)
--         antes de eliminarlos, para no violar FK constraints sin CASCADE.
-- Paso 2: eliminar los albaranes duplicados.
-- Paso 3: eliminar cualquier índice único previo generado automáticamente por Hibernate.
-- Paso 4: añadir la constraint con nombre canónico (idempotente).

-- Paso 1a: actualizar hojas_ruta_entregas para apuntar al superviviente
UPDATE hojas_ruta_entregas hre
INNER JOIN albaranes_venta av_dup ON hre.albaran_id = av_dup.id
INNER JOIN (
    SELECT numero, MAX(id) AS max_id
    FROM albaranes_venta
    GROUP BY numero
    HAVING COUNT(*) > 1
) dups ON av_dup.numero = dups.numero AND av_dup.id < dups.max_id
SET hre.albaran_id = dups.max_id;

-- Paso 1b: actualizar devoluciones para apuntar al superviviente
UPDATE devoluciones d
INNER JOIN albaranes_venta av_dup ON d.albaran_id = av_dup.id
INNER JOIN (
    SELECT numero, MAX(id) AS max_id
    FROM albaranes_venta
    GROUP BY numero
    HAVING COUNT(*) > 1
) dups ON av_dup.numero = dups.numero AND av_dup.id < dups.max_id
SET d.albaran_id = dups.max_id;

-- Paso 1c: mover vínculos factura del duplicado al superviviente, luego eliminar los duplicados
INSERT IGNORE INTO albaranes_venta_facturas (albaranes_ventas_id, facturas_id)
SELECT dups.max_id, avf.facturas_id
FROM albaranes_venta_facturas avf
INNER JOIN albaranes_venta av_dup ON avf.albaranes_ventas_id = av_dup.id
INNER JOIN (
    SELECT numero, MAX(id) AS max_id
    FROM albaranes_venta
    GROUP BY numero
    HAVING COUNT(*) > 1
) dups ON av_dup.numero = dups.numero AND av_dup.id < dups.max_id;

DELETE avf FROM albaranes_venta_facturas avf
INNER JOIN albaranes_venta av_dup ON avf.albaranes_ventas_id = av_dup.id
INNER JOIN (
    SELECT numero, MAX(id) AS max_id
    FROM albaranes_venta
    GROUP BY numero
    HAVING COUNT(*) > 1
) dups ON av_dup.numero = dups.numero AND av_dup.id < dups.max_id;

-- Paso 2: eliminar los albaranes duplicados (ya sin referencias huérfanas)
DELETE a FROM albaranes_venta a
INNER JOIN (
    SELECT numero, MAX(id) AS max_id
    FROM albaranes_venta
    GROUP BY numero
    HAVING COUNT(*) > 1
) dups ON a.numero = dups.numero AND a.id < dups.max_id;

-- Paso 3: eliminar cualquier índice único previo generado automáticamente por Hibernate
-- (incluyendo uq_albaranes_venta_numero si ya existe, para hacerlo idempotente)
SET @drop_idx = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME   = 'albaranes_venta'
       AND COLUMN_NAME  = 'numero'
       AND NON_UNIQUE   = 0
       AND INDEX_NAME  != 'PRIMARY') > 0,
    (SELECT CONCAT('DROP INDEX `', INDEX_NAME, '` ON albaranes_venta')
     FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME   = 'albaranes_venta'
       AND COLUMN_NAME  = 'numero'
       AND NON_UNIQUE   = 0
       AND INDEX_NAME  != 'PRIMARY'
     LIMIT 1),
    'SELECT 1'
);
PREPARE _stmt FROM @drop_idx;
EXECUTE _stmt;
DEALLOCATE PREPARE _stmt;

-- Paso 4: añadir la constraint con nombre canónico
ALTER TABLE albaranes_venta
    ADD CONSTRAINT uq_albaranes_venta_numero UNIQUE (numero);
