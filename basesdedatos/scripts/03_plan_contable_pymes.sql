-- =====================================================
-- PLAN CONTABLE PYMES - ESPAÑA
-- Script para cargar el Plan General Contable simplificado
-- =====================================================

USE tahona;

-- Limpiar tabla (si existe)
DELETE FROM plan_contable;

-- =====================================================
-- GRUPO 1: FINANCIACIÓN BÁSICA
-- =====================================================

INSERT INTO plan_contable (codigo, nombre, tipo_cuenta, activo) VALUES
('100', 'Capital social', 'PATRIMONIO_NETO', TRUE),
('102', 'Capital', 'PATRIMONIO_NETO', TRUE),
('112', 'Reserva legal', 'PATRIMONIO_NETO', TRUE),
('113', 'Reservas voluntarias', 'PATRIMONIO_NETO', TRUE),
('118', 'Aportaciones de socios o propietarios', 'PATRIMONIO_NETO', TRUE),
('120', 'Remanente', 'PATRIMONIO_NETO', TRUE),
('121', 'Resultados negativos de ejercicios anteriores', 'PATRIMONIO_NETO', TRUE),
('129', 'Resultado del ejercicio', 'PATRIMONIO_NETO', TRUE),
('170', 'Deudas a largo plazo con entidades de crédito', 'PASIVO', TRUE),
('171', 'Deudas a largo plazo', 'PASIVO', TRUE),
('172', 'Deudas a largo plazo transformables en subvenciones', 'PASIVO', TRUE),
('173', 'Proveedores de inmovilizado a largo plazo', 'PASIVO', TRUE),
('174', 'Acreedores por arrendamiento financiero a largo plazo', 'PASIVO', TRUE),
('175', 'Efectos a pagar a largo plazo', 'PASIVO', TRUE),
('176', 'Pasivos por derivados financieros a largo plazo', 'PASIVO', TRUE);

-- =====================================================
-- GRUPO 2: ACTIVO NO CORRIENTE
-- =====================================================

INSERT INTO plan_contable (codigo, nombre, tipo_cuenta, activo) VALUES
('200', 'Investigación', 'ACTIVO', TRUE),
('201', 'Desarrollo', 'ACTIVO', TRUE),
('202', 'Concesiones administrativas', 'ACTIVO', TRUE),
('203', 'Propiedad industrial', 'ACTIVO', TRUE),
('204', 'Fondo de comercio', 'ACTIVO', TRUE),
('205', 'Derechos de traspaso', 'ACTIVO', TRUE),
('206', 'Aplicaciones informáticas', 'ACTIVO', TRUE),
('210', 'Terrenos y bienes naturales', 'ACTIVO', TRUE),
('211', 'Construcciones', 'ACTIVO', TRUE),
('212', 'Instalaciones técnicas', 'ACTIVO', TRUE),
('213', 'Maquinaria', 'ACTIVO', TRUE),
('214', 'Utillaje', 'ACTIVO', TRUE),
('215', 'Otras instalaciones', 'ACTIVO', TRUE),
('216', 'Mobiliario', 'ACTIVO', TRUE),
('217', 'Equipos para procesos de información', 'ACTIVO', TRUE),
('218', 'Elementos de transporte', 'ACTIVO', TRUE),
('219', 'Otro inmovilizado material', 'ACTIVO', TRUE),
('220', 'Inversiones en terrenos y bienes naturales', 'ACTIVO', TRUE),
('221', 'Inversiones en construcciones', 'ACTIVO', TRUE),
('280', 'Amortización acumulada del inmovilizado intangible', 'ACTIVO', TRUE),
('281', 'Amortización acumulada del inmovilizado material', 'ACTIVO', TRUE),
('282', 'Amortización acumulada de las inversiones inmobiliarias', 'ACTIVO', TRUE);

-- =====================================================
-- GRUPO 3: EXISTENCIAS
-- =====================================================

