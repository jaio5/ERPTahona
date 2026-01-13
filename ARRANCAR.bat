@echo off
cls
echo.
echo ════════════════════════════════════════════════
echo    ERP PANADERIA TAHONA - ARRANQUE
echo ════════════════════════════════════════════════
echo.

cd /d "%~dp0"

REM Iniciar MySQL
echo Iniciando MySQL...
net start MySQL80 > nul 2>&1

REM Compilar
echo Compilando proyecto...
call mvn clean compile -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se pudo compilar
    pause
    exit /b 1
)

REM Arrancar
echo.
echo ════════════════════════════════════════════════
echo    ARRANCANDO APLICACION...
echo    Usuario: admin
echo    Password: admin
echo ════════════════════════════════════════════════
echo.

call mvn javafx:run

pause

