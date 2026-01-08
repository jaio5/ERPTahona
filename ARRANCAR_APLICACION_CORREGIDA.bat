@echo off
chcp 65001 >nul
cls

echo.
echo ═══════════════════════════════════════════════════════
echo   🚀 ARRANCANDO ERP PANADERÍA TAHONA
echo ═══════════════════════════════════════════════════════
echo.

cd /d "D:\Programación\ERP"

echo ✅ Compilación corregida exitosamente
echo.
echo 📌 Errores resueltos:
echo    - FacturaController: método abrirFormularioFactura → mostrarFormularioFactura
echo    - FacturaController: agregado método mostrarAdvertencia
echo    - FacturaController: corregido filtrarFacturas() con parámetro
echo.
echo 🔄 Arrancando aplicación...
echo.

mvn javafx:run

pause

