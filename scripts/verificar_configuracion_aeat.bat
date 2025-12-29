@echo off
chcp 65001 >nul
echo.
echo ══════════════════════════════════════════════════════════
echo   🔍 VERIFICACIÓN DE CONFIGURACIÓN PARA AEAT
echo ══════════════════════════════════════════════════════════
echo.

cd /d "%~dp0.."

echo 📋 Verificando configuración de empresa...
echo.

mysql -u root -pIirne322* -e "USE tahona; SELECT CASE WHEN COUNT(*) > 0 THEN '✅ Configuración de empresa: OK' ELSE '❌ Sin configuración de empresa' END AS resultado FROM empresa_config WHERE activo = TRUE;" 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: No se pudo conectar a MySQL
    echo.
    echo Verifica:
    echo   - MySQL está corriendo
    echo   - Usuario y contraseña correctos
    echo   - Base de datos 'tahona' existe
    echo.
    pause
    exit /b 1
)

echo.
echo 🔐 Verificando Verifactu...
echo.

mysql -u root -pIirne322* tahona -e "SELECT nombre_empresa, cif, CASE WHEN verifactu_habilitado = TRUE THEN '✅ HABILITADO' ELSE '❌ DESHABILITADO' END AS verifactu, verifactu_nif_emisor FROM empresa_config WHERE activo = TRUE;" 2>nul

echo.
echo 📦 Verificando datos para facturación...
echo.

mysql -u root -pIirne322* tahona -e "SELECT 'Clientes' AS tipo, COUNT(*) AS total FROM clientes WHERE activo = TRUE UNION ALL SELECT 'Artículos', COUNT(*) FROM articulos WHERE activo = TRUE UNION ALL SELECT 'Almacenes', COUNT(*) FROM almacenes;" 2>nul

echo.
echo 📂 Verificando certificado...
echo.

if exist "src\main\resources\certs\mi_certificado.p12" (
    echo ✅ Certificado encontrado: src\main\resources\certs\mi_certificado.p12
) else (
    echo ❌ Certificado NO encontrado
    echo.
    echo Para habilitar Verifactu necesitas:
    echo   1. Obtener certificado digital de la FNMT
    echo   2. Guardarlo en: src\main\resources\certs\mi_certificado.p12
    echo   3. Configurar contraseña en application.properties
)

echo.
echo 📝 Verificando application.properties...
echo.

findstr /C:"verifactu.aeat.enabled" src\main\resources\application.properties >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Configuración Verifactu encontrada en application.properties
    echo.
    findstr /C:"verifactu" src\main\resources\application.properties
) else (
    echo ⚠️ Sin configuración Verifactu en application.properties
    echo.
    echo Añade estas líneas:
    echo   verifactu.aeat.enabled=true
    echo   verifactu.keystore.path=/certs/mi_certificado.p12
    echo   verifactu.keystore.password=TU_PASSWORD
)

echo.
echo ══════════════════════════════════════════════════════════
echo.
echo 📚 DOCUMENTACIÓN:
echo   - GUIA_CONFIGURACION_AEAT.md (guía completa)
echo   - Scripts SQL en: scripts\sql\
echo.
echo 🚀 SIGUIENTE PASO:
if not exist "src\main\resources\certs\mi_certificado.p12" (
    echo   1. Obtener certificado digital de la FNMT
    echo   2. Ejecutar: mysql -u root -p tahona ^< scripts\sql\configurar_empresa_grupo_babo.sql
    echo   3. Copiar certificado a: src\main\resources\certs\
    echo   4. Configurar application.properties
) else (
    echo   1. Verificar datos de empresa en MySQL
    echo   2. Configurar contraseña del certificado
    echo   3. Ejecutar: .\scripts\iniciar.bat
    echo   4. Probar el flujo de facturación
)
echo.
echo ══════════════════════════════════════════════════════════
echo.
pause

