-- Optimistic locking para Factura, AlbaranVenta y Presupuesto: previene actualizaciones concurrentes perdidas.
ALTER TABLE facturas
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE albaranes_venta
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE presupuestos
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
