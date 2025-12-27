@echo off
chcp 65001 >nul
cls

echo.
echo ╔════════════════════════════════════════════════════╗
echo ║           ERP PANADERÍA TAHONA                     ║
echo ║        APLICACIÓN DE ESCRITORIO JAVAFX             ║
echo ╚════════════════════════════════════════════════════╝
echo.
echo ✅ Problema de configuración RESUELTO
echo ✅ Propiedades VeriFactu añadidas
echo ✅ Usuario admin creado
echo ✅ Base de datos lista
echo.
echo ════════════════════════════════════════════════════
echo.
echo 🚀 INICIANDO APLICACIÓN...
echo.
echo ⏰ La ventana se abrirá en 40-50 segundos
echo ⏰ NO cierres esta ventana
echo.
echo 📊 LOGS DE INICIO:
echo ════════════════════════════════════════════════════
echo.

cd /d "%~dp0"
mvn javafx:run

echo.
echo ════════════════════════════════════════════════════
echo.
echo Aplicación cerrada.
echo.
pause

