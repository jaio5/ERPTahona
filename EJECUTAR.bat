@echo off
cls
echo ========================================
echo  ERP Panaderia Tahona - Ejecutando
echo ========================================
echo.
echo Compilando...
cd /d "%~dp0"
call mvn clean compile -DskipTests
echo.
echo Ejecutando aplicacion JavaFX...
echo La ventana se abrira en unos segundos...
echo.
call mvn javafx:run
pause