INSERT INTO plan_contable (codigo, nombre, tipo_cuenta, activo) VALUES
('300', 'Mercaderías A', 'ACTIVO', TRUE),
('301', 'Mercaderías B', 'ACTIVO', TRUE),
('310', 'Materias primas A', 'ACTIVO', TRUE),
('311', 'Materias primas B', 'ACTIVO', TRUE),
('320', 'Elementos y conjuntos incorporables', 'ACTIVO', TRUE),
('321', 'Combustibles', 'ACTIVO', TRUE),
('322', 'Repuestos', 'ACTIVO', TRUE),
('325', 'Materiales diversos', 'ACTIVO', TRUE),
('326', 'Embalajes', 'ACTIVO', TRUE),
('327', 'Envases', 'ACTIVO', TRUE),
('328', 'Material de oficina', 'ACTIVO', TRUE),
('330', 'Productos en curso A', 'ACTIVO', TRUE),
('331', 'Productos en curso B', 'ACTIVO', TRUE),
('340', 'Productos semiterminados A', 'ACTIVO', TRUE),
('341', 'Productos semiterminados B', 'ACTIVO', TRUE),
('350', 'Productos terminados A', 'ACTIVO', TRUE),
('351', 'Productos terminados B', 'ACTIVO', TRUE),
('360', 'Subproductos A', 'ACTIVO', TRUE),
('361', 'Subproductos B', 'ACTIVO', TRUE),
('365', 'Residuos A', 'ACTIVO', TRUE),
('366', 'Residuos B', 'ACTIVO', TRUE),
('390', 'Deterioro de valor de las mercaderías', 'ACTIVO', TRUE),
('391', 'Deterioro de valor de las materias primas', 'ACTIVO', TRUE),
('392', 'Deterioro de valor de otros aprovisionamientos', 'ACTIVO', TRUE);

-- =====================================================
-- GRUPO 4: ACREEDORES Y DEUDORES
-- =====================================================

INSERT INTO plan_contable (codigo, nombre, tipo_cuenta, activo) VALUES
('400', 'Proveedores', 'PASIVO', TRUE),
('401', 'Proveedores, efectos comerciales a pagar', 'PASIVO', TRUE),
('403', 'Proveedores, empresas del grupo', 'PASIVO', TRUE),
('404', 'Proveedores, empresas asociadas', 'PASIVO', TRUE),
('405', 'Proveedores, otras partes vinculadas', 'PASIVO', TRUE),
('406', 'Envases y embalajes a devolver a proveedores', 'PASIVO', TRUE),
('407', 'Anticipos a proveedores', 'ACTIVO', TRUE),
('410', 'Acreedores por prestaciones de servicios', 'PASIVO', TRUE),
('411', 'Acreedores, efectos comerciales a pagar', 'PASIVO', TRUE),
('419', 'Acreedores por operaciones en común', 'PASIVO', TRUE),
('430', 'Clientes', 'ACTIVO', TRUE),
('431', 'Clientes, efectos comerciales a cobrar', 'ACTIVO', TRUE),
('432', 'Clientes, operaciones de factoring', 'ACTIVO', TRUE),
('433', 'Clientes, empresas del grupo', 'ACTIVO', TRUE),
('434', 'Clientes, empresas asociadas', 'ACTIVO', TRUE),
('435', 'Clientes, otras partes vinculadas', 'ACTIVO', TRUE),
('436', 'Clientes de dudoso cobro', 'ACTIVO', TRUE),
('437', 'Envases y embalajes a devolver por clientes', 'PASIVO', TRUE),
('438', 'Anticipos de clientes', 'PASIVO', TRUE),
('440', 'Deudores', 'ACTIVO', TRUE),
('441', 'Deudores, efectos comerciales a cobrar', 'ACTIVO', TRUE),
('446', 'Deudores de dudoso cobro', 'ACTIVO', TRUE),
('460', 'Hacienda Pública, deudor por diversos conceptos', 'ACTIVO', TRUE),
('465', 'Hacienda Pública, acreedora por conceptos fiscales', 'PASIVO', TRUE),
('470', 'Hacienda Pública, deudora por IVA', 'ACTIVO', TRUE),
('472', 'Hacienda Pública, IVA soportado', 'ACTIVO', TRUE),
('473', 'Hacienda Pública, retenciones y pagos a cuenta', 'ACTIVO', TRUE),
('475', 'Hacienda Pública, acreedora por IVA', 'PASIVO', TRUE),
('476', 'Organismos de la Seguridad Social, acreedores', 'PASIVO', TRUE),
('477', 'Hacienda Pública, IVA repercutido', 'PASIVO', TRUE),
('490', 'Deterioro de valor de créditos por operaciones comerciales', 'ACTIVO', TRUE);

