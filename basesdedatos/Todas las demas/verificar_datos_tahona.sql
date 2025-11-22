-- ================================================================
-- SCRIPT DE VERIFICACIÓN - Base de Datos Tahona ERP
-- Ejecutar DESPUÉS de cargar tahona_mysql_mejorado.sql
-- ================================================================

USE tahona;

-- ================================================================
-- VERIFICACIÓN DE TABLAS CREADAS
-- ================================================================
SELECT
    COUNT(*) AS total_tablas,
    SUM(CASE WHEN ENGINE = 'InnoDB' THEN 1 ELSE 0 END) AS tablas_innodb,
    SUM(CASE WHEN TABLE_COLLATION = 'utf8mb4_unicode_ci' THEN 1 ELSE 0 END) AS tablas_utf8mb4
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'tahona';

-- ================================================================
-- CONTEO DE REGISTROS POR TABLA (TABLAS CON DATOS)
-- ================================================================
SELECT '=== CONTEO DE DATOS POR TABLA ===' AS '';

SELECT 'Almacenes' AS Tabla, COUNT(*) AS Registros FROM Almacenes
UNION ALL
SELECT 'Articulos', COUNT(*) FROM Articulos
UNION ALL
SELECT 'BalancesConImporte', COUNT(*) FROM BalancesConImporte
UNION ALL
SELECT 'balancescuentas', COUNT(*) FROM balancescuentas
UNION ALL
SELECT 'Clientes', COUNT(*) FROM Clientes
UNION ALL
SELECT 'Cuentas', COUNT(*) FROM Cuentas
UNION ALL
SELECT 'Familias', COUNT(*) FROM Familias
UNION ALL
SELECT 'FormasdePago', COUNT(*) FROM FormasdePago
UNION ALL
SELECT 'FormasdePagoDesglose', COUNT(*) FROM FormasdePagoDesglose
UNION ALL
SELECT 'Provincias', COUNT(*) FROM Provincias
UNION ALL
SELECT 'Subcuentas', COUNT(*) FROM Subcuentas
UNION ALL
SELECT 'TiposdeIVA', COUNT(*) FROM TiposdeIVA
UNION ALL
SELECT 'Zonas', COUNT(*) FROM Zonas
ORDER BY Tabla;

-- ================================================================
-- MUESTRA DE DATOS - ARTICULOS
-- ================================================================
SELECT '=== MUESTRA DE ARTÍCULOS ===' AS '';

SELECT
    CodigoArticulo,
    DescripcionArticulo,
    FamiliaArticulo,
    PVP1,
    Tipo_de_IVA
FROM Articulos
ORDER BY CodigoArticulo
LIMIT 10;

-- ================================================================
-- MUESTRA DE DATOS - CLIENTES
-- ================================================================
SELECT '=== MUESTRA DE CLIENTES ===' AS '';

SELECT
    Codigo,
    Nombre,
    Poblacion,
    Provincia,
    CIF
FROM Clientes
WHERE Nombre NOT LIKE '%**baja**%'
ORDER BY Codigo
LIMIT 10;

-- ================================================================
-- MUESTRA DE DATOS - FAMILIAS
-- ================================================================
SELECT '=== FAMILIAS DE PRODUCTOS ===' AS '';

SELECT
    CodigoFamilia,
    DescripcionFamilia
FROM Familias
ORDER BY CodigoFamilia;

-- ================================================================
-- VERIFICACIÓN DE TIPOS DE IVA
-- ================================================================
SELECT '=== TIPOS DE IVA CONFIGURADOS ===' AS '';

SELECT
    Codigo_Tipo_de_IVA,
    Porcentaje_de_IVA AS 'IVA %',
    Porcentaje_de_REQ AS 'REQ %'
FROM TiposdeIVA
ORDER BY Codigo_Tipo_de_IVA;

-- ================================================================
-- VERIFICACIÓN DE FORMAS DE PAGO
-- ================================================================
SELECT '=== FORMAS DE PAGO ===' AS '';

SELECT * FROM FormasdePago ORDER BY CodigoFormaPago;

-- ================================================================
-- VERIFICACIÓN PROVINCIA CIUDAD REAL
-- ================================================================
SELECT '=== VERIFICACIÓN PROVINCIA CIUDAD REAL ===' AS '';

SELECT * FROM Provincias WHERE CodigoProvincia = '13';

-- ================================================================
-- RESUMEN FINAL
-- ================================================================
SELECT '=== RESUMEN DE VERIFICACIÓN ===' AS '';

SELECT
    'Base de datos cargada correctamente' AS Estado,
    DATABASE() AS BaseDatos,
    (SELECT COUNT(*) FROM Articulos) AS TotalArticulos,
    (SELECT COUNT(*) FROM Clientes) AS TotalClientes,
    (SELECT COUNT(*) FROM Cuentas) AS TotalCuentas,
    (SELECT COUNT(*) FROM BalancesConImporte) AS TotalBalances;

-- ================================================================
-- FIN DE VERIFICACIÓN
-- ================================================================

