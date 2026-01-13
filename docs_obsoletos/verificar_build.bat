@echo off
echo.
echo ====================================
echo   VERIFICACION DE COMPILACION
echo ====================================
echo.

cd /d "D:\Programacion\ERP"

echo [1/2] Compilando aplicacion...
call mvn clean compile -DskipTests -q

if %ERRORLEVEL% == 0 (
    echo.
    echo [OK] BUILD SUCCESS - La aplicacion compila correctamente
    echo.
    echo [2/2] Verificando archivos compilados...
    if exist "target\classes\alicanteweb\erp\ErpLauncher.class" (
        echo [OK] ErpLauncher.class encontrado
    ) else (
        echo [ERROR] ErpLauncher.class NO encontrado
    )

    if exist "target\classes\alicanteweb\erp\service\PresupuestoService.class" (
        echo [OK] PresupuestoService.class encontrado
    ) else (
        echo [ERROR] PresupuestoService.class NO encontrado
    )

    if exist "target\classes\alicanteweb\erp\service\RolService.class" (
        echo [OK] RolService.class encontrado
    ) else (
        echo [ERROR] RolService.class NO encontrado
    )

    echo.
    echo ====================================
    echo   COMPILACION EXITOSA
    echo ====================================
) else (
    echo.
    echo [ERROR] BUILD FAILURE - Error al compilar
    echo.
    echo Ejecuta manualmente: mvn clean compile -DskipTests
    echo.
    echo ====================================
    echo   ERROR EN COMPILACION
    echo ====================================
)

echo.
pause

