@echo off
echo =====================================
echo   ERP Panaderia Tahona
echo   Arrancando aplicacion...
echo =====================================
echo.
cd /d "%~dp0"
call mvn javafx:run
pause
