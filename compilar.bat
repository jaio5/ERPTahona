@echo off
echo =====================================
echo   ERP Panaderia Tahona
echo   Compilando proyecto...
echo =====================================
echo.
cd /d "%~dp0"
call mvn clean compile
echo.
echo =====================================
echo   Compilacion completada
echo =====================================
pause
