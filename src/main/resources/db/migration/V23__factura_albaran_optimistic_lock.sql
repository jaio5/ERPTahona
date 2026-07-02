-- Optimistic locking para Factura y AlbaranVenta: previene actualizaciones concurrentes perdidas.
-- (presupuestos se crea en V24 ya con columna version)
ALTER TABLE facturas
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE albaranes_venta
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