-- =====================================================
-- GRUPO 5: CUENTAS FINANCIERAS
-- =====================================================

INSERT INTO plan_contable (codigo, nombre, tipo_cuenta, activo) VALUES
('500', 'Obligaciones y bonos a corto plazo', 'PASIVO', TRUE),
('505', 'Deudas a corto plazo con otras entidades de crédito', 'PASIVO', TRUE),
('509', 'Valores negociables amortizados', 'PASIVO', TRUE),
('510', 'Deudas a corto plazo con entidades de crédito', 'PASIVO', TRUE),
('511', 'Proveedores de inmovilizado a corto plazo', 'PASIVO', TRUE),
('512', 'Acreedores por arrendamiento financiero a corto plazo', 'PASIVO', TRUE),
('520', 'Deudas a corto plazo con entidades de crédito', 'PASIVO', TRUE),
('521', 'Deudas a corto plazo', 'PASIVO', TRUE),
('522', 'Deudas a corto plazo transformables en subvenciones', 'PASIVO', TRUE),
('523', 'Proveedores de inmovilizado a corto plazo', 'PASIVO', TRUE),
('524', 'Acreedores por arrendamiento financiero a corto plazo', 'PASIVO', TRUE),
('525', 'Efectos a pagar a corto plazo', 'PASIVO', TRUE),
('526', 'Dividendo activo a pagar', 'PASIVO', TRUE),
('527', 'Intereses a corto plazo de deudas con entidades de crédito', 'PASIVO', TRUE),
('540', 'Inversiones financieras a corto plazo en instrumentos de patrimonio', 'ACTIVO', TRUE),
('541', 'Valores representativos de deuda a corto plazo', 'ACTIVO', TRUE),
('542', 'Créditos a corto plazo', 'ACTIVO', TRUE),
('543', 'Créditos a corto plazo por enajenación de inmovilizado', 'ACTIVO', TRUE),
('544', 'Créditos a corto plazo al personal', 'ACTIVO', TRUE),
('545', 'Dividendo a cobrar', 'ACTIVO', TRUE),
('546', 'Intereses a corto plazo de valores representativos de deuda', 'ACTIVO', TRUE),
('547', 'Intereses a corto plazo de créditos', 'ACTIVO', TRUE),
('548', 'Imposiciones a corto plazo', 'ACTIVO', TRUE),
('551', 'Cuenta corriente con socios y administradores', 'ACTIVO', TRUE),
('552', 'Cuenta corriente con otras personas y entidades vinculadas', 'ACTIVO', TRUE),
('554', 'Cuenta corriente con uniones temporales de empresas y comunidades de bienes', 'ACTIVO', TRUE),
('555', 'Partidas pendientes de aplicación', 'ACTIVO', TRUE),
('556', 'Desembolsos exigidos sobre instrumentos de patrimonio', 'ACTIVO', TRUE),
('558', 'Socios por desembolsos exigidos', 'ACTIVO', TRUE),
('560', 'Fianzas y depósitos recibidos a corto plazo', 'PASIVO', TRUE),
('561', 'Depósitos recibidos a corto plazo', 'PASIVO', TRUE),
('565', 'Fianzas y depósitos constituidos a corto plazo', 'ACTIVO', TRUE),
('566', 'Depósitos constituidos a corto plazo', 'ACTIVO', TRUE),
('570', 'Caja, euros', 'ACTIVO', TRUE),
('571', 'Caja, moneda extranjera', 'ACTIVO', TRUE),
('572', 'Bancos e instituciones de crédito c/c vista, euros', 'ACTIVO', TRUE),
('573', 'Bancos e instituciones de crédito c/c vista, moneda extranjera', 'ACTIVO', TRUE),
('574', 'Bancos e instituciones de crédito, cuentas de ahorro, euros', 'ACTIVO', TRUE),
('575', 'Bancos e instituciones de crédito, cuentas de ahorro, moneda extranjera', 'ACTIVO', TRUE);

