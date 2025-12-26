@echo off
REM Script para ejecutar la configuración de base de datos para ERP Tahona
REM Configura empresa GRUPO BABO y estados de facturas

echo ================================================
echo CONFIGURACION DE BASE DE DATOS - ERP TAHONA
echo ================================================
echo.
echo Este script va a:
echo 1. Crear tabla empresa_config con datos de GRUPO BABO
echo 2. Anadir campos de estado a facturas
echo 3. Configurar Verifactu
echo.
echo Presiona cualquier tecla para continuar o Ctrl+C para cancelar...
pause > nul

echo.
echo Ejecutando script SQL...
echo.

REM Intenta ejecutar el SQL
mysql -u root -p tahona < "%~dp0basesdedatos\agregar_estado_facturas_y_empresa.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo ^>^>^> EXITO: Base de datos configurada correctamente
    echo ================================================
    echo.
    echo Datos de GRUPO BABO insertados:
    echo - CIF: F54059985
    echo - Nombre: GRUPO BABO, S.Coop.V.L.
    echo - Telefono: 965 68 73 58
    echo.
    echo Verifica los datos:
    mysql -u root -p tahona -e "SELECT nombre_empresa, cif, telefono FROM empresa_config WHERE activo = TRUE;"
    echo.
    echo Siguiente paso:
    echo   .\mvnw clean compile
    echo.
) else (
    echo.
    echo ================================================
    echo ^>^>^> ERROR: No se pudo ejecutar el script SQL
    echo ================================================
    echo.
    echo Verifica:
    echo 1. MySQL esta instalado y en el PATH
    echo 2. El usuario root tiene acceso
    echo 3. La base de datos 'tahona' existe
    echo.
    echo Puedes ejecutar manualmente:
    echo   mysql -u root -p tahona ^< basesdedatos\agregar_estado_facturas_y_empresa.sql
    echo.
)

pause

