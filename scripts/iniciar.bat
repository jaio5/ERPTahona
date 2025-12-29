@echo off
chcp 65001 > nul
echo ═══════════════════════════════════════════════════════
echo   INICIANDO APLICACIÓN ERP - TAHONA
echo ═══════════════════════════════════════════════════════
echo.
echo 📋 Información del sistema:
echo    • Base de datos: tahona
echo    • Usuario DB: root
echo    • Usuario App: admin
echo    • Contraseña App: admin
echo.
echo 🔄 Compilando y ejecutando la aplicación...
echo    (Esto puede tardar 30-40 segundos)
echo.

cd /d "%~dp0"

echo [%time%] Iniciando Maven...
mvn javafx:run

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ═══════════════════════════════════════════════════════
    echo   ✓ APLICACIÓN FINALIZADA CORRECTAMENTE
    echo ═══════════════════════════════════════════════════════
) else (
    echo.
    echo ═══════════════════════════════════════════════════════
    echo   ✗ ERROR AL EJECUTAR LA APLICACIÓN
    echo ═══════════════════════════════════════════════════════
    echo.
    echo Posibles soluciones:
    echo  1. Verificar que MySQL está corriendo
    echo  2. Ejecutar: verificar_sistema_completo.bat
    echo  3. Revisar los logs arriba
)

echo.

pause

