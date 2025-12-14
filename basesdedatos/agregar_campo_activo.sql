-- Script para agregar el campo 'activo' a las tablas del ERP
-- Esto permite implementar baja lógica en lugar de eliminación física

USE tahona;

-- Agregar campo activo a la tabla almacenes
ALTER TABLE almacenes
ADD COLUMN IF NOT EXISTS activo TINYINT(1) DEFAULT 1 COMMENT 'Indica si el almacén está activo (1) o dado de baja (0)';

-- Actualizar registros existentes para que estén activos por defecto
UPDATE almacenes SET activo = 1 WHERE activo IS NULL;

-- Agregar campo activo a la tabla clientes
ALTER TABLE clientes
ADD COLUMN IF NOT EXISTS activo TINYINT(1) DEFAULT 1 COMMENT 'Indica si el cliente está activo (1) o dado de baja (0)';

-- Actualizar registros existentes para que estén activos por defecto
UPDATE clientes SET activo = 1 WHERE activo IS NULL;

-- Agregar campo activo a la tabla articulos
ALTER TABLE articulos
ADD COLUMN IF NOT EXISTS activo TINYINT(1) DEFAULT 1 COMMENT 'Indica si el artículo está activo (1) o dado de baja (0)';

-- Actualizar registros existentes para que estén activos por defecto
UPDATE articulos SET activo = 1 WHERE activo IS NULL;

-- Agregar campo activo a la tabla proveedores
ALTER TABLE proveedores
ADD COLUMN IF NOT EXISTS activo TINYINT(1) DEFAULT 1 COMMENT 'Indica si el proveedor está activo (1) o dado de baja (0)';

-- Actualizar registros existentes para que estén activos por defecto
UPDATE proveedores SET activo = 1 WHERE activo IS NULL;

-- Verificar los cambios
SELECT 'almacenes' as tabla, COUNT(*) as total, SUM(activo) as activos FROM almacenes
UNION ALL
SELECT 'clientes' as tabla, COUNT(*) as total, SUM(activo) as activos FROM clientes
UNION ALL
SELECT 'articulos' as tabla, COUNT(*) as total, SUM(activo) as activos FROM articulos
UNION ALL
SELECT 'proveedores' as tabla, COUNT(*) as total, SUM(activo) as activos FROM proveedores;

