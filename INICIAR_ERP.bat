@echo off
chcp 65001 >nul
cls

echo ╔════════════════════════════════════════════╗
echo ║                                            ║
echo ║   🏢 ERP PANADERÍA TAHONA                  ║
echo ║   Versión 0.0.1                            ║
echo ║                                            ║
echo ╚════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo [1/4] Verificando Java...
java -version 2>nul
if errorlevel 1 (
    echo ❌ ERROR: Java no está instalado o no está en el PATH
    echo.
    echo Descarga Java 17 desde: https://adoptium.net/
    pause
    exit /b 1
)
echo ✅ Java encontrado
echo.

echo [2/4] Verificando Maven...
mvn -version 2>nul
if errorlevel 1 (
    echo ⚠️  ADVERTENCIA: Maven no encontrado
    echo.
    echo Para ejecutar con Maven, descárgalo desde: https://maven.apache.org/
    echo.
    echo 💡 RECOMENDACIÓN: Usa IntelliJ IDEA en su lugar
    echo    1. Abre IntelliJ IDEA
    echo    2. Abre el proyecto: D:\Programación\ERP
    echo    3. Ejecuta ErpLauncher.java
    echo.
    pause
    exit /b 1
)
echo ✅ Maven encontrado
echo.

echo [3/4] Verificando MySQL...
mysql -u root --version 2>nul
if errorlevel 1 (
    echo ⚠️  ADVERTENCIA: No se pudo verificar MySQL
    echo    Asegúrate de que MySQL está corriendo y la BD 'tahona' existe
)
echo.

echo ╔════════════════════════════════════════════╗
echo ║  ⚠️  ADVERTENCIA IMPORTANTE                ║
echo ╠════════════════════════════════════════════╣
echo ║                                            ║
echo ║  Maven puede fallar con Lombok             ║
echo ║                                            ║
echo ║  ✅ SOLUCIÓN RECOMENDADA:                  ║
echo ║     Ejecutar desde IntelliJ IDEA           ║
echo ║                                            ║
echo ║  Si Maven falla, lee:                      ║
echo ║  SOLUCION_DEFINITIVA_LOMBOK.md             ║
echo ║                                            ║
echo ╚════════════════════════════════════════════╝
echo.
echo ¿Deseas intentar ejecutar con Maven de todos modos? (S/N)
set /p continuar="Respuesta: "

if /i "%continuar%" NEQ "S" (
    echo.
    echo 💡 Abre el proyecto en IntelliJ IDEA y ejecuta ErpLauncher.java
    echo.
    pause
    exit /b 0
)

echo.
echo [4/4] Iniciando aplicación...
echo.
echo ═══════════════════════════════════════════════════════
mvn javafx:run -DskipTests

if errorlevel 1 (
    echo.
    echo.
    echo ╔════════════════════════════════════════════╗
    echo ║  ❌ ERROR AL COMPILAR                      ║
    echo ╠════════════════════════════════════════════╣
    echo ║                                            ║
    echo ║  El problema es con Lombok                 ║
    echo ║                                            ║
    echo ║  ✅ SOLUCIÓN:                              ║
    echo ║                                            ║
    echo ║  1. Abre IntelliJ IDEA                     ║
    echo ║  2. Abre el proyecto en:                   ║
    echo ║     D:\Programación\ERP                    ║
    echo ║  3. Ve a:                                  ║
    echo ║     src/main/java/alicanteweb/erp/         ║
    echo ║     ErpLauncher.java                       ║
    echo ║  4. Click derecho → Run                    ║
    echo ║                                            ║
    echo ║  Lee: SOLUCION_DEFINITIVA_LOMBOK.md        ║
    echo ║       para instrucciones detalladas        ║
    echo ║                                            ║
    echo ╚════════════════════════════════════════════╝
    echo.
    pause
    exit /b 1
)

echo.
echo ╔════════════════════════════════════════════╗
echo ║  ✅ APLICACIÓN CERRADA                     ║
echo ╚════════════════════════════════════════════╝
echo.
pause

