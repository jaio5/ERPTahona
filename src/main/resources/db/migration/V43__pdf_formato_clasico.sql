-- V43: campos para el formato clásico de factura y albarán (modelo "Tahona de Fernando").
--   * clientes.representante         -> casilla "Repres." de la factura
--   * empresa_config.whatsapp        -> cabecera del albarán (Tel. + Whatsapp)
--   * albaranes_venta.numero_lote    -> "ARTICULOS CON Nº LOTE" del albarán
--   * facturas.rappel_porcentaje/importe -> columnas %RAPPEL / Importe RAPPEL del pie

ALTER TABLE clientes        ADD COLUMN representante VARCHAR(100) NULL;
ALTER TABLE empresa_config  ADD COLUMN whatsapp VARCHAR(20) NULL;
ALTER TABLE albaranes_venta ADD COLUMN numero_lote VARCHAR(50) NULL;

ALTER TABLE facturas ADD COLUMN rappel_porcentaje DECIMAL(5,2) NULL DEFAULT 0.00;
ALTER TABLE facturas ADD COLUMN rappel_importe DECIMAL(10,2) NULL DEFAULT 0.00;
