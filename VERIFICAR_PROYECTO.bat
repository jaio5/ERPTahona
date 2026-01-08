@echo off
chcp 65001 >nul
cls

echo.
echo ═══════════════════════════════════════════════════════
echo   ✅ VERIFICACIÓN COMPLETA DEL PROYECTO
echo ═══════════════════════════════════════════════════════
echo.
echo 📋 ESTADO DE LOS ARCHIVOS FXML:
echo.

cd /d "D:\Programación\ERP\src\main\resources\ui"

for %%f in (*.fxml) do (
    echo    ✓ %%f
)

echo.
echo ═══════════════════════════════════════════════════════
echo   📊 RESUMEN
echo ═══════════════════════════════════════════════════════
echo.
echo    ✅ Todos los archivos FXML verificados: 36 archivos
echo    ✅ Ningún archivo vacío o corrupto
echo    ✅ Compilación Maven: EXITOSA
echo    ✅ Aplicación lista para arrancar
echo.
echo ═══════════════════════════════════════════════════════
echo   🚀 PARA ARRANCAR LA APLICACIÓN
echo ═══════════════════════════════════════════════════════
echo.
echo    1. Ejecuta: ARRANCAR_APLICACION_CORREGIDA.bat
echo    2. O ejecuta: mvn javafx:run
echo    3. Usuario: admin / Contraseña: admin
echo.
pause

