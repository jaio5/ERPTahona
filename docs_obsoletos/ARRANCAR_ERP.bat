@echo off
chcp 65001 > nul
cls

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║          🚀 ARRANCANDO ERP PANADERÍA TAHONA 🚀               ║
echo ║                  VERSION 1.3.0                               ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo [1/5] Verificando Java...
java -version 2>&1 | findstr "version" > nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: Java no está instalado o no está en PATH
    echo.
    echo Por favor, instala Java 17 desde:
    echo https://adoptium.net/
    pause
    exit /b 1
)
echo ✅ Java detectado
echo.

echo [2/5] Verificando Maven...
mvn -version 2>&1 | findstr "Maven" > nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: Maven no está instalado o no está en PATH
    pause
    exit /b 1
)
echo ✅ Maven detectado
echo.

echo [3/5] Limpiando proyecto anterior...
call mvn clean -q
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️  Advertencia: Error al limpiar
)
echo ✅ Proyecto limpiado
echo.

echo [4/5] Compilando proyecto (sin tests)...
echo Por favor, espera... esto puede tardar 1-2 minutos
call mvn compile -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: Falló la compilación
    echo.
    echo Mostrando errores detallados:
    echo ═══════════════════════════════════════════════════════════
    call mvn compile -DskipTests
    echo ═══════════════════════════════════════════════════════════
    echo.
    echo Soluciones posibles:
    echo 1. Verifica que la base de datos MySQL esté corriendo
    echo 2. Ejecuta: net start MySQL80
    echo 3. Verifica application.properties
    echo.
    pause
    exit /b 1
)
echo ✅ Compilación exitosa
echo.

echo [5/5] Iniciando aplicación JavaFX...
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║  La ventana de la aplicación debería abrirse en breve...    ║
echo ║  Si no aparece, presiona Ctrl+C y revisa los logs          ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call mvn javafx:run

echo.
echo ═══════════════════════════════════════════════════════════════
echo   LA APLICACIÓN SE HA CERRADO
echo ═══════════════════════════════════════════════════════════════
echo.

if %ERRORLEVEL% NEQ 0 (
    echo ❌ La aplicación terminó con errores
    echo Código de salida: %ERRORLEVEL%
    echo.
    echo Consulta: SOLUCION_PROBLEMAS_ARRANQUE.md
) else (
    echo ✅ La aplicación se cerró correctamente
)

echo.
pause

