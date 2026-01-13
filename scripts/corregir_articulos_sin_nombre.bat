@echo off
chcp 65001 >nul
color 0A
cls

echo.
echo ═══════════════════════════════════════════════════════════
echo   🔧 CORREGIR ARTÍCULOS SIN NOMBRE EN LA BASE DE DATOS
echo ═══════════════════════════════════════════════════════════
echo.
echo Este script actualizará los artículos que tienen nombre NULL
echo y les asignará su código como nombre.
echo.
echo Presiona cualquier tecla para continuar o CTRL+C para cancelar...
pause >nul

echo.
echo 🔍 Verificando artículos problemáticos...
echo.
mysql -u root -pIirne322* tahona -e "SELECT COUNT(*) as articulos_sin_nombre FROM articulos WHERE nombre IS NULL OR nombre = '';"

echo.
echo 📋 Primeros 10 artículos sin nombre:
echo.
mysql -u root -pIirne322* tahona -e "SELECT id, codigo, nombre FROM articulos WHERE nombre IS NULL OR nombre = '' LIMIT 10;"

echo.
echo.
echo ⚠️  ¿Deseas corregir estos artículos? (S/N)
set /p CONFIRMAR="> "

if /i "%CONFIRMAR%" NEQ "S" (
    echo.
    echo ❌ Operación cancelada
    echo.
    pause
    exit /b
)

echo.
echo 🔧 Aplicando correcciones...
echo.
mysql -u root -pIirne322* tahona -e "UPDATE articulos SET nombre = codigo WHERE nombre IS NULL OR nombre = '';"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Correcciones aplicadas exitosamente
    echo.
    echo 📊 Verificando resultados:
    echo.
    mysql -u root -pIirne322* tahona -e "SELECT COUNT(*) as articulos_sin_nombre FROM articulos WHERE nombre IS NULL OR nombre = '';"
    echo.
    echo 📋 Primeros 10 artículos corregidos:
    echo.
    mysql -u root -pIirne322* tahona -e "SELECT id, codigo, nombre FROM articulos WHERE activo = 1 LIMIT 10;"
    echo.
    echo ═══════════════════════════════════════════════════════════
    echo   ✅ PROCESO COMPLETADO
    echo ═══════════════════════════════════════════════════════════
) else (
    echo.
    echo ❌ Error al aplicar correcciones
    echo.
)

echo.
pause

