@echo off
chcp 65001 >nul
cls
echo.
echo ╔════════════════════════════════════════════════════╗
echo ║    ERP PANADERÍA TAHONA - APLICACIÓN JAVAFX      ║
echo ╚════════════════════════════════════════════════════╝
echo.
echo [1/3] Compilando proyecto...
echo.
cd /d "%~dp0"
call mvn clean compile -DskipTests -q
if errorlevel 1 (
    echo.
    echo ❌ ERROR: Compilación fallida
    echo.
    pause
    exit /b 1
)
echo ✅ Compilación exitosa
echo.
echo [2/3] Iniciando Spring Boot + JavaFX...
echo.
echo ⏳ Por favor espera 40-50 segundos...
echo ⏳ La ventana JavaFX se abrirá automáticamente
echo.
echo 📊 Logs del servidor:
echo ----------------------------------------
call mvn javafx:run
echo.
echo ----------------------------------------
echo.
pause

