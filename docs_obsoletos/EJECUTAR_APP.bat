@echo off
chcp 65001 >nul
cls
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║  🏢 ERP PANADERÍA TAHONA - EJECUTOR DIRECTO                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo [INFO] Verificando Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java no encontrado. Descarga Java 17 desde: https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Verificando MySQL...
mysql --version >nul 2>&1
if errorlevel 1 (
    echo [WARN] MySQL CLI no encontrado, pero puede estar corriendo como servicio
)

echo.
echo ═══════════════════════════════════════════════════════════════
echo   INTENTANDO COMPILAR Y EJECUTAR CON INTELLIJ BUILD
echo ═══════════════════════════════════════════════════════════════
echo.

REM Buscar instalación de IntelliJ
set IDEA_HOME=
for %%i in (
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3"
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.3"
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.2"
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.2"
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.3"
) do (
    if exist %%i\bin\idea64.exe (
        set IDEA_HOME=%%i
        goto :found
    )
)

:found
if defined IDEA_HOME (
    echo [OK] IntelliJ encontrado en: %IDEA_HOME%
    echo.
    echo Abriendo IntelliJ IDEA...
    start "" "%IDEA_HOME%\bin\idea64.exe" "%~dp0"

    echo.
    echo ╔══════════════════════════════════════════════════════════════╗
    echo ║  ✅ INSTRUCCIONES EN INTELLIJ IDEA:                          ║
    echo ╠══════════════════════════════════════════════════════════════╣
    echo ║                                                              ║
    echo ║  1. Espera a que IntelliJ cargue el proyecto                ║
    echo ║  2. Si pregunta "Trust Project" → Click en TRUST             ║
    echo ║                                                              ║
    echo ║  3. CONFIGURACIÓN INICIAL (solo primera vez):                ║
    echo ║     - File → Settings (Ctrl+Alt+S)                           ║
    echo ║     - Plugins → Busca "Lombok" → Install                     ║
    echo ║     - Build → Compiler → Annotation Processors               ║
    echo ║       ☑ Enable annotation processing                         ║
    echo ║     - Apply → OK                                             ║
    echo ║     - Click derecho en pom.xml → Maven → Reload              ║
    echo ║                                                              ║
    echo ║  4. EJECUTAR LA APLICACIÓN:                                  ║
    echo ║     - Navega a: src/main/java/alicanteweb/erp/               ║
    echo ║       ErpLauncher.java                                        ║
    echo ║     - Click en el icono ▶️ verde                             ║
    echo ║     - Selecciona "Run 'ErpLauncher.main()'"                  ║
    echo ║                                                              ║
    echo ║  5. LOGIN:                                                   ║
    echo ║     Usuario: admin                                           ║
    echo ║     Contraseña: admin                                        ║
    echo ║                                                              ║
    echo ╚══════════════════════════════════════════════════════════════╝

) else (
    echo [ERROR] IntelliJ IDEA no encontrado
    echo.
    echo ╔══════════════════════════════════════════════════════════════╗
    echo ║  ❌ INTELLIJ IDEA NO INSTALADO                               ║
    echo ╠══════════════════════════════════════════════════════════════╣
    echo ║                                                              ║
    echo ║  Maven no puede compilar debido a Lombok                     ║
    echo ║                                                              ║
    echo ║  ✅ SOLUCIÓN: Instalar IntelliJ IDEA                         ║
    echo ║                                                              ║
    echo ║  1. Descarga IntelliJ IDEA Community Edition (GRATIS):       ║
    echo ║     https://www.jetbrains.com/idea/download/                 ║
    echo ║                                                              ║
    echo ║  2. Instala la versión Community Edition                     ║
    echo ║                                                              ║
    echo ║  3. Ejecuta este script de nuevo                             ║
    echo ║                                                              ║
    echo ╚══════════════════════════════════════════════════════════════╝

    echo.
    set /p abrir="¿Abrir página de descarga de IntelliJ? (S/N): "
    if /i "%abrir%"=="S" (
        start https://www.jetbrains.com/idea/download/
    )
)

echo.
pause

