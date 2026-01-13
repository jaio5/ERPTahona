@echo off
chcp 65001 > nul
cls
echo.
echo ═══════════════════════════════════════════════════════════
echo   🧪 PRUEBA RÁPIDA - MÓDULO DE PRESUPUESTOS
echo ═══════════════════════════════════════════════════════════
echo.
echo 📋 Verificando archivos del módulo...
echo.

if exist "src\main\java\alicanteweb\erp\repository\PresupuestoRepository.java" (
    echo ✅ PresupuestoRepository.java
) else (
    echo ❌ PresupuestoRepository.java NO ENCONTRADO
)

if exist "src\main\java\alicanteweb\erp\service\PresupuestoService.java" (
    echo ✅ PresupuestoService.java
) else (
    echo ❌ PresupuestoService.java NO ENCONTRADO
)

if exist "src\main\java\alicanteweb\erp\controller\PresupuestoController.java" (
    echo ✅ PresupuestoController.java
) else (
    echo ❌ PresupuestoController.java NO ENCONTRADO
)

if exist "src\main\resources\ui\presupuestos_panel.fxml" (
    echo ✅ presupuestos_panel.fxml
) else (
    echo ❌ presupuestos_panel.fxml NO ENCONTRADO
)

echo.
echo ═══════════════════════════════════════════════════════════
echo   📦 Compilando proyecto...
echo ═══════════════════════════════════════════════════════════
echo.

call mvn clean compile -DskipTests -q

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ COMPILACIÓN EXITOSA
    echo.
    echo ═══════════════════════════════════════════════════════════
    echo   🚀 Iniciando aplicación...
    echo ═══════════════════════════════════════════════════════════
    echo.
    echo Instrucciones de prueba:
    echo   1. Inicia sesión con: admin / admin
    echo   2. Haz clic en el botón "Presupuestos" en el menú lateral
    echo   3. Verifica que la vista carga correctamente
    echo   4. Prueba las funciones de búsqueda y filtrado
    echo   5. Selecciona un presupuesto y prueba "Ver Detalles"
    echo.
    echo Presiona Ctrl+C para detener la aplicación
    echo.
    pause
    call mvn javafx:run
) else (
    echo.
    echo ❌ ERROR EN LA COMPILACIÓN
    echo.
    echo Revisa los errores anteriores para más detalles.
)

echo.
pause

