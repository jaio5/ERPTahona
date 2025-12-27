@echo off
chcp 65001 >nul
color 0A
cls
echo.
echo ═══════════════════════════════════════════════════════
echo   ERP PANADERÍA TAHONA - DIAGNÓSTICO Y EJECUCIÓN
echo ═══════════════════════════════════════════════════════
echo.
cd /d "%~dp0"

echo [1] Verificando Java...
java -version 2>&1 | findstr "version"
if errorlevel 1 (
    color 0C
    echo ERROR: Java no encontrado
    pause
    exit /b 1
)
echo ✓ Java OK
echo.

echo [2] Verificando Maven...
mvn -version 2>&1 | findstr "Maven"
if errorlevel 1 (
    color 0C
    echo ERROR: Maven no encontrado
    pause
    exit /b 1
)
echo ✓ Maven OK
echo.

echo [3] Verificando login.fxml...
if exist "src\main\resources\ui\login.fxml" (
    for %%A in ("src\main\resources\ui\login.fxml") do echo ✓ Archivo existe: %%~zA bytes
) else (
    color 0C
    echo ERROR: login.fxml NO EXISTE
    pause
    exit /b 1
)
echo.

echo [4] Compilando proyecto...
echo.
call mvn clean compile -DskipTests
if errorlevel 1 (
    color 0C
    echo.
    echo ERROR: Compilación fallida
    echo.
    pause
    exit /b 1
)
echo.
echo ✓ Compilación exitosa
echo.

echo ═══════════════════════════════════════════════════════
echo   INICIANDO APLICACIÓN JAVAFX
echo ═══════════════════════════════════════════════════════
echo.
echo ⏰ La ventana JavaFX se abrirá en 40-50 segundos
echo ⏰ NO cierres esta ventana
echo.
echo Si ves errores a continuación, cópialos completos.
echo.
echo ───────────────────────────────────────────────────────
echo.

call mvn javafx:run

echo.
echo ───────────────────────────────────────────────────────
echo.
if errorlevel 1 (
    color 0C
    echo.
    echo ═══════════════════════════════════════════════════════
    echo   LA APLICACIÓN TERMINÓ CON ERROR
    echo ═══════════════════════════════════════════════════════
    echo.
    echo Por favor, lee los mensajes de error arriba.
    echo.
) else (
    color 0A
    echo.
    echo ═══════════════════════════════════════════════════════
    echo   APLICACIÓN CERRADA NORMALMENTE
    echo ═══════════════════════════════════════════════════════
    echo.
)

pause

