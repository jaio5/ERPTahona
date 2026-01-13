@echo off
chcp 65001 > nul
cls
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║          🚀 ARRANCANDO ERP PANADERÍA TAHONA 🚀               ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo Por favor, espere...
echo.

cd /d "%~dp0"

echo [1/2] Compilando proyecto...
call mvn clean compile -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: No se pudo compilar el proyecto
    echo.
    echo Ejecutando diagnóstico detallado...
    call mvn compile -DskipTests
    pause
    exit /b 1
)

echo ✅ Compilación exitosa
echo.
echo [2/2] Iniciando aplicación...
echo.

call mvn javafx:run

echo.
echo ═══════════════════════════════════════════════════════════════
echo   LA APLICACIÓN SE HA CERRADO
echo ═══════════════════════════════════════════════════════════════
echo.
pause

