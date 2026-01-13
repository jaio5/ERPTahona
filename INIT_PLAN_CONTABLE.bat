@echo off
cls
echo.
echo ════════════════════════════════════════════════════════════
echo    INICIALIZACIÓN DEL PLAN GENERAL CONTABLE
echo    ERP Panadería Tahona
echo ════════════════════════════════════════════════════════════
echo.

cd /d "%~dp0"

REM Verificar que MySQL está corriendo
echo Verificando MySQL...
sc query MySQL80 | find "RUNNING" > nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] MySQL no está corriendo. Iniciando...
    net start MySQL80
    timeout /t 3 > nul
)

echo.
echo ════════════════════════════════════════════════════════════
echo    EJECUTANDO SCRIPT SQL
echo ════════════════════════════════════════════════════════════
echo.

REM Solicitar contraseña de MySQL
set /p PASSWORD="Introduce la contraseña de root de MySQL: "

REM Ejecutar el script SQL
mysql -u root -p%PASSWORD% < init_plan_contable.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ════════════════════════════════════════════════════════════
    echo    ✅ PLAN CONTABLE INICIALIZADO CORRECTAMENTE
    echo ════════════════════════════════════════════════════════════
    echo.
    echo El Plan General Contable ha sido cargado en la base de datos.
    echo.
    echo Cuentas básicas creadas:
    echo   - 430: Clientes
    echo   - 700: Ventas de mercaderías
    echo   - 477: IVA repercutido
    echo   - 600: Compras de mercaderías
    echo   - 472: IVA soportado
    echo   - 400: Proveedores
    echo   - 572: Bancos c/c
    echo   - 570: Caja
    echo   + 20 cuentas adicionales
    echo.
) else (
    echo.
    echo ════════════════════════════════════════════════════════════
    echo    ❌ ERROR AL INICIALIZAR EL PLAN CONTABLE
    echo ════════════════════════════════════════════════════════════
    echo.
    echo Verifica:
    echo   - La contraseña de MySQL es correcta
    echo   - La base de datos 'tahona' existe
    echo   - MySQL está corriendo correctamente
    echo.
)

echo.
pause

