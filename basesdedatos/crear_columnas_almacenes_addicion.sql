-- Script de migración: añadir columnas para campos extra del formulario de almacén
-- Backup recomendado antes de ejecutar:
-- mysqldump -u <usuario> -p <basedatos> almacenes > backups/almacenes_pre_migration.sql

ALTER TABLE almacenes
  ADD COLUMN descripcion VARCHAR(1000) NULL,
  ADD COLUMN capacidad DECIMAL(10,2) NULL,
  ADD COLUMN disponible DECIMAL(10,2) NULL,
  ADD COLUMN localidad VARCHAR(255) NULL,
  ADD COLUMN responsable VARCHAR(255) NULL;

-- Opcional: inicializar 'disponible' con 'capacidad' si quieres
-- UPDATE almacenes SET disponible = capacidad WHERE disponible IS NULL AND capacidad IS NOT NULL;

-- Rollback (si necesitas revertir):
-- ALTER TABLE almacenes
--   DROP COLUMN descripcion,
--   DROP COLUMN capacidad,
--   DROP COLUMN disponible,
--   DROP COLUMN localidad,
--   DROP COLUMN responsable;