-- =====================================================
-- GRUPO 6: COMPRAS Y GASTOS
-- =====================================================

INSERT INTO plan_contable (codigo, nombre, tipo_cuenta, activo) VALUES
('600', 'Compras de mercaderías', 'GASTO', TRUE),
('601', 'Compras de materias primas', 'GASTO', TRUE),
('602', 'Compras de otros aprovisionamientos', 'GASTO', TRUE),
('606', 'Descuentos sobre compras por pronto pago', 'GASTO', TRUE),
('607', 'Trabajos realizados por otras empresas', 'GASTO', TRUE),
('608', 'Devoluciones de compras y operaciones similares', 'GASTO', TRUE),
('609', 'Rappels por compras', 'GASTO', TRUE),
('610', 'Variación de existencias de mercaderías', 'GASTO', TRUE),
('611', 'Variación de existencias de materias primas', 'GASTO', TRUE),
('612', 'Variación de existencias de otros aprovisionamientos', 'GASTO', TRUE),
('620', 'Gastos en investigación y desarrollo del ejercicio', 'GASTO', TRUE),
('621', 'Arrendamientos y cánones', 'GASTO', TRUE),
('622', 'Reparaciones y conservación', 'GASTO', TRUE),
('623', 'Servicios de profesionales independientes', 'GASTO', TRUE),
('624', 'Transportes', 'GASTO', TRUE),
('625', 'Primas de seguros', 'GASTO', TRUE),
('626', 'Servicios bancarios y similares', 'GASTO', TRUE),
('627', 'Publicidad, propaganda y relaciones públicas', 'GASTO', TRUE),
('628', 'Suministros', 'GASTO', TRUE),
('629', 'Otros servicios', 'GASTO', TRUE),
('630', 'Tributos', 'GASTO', TRUE),
('631', 'Otros tributos', 'GASTO', TRUE),
('640', 'Sueldos y salarios', 'GASTO', TRUE),
('641', 'Indemnizaciones', 'GASTO', TRUE),
('642', 'Seguridad Social a cargo de la empresa', 'GASTO', TRUE),
('649', 'Otros gastos sociales', 'GASTO', TRUE),
('650', 'Pérdidas de créditos comerciales incobrables', 'GASTO', TRUE),
('659', 'Otras pérdidas en gestión corriente', 'GASTO', TRUE),
('662', 'Intereses de deudas a largo plazo', 'GASTO', TRUE),
('663', 'Intereses de deudas a corto plazo', 'GASTO', TRUE),
('664', 'Dividendos de acciones o participaciones', 'GASTO', TRUE),
('665', 'Intereses por descuento de efectos', 'GASTO', TRUE),
('666', 'Pérdidas en valores negociables', 'GASTO', TRUE),
('667', 'Pérdidas de créditos no comerciales', 'GASTO', TRUE),
('668', 'Diferencias negativas de cambio', 'GASTO', TRUE),
('669', 'Otros gastos financieros', 'GASTO', TRUE),
('670', 'Pérdidas procedentes del inmovilizado intangible', 'GASTO', TRUE),
('671', 'Pérdidas procedentes del inmovilizado material', 'GASTO', TRUE),
('672', 'Pérdidas procedentes de las inversiones inmobiliarias', 'GASTO', TRUE),
('678', 'Gastos excepcionales', 'GASTO', TRUE),
('680', 'Amortización del inmovilizado intangible', 'GASTO', TRUE),
('681', 'Amortización del inmovilizado material', 'GASTO', TRUE),
('682', 'Amortización de las inversiones inmobiliarias', 'GASTO', TRUE);

