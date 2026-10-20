-- V51: amplía facturas.numero_albaran. Una factura que agrupa varios albaranes
-- guarda la lista de números ("ALB-2026-000001, ALB-2026-000002, ..."), y con
-- 50 caracteres no cabían más de tres.

ALTER TABLE facturas MODIFY COLUMN numero_albaran VARCHAR(500);
