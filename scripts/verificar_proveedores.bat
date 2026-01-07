@echo off
chcp 65001 > nul
echo.
echo ═══════════════════════════════════════════════════════════════
echo   VERIFICACIÓN RÁPIDA - MÓDULO DE PROVEEDORES
echo ═══════════════════════════════════════════════════════════════
echo.

echo [1/5] Verificando archivos FXML...
if exist "src\main\resources\ui\proveedores_panel.fxml" (
    echo   ✓ proveedores_panel.fxml - OK
) else (
    echo   ✗ proveedores_panel.fxml - FALTA
    goto :error
)

if exist "src\main\resources\ui\proveedor_form.fxml" (
    echo   ✓ proveedor_form.fxml - OK
) else (
    echo   ✗ proveedor_form.fxml - FALTA
    goto :error
)

echo.
echo [2/5] Verificando controladores Java...
if exist "src\main\java\alicanteweb\erp\controller\ProveedorController.java" (
    echo   ✓ ProveedorController.java - OK
) else (
    echo   ✗ ProveedorController.java - FALTA
    goto :error
)

if exist "src\main\java\alicanteweb\erp\controller\ProveedorFormController.java" (
    echo   ✓ ProveedorFormController.java - OK
) else (
    echo   ✗ ProveedorFormController.java - FALTA
    goto :error
)

echo.
echo [3/5] Verificando servicios...
if exist "src\main\java\alicanteweb\erp\service\ProveedorService.java" (
    echo   ✓ ProveedorService.java - OK
) else (
    echo   ✗ ProveedorService.java - FALTA
    goto :error
)

echo.
echo [4/5] Verificando repositorios...
if exist "src\main\java\alicanteweb\erp\repository\ProveedorRepository.java" (
    echo   ✓ ProveedorRepository.java - OK
) else (
    echo   ✗ ProveedorRepository.java - FALTA
    goto :error
)

echo.
echo [5/5] Verificando entidades...
if exist "src\main\java\alicanteweb\erp\entities\Proveedor.java" (
    echo   ✓ Proveedor.java - OK
) else (
    echo   ✗ Proveedor.java - FALTA
    goto :error
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo   ✓ TODOS LOS ARCHIVOS NECESARIOS ESTÁN PRESENTES
echo ═══════════════════════════════════════════════════════════════
echo.
echo Para ejecutar la aplicación, usa:
echo   mvn javafx:run
echo.
echo Para probar el módulo de proveedores:
echo   1. Inicia sesión con: admin / admin
echo   2. Haz clic en el botón "Proveedores"
echo   3. Verifica que se carga la vista correctamente
echo.
pause
exit /b 0

:error
echo.
echo ═══════════════════════════════════════════════════════════════
echo   ✗ ERROR: Faltan archivos necesarios
echo ═══════════════════════════════════════════════════════════════
echo.
pause
exit /b 1

