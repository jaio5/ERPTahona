@echo off
pause

call mvn javafx:run
echo.
echo Ejecutando aplicacion JavaFX...
echo.

)
    exit /b 1
    pause
    echo ERROR: Compilacion fallida
if errorlevel 1 (
call mvn clean compile -DskipTests
echo Compilando aplicacion...
echo.

)
    exit /b 1
    pause
    echo ERROR: Java no encontrado
if errorlevel 1 (
java -version
echo Verificando Java...

cd /d "%~dp0"

echo.
echo ========================================
echo  ERP Panaderia Tahona - JavaFX Desktop
echo ========================================

