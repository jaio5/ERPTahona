-- ================================================================
-- SCRIPT DE INICIALIZACIÓN DEL PLAN GENERAL CONTABLE
-- Para ERP Panadería Tahona
-- Fecha: 2026-01-13
-- ================================================================

USE tahona;

-- Eliminar datos existentes si los hay
TRUNCATE TABLE plan_cuentas;

-- ================================================================
-- CUENTAS BÁSICAS NECESARIAS PARA ASIENTOS AUTOMÁTICOS
-- ================================================================

-- 430 - Clientes
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('430', 'Clientes', 'ACTIVO', 1, 'Créditos con clientes por ventas de mercancías y prestación de servicios', 1);

-- 700 - Ventas de mercaderías
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('700', 'Ventas de mercaderías', 'INGRESO', 1, 'Enajenación de bienes adquiridos previamente por la empresa', 1);

-- 477 - IVA repercutido
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('477', 'IVA repercutido', 'PASIVO', 1, 'IVA devengado con motivo de la entrega de bienes o prestación de servicios', 1);

-- 600 - Compras de mercaderías
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('600', 'Compras de mercaderías', 'GASTO', 1, 'Aprovisionamientos de mercancías de la empresa', 1);

-- 472 - IVA soportado
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('472', 'IVA soportado', 'ACTIVO', 1, 'IVA soportado en las adquisiciones de bienes y servicios', 1);

-- 400 - Proveedores
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('400', 'Proveedores', 'PASIVO', 1, 'Deudas con suministradores de mercancías', 1);

-- 572 - Bancos e instituciones de crédito c/c vista, euros
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('572', 'Bancos c/c', 'ACTIVO', 1, 'Saldos a favor de la empresa en cuentas corrientes bancarias', 1);

-- 570 - Caja, euros
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('570', 'Caja', 'ACTIVO', 1, 'Disponibilidad de medios líquidos en caja', 1);

-- ================================================================
-- CUENTAS ADICIONALES ÚTILES
-- ================================================================

-- 100 - Capital social
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('100', 'Capital social', 'PATRIMONIO', 1, 'Capital escriturado en las sociedades', 1);

-- 129 - Resultado del ejercicio
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('129', 'Resultado del ejercicio', 'PATRIMONIO', 1, 'Beneficio o pérdida del ejercicio', 1);

-- 210 - Terrenos y bienes naturales
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('210', 'Terrenos', 'ACTIVO', 1, 'Solares de naturaleza urbana, fincas rústicas', 1);

-- 211 - Construcciones
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('211', 'Construcciones', 'ACTIVO', 1, 'Edificaciones en general', 1);

-- 216 - Mobiliario
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('216', 'Mobiliario', 'ACTIVO', 1, 'Mobiliario, material y equipos de oficina', 1);

-- 217 - Equipos para procesos de información
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('217', 'Equipos informáticos', 'ACTIVO', 1, 'Ordenadores y demás equipos electrónicos', 1);

-- 218 - Elementos de transporte
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('218', 'Elementos de transporte', 'ACTIVO', 1, 'Vehículos de todas clases', 1);

-- 410 - Acreedores por prestaciones de servicios
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('410', 'Acreedores', 'PASIVO', 1, 'Deudas con suministradores de servicios', 1);

-- 475 - IVA soportado deducible
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('475', 'IVA soportado deducible', 'ACTIVO', 1, 'IVA soportado de cuota deducible', 1);

-- 520 - Deudas a corto plazo con entidades de crédito
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('520', 'Deudas a c/p con bancos', 'PASIVO', 1, 'Préstamos y otras deudas con vencimiento a corto plazo', 1);

-- 521 - Deudas a corto plazo
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('521', 'Deudas a c/p', 'PASIVO', 1, 'Deudas con vencimiento a corto plazo', 1);

-- 626 - Servicios bancarios y similares
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('626', 'Servicios bancarios', 'GASTO', 1, 'Comisiones y gastos bancarios', 1);

-- 627 - Publicidad, propaganda y relaciones públicas
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('627', 'Publicidad', 'GASTO', 1, 'Gastos de publicidad y propaganda', 1);

-- 628 - Suministros
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('628', 'Suministros', 'GASTO', 1, 'Electricidad, agua, gas y otros suministros', 1);

-- 629 - Otros servicios
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('629', 'Otros servicios', 'GASTO', 1, 'Servicios diversos no incluidos en otras cuentas', 1);

-- 640 - Sueldos y salarios
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('640', 'Sueldos y salarios', 'GASTO', 1, 'Remuneraciones al personal', 1);

-- 642 - Seguridad Social a cargo de la empresa
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('642', 'Seguridad Social', 'GASTO', 1, 'Cuotas de la empresa a la Seguridad Social', 1);

-- 681 - Amortización del inmovilizado material
INSERT INTO plan_cuentas (codigo, nombre, tipo, nivel, descripcion, activo) VALUES
('681', 'Amortizaciones', 'GASTO', 1, 'Amortización del inmovilizado material', 1);

-- ================================================================
-- VERIFICACIÓN
-- ================================================================

SELECT
    COUNT(*) as total_cuentas,
    SUM(CASE WHEN tipo = 'ACTIVO' THEN 1 ELSE 0 END) as activos,
    SUM(CASE WHEN tipo = 'PASIVO' THEN 1 ELSE 0 END) as pasivos,
    SUM(CASE WHEN tipo = 'GASTO' THEN 1 ELSE 0 END) as gastos,
    SUM(CASE WHEN tipo = 'INGRESO' THEN 1 ELSE 0 END) as ingresos,
    SUM(CASE WHEN tipo = 'PATRIMONIO' THEN 1 ELSE 0 END) as patrimonio
FROM plan_cuentas;

-- Mostrar todas las cuentas creadas
SELECT codigo, nombre, tipo, activo
FROM plan_cuentas
ORDER BY CAST(codigo AS UNSIGNED);

-- ================================================================
-- FIN DEL SCRIPT
-- ================================================================

