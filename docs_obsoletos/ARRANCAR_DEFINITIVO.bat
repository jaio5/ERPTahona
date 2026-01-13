@echo off
chcp 65001 > nul
cls

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║          🚀 ARRANQUE DEFINITIVO - ERP TAHONA 🚀              ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

REM ============================================================
REM PASO 1: Verificar MySQL
REM ============================================================
echo [1/5] Verificando MySQL...
echo.

net start MySQL80 > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ MySQL iniciado correctamente
) else (
    sc query MySQL80 | findstr "RUNNING" > nul
    if %ERRORLEVEL% EQU 0 (
        echo ✅ MySQL ya estaba corriendo
    ) else (
        echo.
        echo ❌ ERROR: MySQL no está disponible
        echo.
        echo Soluciones posibles:
        echo 1. Abrir Servicios de Windows (services.msc)
        echo 2. Buscar "MySQL80"
        echo 3. Iniciar el servicio
        echo.
        echo O ejecutar como Administrador:
        echo    net start MySQL80
        echo.
        pause
        exit /b 1
    )
)
echo.

REM ============================================================
REM PASO 2: Verificar Java
REM ============================================================
echo [2/5] Verificando Java...
java -version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java no encontrado
    pause
    exit /b 1
)
echo ✅ Java OK
echo.

REM ============================================================
REM PASO 3: Verificar Maven
REM ============================================================
echo [3/5] Verificando Maven...
mvn -version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven no encontrado
    pause
    exit /b 1
)
echo ✅ Maven OK
echo.

REM ============================================================
REM PASO 4: Compilar Proyecto
REM ============================================================
echo [4/5] Compilando proyecto...
echo Por favor, espera (puede tardar 1-2 minutos)...
echo.

call mvn clean compile -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: Falló la compilación
    echo.
    echo Mostrando errores:
    echo ═══════════════════════════════════════════════════════════
    call mvn compile -DskipTests
    echo ═══════════════════════════════════════════════════════════
    echo.
    pause
    exit /b 1
)

echo ✅ Compilación exitosa
echo.

REM ============================================================
REM PASO 5: Arrancar Aplicación
REM ============================================================
echo [5/5] Arrancando aplicación JavaFX...
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║  La ventana de login debería aparecer en unos segundos...   ║
echo ║                                                              ║
echo ║  Usuario: admin                                             ║
echo ║  Contraseña: admin                                          ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

call mvn javafx:run

REM ============================================================
REM CIERRE
REM ============================================================
echo.
echo ═══════════════════════════════════════════════════════════════
if %ERRORLEVEL% EQU 0 (
    echo   ✅ La aplicación se cerró correctamente
) else (
    echo   ⚠️  La aplicación terminó con código: %ERRORLEVEL%
    echo.
    echo   Consulta: GUIA_RAPIDA_ARRANQUE.md
)
echo ═══════════════════════════════════════════════════════════════
echo.
pause

