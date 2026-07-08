-- V44: descuentos de línea (tipo % o importe) y descuento global de documento.
--   * *_lineas.descuento_tipo    -> interpreta el valor 'descuento' ya existente (% por defecto)
--   * facturas / albaranes_venta -> descuento global del documento (tipo + valor)

ALTER TABLE factura_lineas      ADD COLUMN descuento_tipo VARCHAR(10) NULL DEFAULT 'PORCENTAJE';
ALTER TABLE albaran_venta_lineas ADD COLUMN descuento_tipo VARCHAR(10) NULL DEFAULT 'PORCENTAJE';

ALTER TABLE facturas ADD COLUMN descuento_global_tipo VARCHAR(10) NULL DEFAULT 'PORCENTAJE';
ALTER TABLE facturas ADD COLUMN descuento_global_valor DECIMAL(10,2) NULL DEFAULT 0.00;

ALTER TABLE albaranes_venta ADD COLUMN descuento_global_tipo VARCHAR(10) NULL DEFAULT 'PORCENTAJE';
ALTER TABLE albaranes_venta ADD COLUMN descuento_global_valor DECIMAL(10,2) NULL DEFAULT 0.00;
