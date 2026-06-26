-- Añade campo estado a albaranes_venta para tracking del ciclo de vida del documento
ALTER TABLE albaranes_venta
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
    COMMENT 'PENDIENTE=sin facturar, FACTURADO=tiene factura, ANULADO';
