@echo off
REM Script para verificar que la aplicación arranca correctamente
REM y que todas las vistas cargan sin errores

echo ========================================
echo   VERIFICACION DE LA APLICACION ERP
echo ========================================
echo.

echo [1/3] Compilando el proyecto...
call mvn clean compile -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] La compilacion fallo
    pause
    exit /b 1
)

echo.
echo [2/3] Compilacion exitosa
echo.
echo [3/3] Instrucciones para probar:
echo.
echo 1. La aplicacion se iniciara
echo 2. Inicia sesion con:
echo    Usuario: admin
echo    Contraseña: admin
echo.
echo 3. Prueba cada uno de los modulos:
echo    - Clientes
echo    - Articulos
echo    - Proveedores ^(PRINCIPAL A PROBAR^)
echo    - Facturas
echo    - Albaranes
echo    - Compras
echo.
echo 4. Si algun modulo da error, presiona Ctrl+C y reportalo
echo.
echo Presiona cualquier tecla para iniciar la aplicacion...
pause > nul

echo.
echo Iniciando aplicacion JavaFX...
echo.
call mvn javafx:run

echo.
echo ========================================
echo   VERIFICACION COMPLETADA
echo ========================================
pause

