@echo off
chcp 65001 >nul
cls

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                                                            ║
echo ║   🏢 ERP PANADERÍA TAHONA - ARRANQUE AUTOMÁTICO            ║
echo ║                                                            ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo ═══════════════════════════════════════════════════════════════
echo   DIAGNÓSTICO DEL PROBLEMA
echo ═══════════════════════════════════════════════════════════════
echo.
echo ❌ MAVEN NO PUEDE COMPILAR
echo    Razón: Lombok no procesa anotaciones correctamente
echo.
echo ✅ SOLUCIÓN: Ejecutar desde IntelliJ IDEA
echo    IntelliJ tiene compilador propio que SÍ funciona
echo.
echo ═══════════════════════════════════════════════════════════════
echo.

REM Verificar Java
echo [1/3] Verificando Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo    ❌ Java no encontrado
    echo    📥 Descarga Java 17: https://adoptium.net/
    pause
    exit /b 1
)
echo    ✅ Java OK
echo.

REM Verificar MySQL
echo [2/3] Verificando MySQL...
sc query MySQL80 | find "RUNNING" >nul 2>&1
if errorlevel 1 (
    echo    ⚠️  MySQL no está corriendo o no está instalado
    echo    💡 Asegúrate de iniciar MySQL antes de usar la app
) else (
    echo    ✅ MySQL corriendo
)
echo.

REM Buscar IntelliJ
echo [3/3] Buscando IntelliJ IDEA...
set IDEA_EXE=
for %%D in (
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\idea64.exe"
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.3\bin\idea64.exe"
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.2\bin\idea64.exe"
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.2\bin\idea64.exe"
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1\bin\idea64.exe"
    "C:\Program Files\JetBrains\IntelliJ IDEA 2024.1\bin\idea64.exe"
    "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.3\bin\idea64.exe"
) do (
    if exist %%D (
        set IDEA_EXE=%%D
        goto :idea_found
    )
)

:idea_found
if defined IDEA_EXE (
    echo    ✅ IntelliJ encontrado
    echo.
    echo ═══════════════════════════════════════════════════════════════
    echo   ABRIENDO INTELLIJ IDEA...
    echo ═══════════════════════════════════════════════════════════════
    echo.

    start "" %IDEA_EXE% "%~dp0"

    timeout /t 3 /nobreak >nul

    echo.
    echo ╔════════════════════════════════════════════════════════════╗
    echo ║                                                            ║
    echo ║  ✅ INTELLIJ IDEA ABIERTO                                  ║
    echo ║                                                            ║
    echo ║  📋 SIGUE ESTOS PASOS:                                     ║
    echo ║                                                            ║
    echo ║  PRIMERA VEZ (Configuración - 3 minutos):                  ║
    echo ║  ────────────────────────────────────────                  ║
    echo ║  1. Espera a que IntelliJ indexe el proyecto              ║
    echo ║     (barra de progreso abajo a la derecha)                ║
    echo ║                                                            ║
    echo ║  2. Si pregunta "Trust Project" → Click TRUST              ║
    echo ║                                                            ║
    echo ║  3. Configura Lombok:                                      ║
    echo ║     • File → Settings (Ctrl+Alt+S)                         ║
    echo ║     • Plugins → Busca "Lombok" → Install                   ║
    echo ║     • Restart si es necesario                              ║
    echo ║     • Build → Compiler → Annotation Processors             ║
    echo ║       ☑ Enable annotation processing                       ║
    echo ║     • Apply → OK                                           ║
    echo ║                                                            ║
    echo ║  4. Recarga Maven:                                         ║
    echo ║     • Click derecho en pom.xml                             ║
    echo ║     • Maven → Reload project                               ║
    echo ║     • Espera a que descargue dependencias                  ║
    echo ║                                                            ║
    echo ║  ────────────────────────────────────────────              ║
    echo ║                                                            ║
    echo ║  EJECUTAR (30 segundos):                                   ║
    echo ║  ────────────────────────────────────────                  ║
    echo ║  1. En el explorador (izquierda), navega a:                ║
    echo ║     src → main → java → alicanteweb → erp                  ║
    echo ║                                                            ║
    echo ║  2. Abre: ErpLauncher.java                                 ║
    echo ║                                                            ║
    echo ║  3. Click en el icono ▶️ verde junto a:                    ║
    echo ║     public class ErpLauncher                               ║
    echo ║                                                            ║
    echo ║  4. Selecciona: "Run 'ErpLauncher.main()'"                 ║
    echo ║                                                            ║
    echo ║  ────────────────────────────────────────────              ║
    echo ║                                                            ║
    echo ║  CREDENCIALES DE LOGIN:                                    ║
    echo ║     Usuario: admin                                         ║
    echo ║     Contraseña: admin                                      ║
    echo ║                                                            ║
    echo ╚════════════════════════════════════════════════════════════╝
    echo.
    echo 💡 Tip: Una vez configurado, solo necesitas ejecutar
    echo    ErpLauncher.java cada vez que quieras usar la app
    echo.
    echo 📚 Documentación adicional:
    echo    • README_IMPORTANTE.md
    echo    • SOLUCION_DEFINITIVA_LOMBOK.md
    echo.

) else (
    echo    ❌ IntelliJ IDEA no encontrado
    echo.
    echo ╔════════════════════════════════════════════════════════════╗
    echo ║                                                            ║
    echo ║  ❌ INTELLIJ IDEA NO INSTALADO                             ║
    echo ║                                                            ║
    echo ║  Maven NO puede compilar este proyecto.                    ║
    echo ║  IntelliJ IDEA es OBLIGATORIO.                             ║
    echo ║                                                            ║
    echo ║  📥 DESCARGA INTELLIJ IDEA (GRATIS):                       ║
    echo ║                                                            ║
    echo ║     https://www.jetbrains.com/idea/download/               ║
    echo ║                                                            ║
    echo ║  Selecciona: Community Edition (gratuita)                  ║
    echo ║  Tamaño: ~900 MB                                           ║
    echo ║  Tiempo: 5-7 minutos                                       ║
    echo ║                                                            ║
    echo ║  DESPUÉS DE INSTALAR:                                      ║
    echo ║  1. Ejecuta este script de nuevo                           ║
    echo ║  2. Sigue los pasos que aparecerán                         ║
    echo ║  3. ¡Tu app estará corriendo!                              ║
    echo ║                                                            ║
    echo ╚════════════════════════════════════════════════════════════╝
    echo.

    set /p descargar="¿Abrir página de descarga ahora? (S/N): "
    if /i "%descargar%"=="S" (
        echo Abriendo navegador...
        start https://www.jetbrains.com/idea/download/
    )

    echo.
    set /p carpeta="¿Abrir carpeta del proyecto? (S/N): "
    if /i "%carpeta%"=="S" (
        start "" "%~dp0"
        echo.
        echo 💡 Luego de instalar IntelliJ:
        echo    File → Open → Selecciona esta carpeta
    )
)

echo.
pause

