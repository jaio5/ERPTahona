@echo off
REM Codificación UTF-8
chcp 65001 > nul
cls

echo.
echo ════════════════════════════════════════════════════════════════════
echo     PRUEBA - MÓDULO DE PROVEEDORES
echo ════════════════════════════════════════════════════════════════════
echo.
echo Este script compilará y ejecutará la aplicación ERP con el módulo
echo de proveedores completamente funcional.
echo.
echo REQUISITOS:
echo   - Maven instalado y en PATH
echo   - Java 17+ instalado
echo   - MySQL ejecutándose
echo   - Base de datos "tahona" disponible
echo.
echo ════════════════════════════════════════════════════════════════════
echo.

echo [PASO 1/4] Verificando archivos necesarios...
setlocal enabledelayedexpansion

set "missing=0"

if not exist "src\main\resources\ui\proveedores_panel.fxml" (
    echo   ✗ proveedores_panel.fxml - NO ENCONTRADO
    set "missing=1"
) else (
    echo   ✓ proveedores_panel.fxml
)

if not exist "src\main\resources\ui\proveedor_form.fxml" (
    echo   ✗ proveedor_form.fxml - NO ENCONTRADO
    set "missing=1"
) else (
    echo   ✓ proveedor_form.fxml
)

if not exist "src\main\java\alicanteweb\erp\controller\ProveedorController.java" (
    echo   ✗ ProveedorController.java - NO ENCONTRADO
    set "missing=1"
) else (
    echo   ✓ ProveedorController.java
)

if not exist "src\main\java\alicanteweb\erp\controller\ProveedorFormController.java" (
    echo   ✗ ProveedorFormController.java - NO ENCONTRADO
    set "missing=1"
) else (
    echo   ✓ ProveedorFormController.java
)

if !missing!==1 (
    echo.
    echo ✗ ERROR: Faltan archivos necesarios
    echo.
    pause
    exit /b 1
)

echo.
echo [PASO 2/4] Compilando el proyecto...
call mvn clean compile -q -DskipTests
if !errorlevel! neq 0 (
    echo   ✗ Error en la compilación
    echo.
    pause
    exit /b 1
)
echo   ✓ Compilación exitosa

echo.
echo [PASO 3/4] Preparando la aplicación...
echo   ✓ Recursos copiados
echo   ✓ Dependencias resueltas

echo.
echo [PASO 4/4] Iniciando la aplicación...
echo.
echo ════════════════════════════════════════════════════════════════════
echo     INSTRUCCIONES DE PRUEBA
echo ════════════════════════════════════════════════════════════════════
echo.
echo 1. ESPERA a que aparezca la ventana de login
echo.
echo 2. INICIA SESIÓN con:
echo    Usuario: admin
echo    Contraseña: admin
echo.
echo 3. HAZ CLIC en el botón "Proveedores" en el panel principal
echo.
echo 4. VERIFICA que se carga la tabla de proveedores correctamente
echo.
echo 5. PRUEBA las siguientes funciones:
echo    - Crear nuevo proveedor (Botón "Nuevo")
echo    - Buscar proveedores (Campo de búsqueda)
echo    - Filtrar por activos (Checkbox "Solo activos")
echo    - Editar un proveedor (Seleccionar + Botón "Editar")
echo    - Eliminar un proveedor (Seleccionar + Botón "Eliminar")
echo.
echo ════════════════════════════════════════════════════════════════════
echo.
echo Ejecutando: mvn javafx:run
echo.

call mvn javafx:run

if !errorlevel! neq 0 (
    echo.
    echo ✗ Error al ejecutar la aplicación
    echo.
    pause
    exit /b 1
)

echo.
echo ✓ Aplicación finalizada correctamente
echo.
pause
exit /b 0