-- =====================================================
-- GRUPO 7: VENTAS E INGRESOS
-- =====================================================

INSERT INTO plan_contable (codigo, nombre, tipo_cuenta, activo) VALUES
('700', 'Ventas de mercaderías', 'INGRESO', TRUE),
('701', 'Ventas de productos terminados', 'INGRESO', TRUE),
('702', 'Ventas de productos semiterminados', 'INGRESO', TRUE),
('703', 'Ventas de subproductos y residuos', 'INGRESO', TRUE),
('704', 'Ventas de envases y embalajes', 'INGRESO', TRUE),
('705', 'Prestaciones de servicios', 'INGRESO', TRUE),
('706', 'Descuentos sobre ventas por pronto pago', 'INGRESO', TRUE),
('708', 'Devoluciones de ventas y operaciones similares', 'INGRESO', TRUE),
('709', 'Rappels sobre ventas', 'INGRESO', TRUE),
('710', 'Variación de existencias de productos en curso', 'INGRESO', TRUE),
('711', 'Variación de existencias de productos semiterminados', 'INGRESO', TRUE),
('712', 'Variación de existencias de productos terminados', 'INGRESO', TRUE),
('713', 'Variación de existencias de subproductos, residuos y materiales recuperados', 'INGRESO', TRUE),
('740', 'Subvenciones a la explotación', 'INGRESO', TRUE),
('746', 'Subvenciones, donaciones y legados de capital transferidos al resultado del ejercicio', 'INGRESO', TRUE),
('747', 'Otras subvenciones, donaciones y legados transferidos al resultado del ejercicio', 'INGRESO', TRUE),
('760', 'Ingresos de participaciones en instrumentos de patrimonio', 'INGRESO', TRUE),
('761', 'Ingresos de valores representativos de deuda', 'INGRESO', TRUE),
('762', 'Ingresos de créditos a largo plazo', 'INGRESO', TRUE),
('763', 'Ingresos de créditos a corto plazo', 'INGRESO', TRUE),
('765', 'Descuentos sobre compras por pronto pago', 'INGRESO', TRUE),
('766', 'Beneficios en valores negociables', 'INGRESO', TRUE),
('768', 'Diferencias positivas de cambio', 'INGRESO', TRUE),
('769', 'Otros ingresos financieros', 'INGRESO', TRUE),
('770', 'Beneficios procedentes del inmovilizado intangible', 'INGRESO', TRUE),
('771', 'Beneficios procedentes del inmovilizado material', 'INGRESO', TRUE),
('772', 'Beneficios procedentes de las inversiones inmobiliarias', 'INGRESO', TRUE),
('773', 'Beneficios procedentes de participaciones a largo plazo en partes vinculadas', 'INGRESO', TRUE),
('775', 'Beneficios por operaciones con obligaciones propias', 'INGRESO', TRUE),
('778', 'Ingresos excepcionales', 'INGRESO', TRUE),
('790', 'Reversión del deterioro del inmovilizado intangible', 'INGRESO', TRUE),
('791', 'Reversión del deterioro del inmovilizado material', 'INGRESO', TRUE),
('792', 'Reversión del deterioro de las inversiones inmobiliarias', 'INGRESO', TRUE);

-- Verificar que se insertaron correctamente
SELECT COUNT(*) as total_cuentas FROM plan_contable;
SELECT tipo_cuenta, COUNT(*) as cantidad FROM plan_contable GROUP BY tipo_cuenta;

-- Mensaje final
SELECT '✅ PLAN CONTABLE PYMES CARGADO CORRECTAMENTE' as Resultado;
SELECT 'Total de cuentas cargadas:' as Info, COUNT(*) as Cantidad FROM plan_contable;

