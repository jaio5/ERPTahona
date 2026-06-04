-- V14: Añadir tipo_impositivo a facturas (Art. 6.1.j RD 1619/2012)
ALTER TABLE facturas ADD COLUMN IF NOT EXISTS tipo_impositivo DECIMAL(5,2);
