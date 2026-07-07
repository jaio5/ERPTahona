-- V40: plan de cuentas básico (PGC pymes) necesario para los asientos automáticos.
-- Sin estas cuentas cualquier venta/cobro/compra/pago falla en una instalación nueva.
-- INSERT IGNORE respeta instalaciones que ya tengan cuentas creadas a mano.

INSERT IGNORE INTO plan_cuentas (codigo, nombre, tipo, nivel, activo) VALUES
  ('100', 'Capital social',                        'PATRIMONIO', 1, 1),
  ('129', 'Resultado del ejercicio',               'PATRIMONIO', 1, 1),
  ('300', 'Mercaderías',                           'ACTIVO',     1, 1),
  ('400', 'Proveedores',                           'PASIVO',     1, 1),
  ('410', 'Acreedores por prestaciones de servicios', 'PASIVO',  1, 1),
  ('430', 'Clientes',                              'ACTIVO',     1, 1),
  ('465', 'Remuneraciones pendientes de pago',     'PASIVO',     1, 1),
  ('472', 'Hacienda Pública, IVA soportado',       'ACTIVO',     1, 1),
  ('473', 'Hacienda Pública, retenciones y pagos a cuenta', 'ACTIVO', 1, 1),
  ('475', 'Hacienda Pública, acreedora por conceptos fiscales', 'PASIVO', 1, 1),
  ('477', 'Hacienda Pública, IVA repercutido',     'PASIVO',     1, 1),
  ('570', 'Caja, euros',                           'ACTIVO',     1, 1),
  ('572', 'Bancos e instituciones de crédito c/c', 'ACTIVO',     1, 1),
  ('600', 'Compras de mercaderías',                'GASTO',      1, 1),
  ('621', 'Arrendamientos y cánones',              'GASTO',      1, 1),
  ('628', 'Suministros',                           'GASTO',      1, 1),
  ('640', 'Sueldos y salarios',                    'GASTO',      1, 1),
  ('700', 'Ventas de mercaderías',                 'INGRESO',    1, 1),
  ('705', 'Prestaciones de servicios',             'INGRESO',    1, 1);
