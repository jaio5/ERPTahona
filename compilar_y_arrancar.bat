@echo off
echo =====================================
echo   ERP Panaderia Tahona
echo   Compilando y arrancando...
echo =====================================
echo.
cd /d "%~dp0"
echo [1/2] Compilando proyecto...
call mvn clean compile -q
if errorlevel 1 (
    echo.
    echo ERROR: La compilacion ha fallado
    pause
    exit /b 1
)
echo.
echo [2/2] Arrancando aplicacion...
echo.
call mvn javafx:run
pause
