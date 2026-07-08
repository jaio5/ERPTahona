-- V47: amplía factura_lineas.descuento de DECIMAL(5,2) a DECIMAL(10,2). Desde V44 el descuento
-- de línea puede ser un importe fijo (descuento_tipo = IMPORTE); con DECIMAL(5,2) (máx 999,99)
-- un descuento en euros mayor desbordaba. Se alinea con albaran_venta_lineas.descuento.

ALTER TABLE factura_lineas MODIFY COLUMN descuento DECIMAL(10,2) NULL;
