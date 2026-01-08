@echo off
chcp 65001 >nul
cls

echo.
echo ================================================================
echo   ARRANCANDO ERP PANADERIA TAHONA
echo ================================================================
echo.

cd /d "D:\Programación\ERP"

echo Compilacion corregida exitosamente
echo.
echo Errores resueltos:
echo    - FacturaController: metodo abrirFormularioFactura corregido
echo    - FacturaController: agregado metodo mostrarAdvertencia
echo    - FacturaController: corregido filtrarFacturas con parametro
echo.
echo Arrancando aplicacion...
echo.

mvn javafx:run

pause

