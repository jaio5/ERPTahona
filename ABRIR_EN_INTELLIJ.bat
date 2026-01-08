@echo off
chcp 65001 >nul
cls

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║                                                        ║
echo ║   🏢 ERP PANADERÍA TAHONA - LANZADOR RÁPIDO           ║
echo ║                                                        ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo.
echo ⚠️  Maven no puede compilar debido a problemas con Lombok
echo.
echo ✅  SOLUCIÓN: Abrir en IntelliJ IDEA
echo.
echo.

REM Buscar IntelliJ IDEA
set IDEA_PATH=""
if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\idea64.exe" (
    set IDEA_PATH="C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3\bin\idea64.exe"
)
if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2024.3\bin\idea64.exe" (
    set IDEA_PATH="C:\Program Files\JetBrains\IntelliJ IDEA 2024.3\bin\idea64.exe"
)
if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.2\bin\idea64.exe" (
    set IDEA_PATH="C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.2\bin\idea64.exe"
)
if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2024.2\bin\idea64.exe" (
    set IDEA_PATH="C:\Program Files\JetBrains\IntelliJ IDEA 2024.2\bin\idea64.exe"
)
if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.3\bin\idea64.exe" (
    set IDEA_PATH="C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2023.3\bin\idea64.exe"
)

if %IDEA_PATH% NEQ "" (
    echo ✅ IntelliJ IDEA encontrado
    echo.
    echo 🚀 Abriendo IntelliJ IDEA con el proyecto...
    echo.
    start %IDEA_PATH% "%~dp0"
    echo.
    echo ╔════════════════════════════════════════════════════════╗
    echo ║  PASOS A SEGUIR EN INTELLIJ:                           ║
    echo ╠════════════════════════════════════════════════════════╣
    echo ║                                                        ║
    echo ║  1. Espera a que IntelliJ cargue el proyecto           ║
    echo ║  2. Ve a: Settings → Plugins                           ║
    echo ║  3. Busca "Lombok" y asegúrate que esté instalado      ║
    echo ║  4. Ve a: Settings → Annotation Processors             ║
    echo ║  5. Marca: Enable annotation processing                ║
    echo ║  6. Abre: ErpLauncher.java                             ║
    echo ║  7. Click derecho → Run ErpLauncher.main               ║
    echo ║                                                        ║
    echo ║  Credenciales:                                         ║
    echo ║    Usuario: admin                                      ║
    echo ║    Contraseña: admin                                   ║
    echo ║                                                        ║
    echo ╚════════════════════════════════════════════════════════╝
    echo.
) else (
    echo ❌ IntelliJ IDEA no encontrado
    echo.
    echo 📥 DESCARGA IntelliJ IDEA Community Edition (GRATIS):
    echo    https://www.jetbrains.com/idea/download/
    echo.
    echo 📋 DESPUÉS DE INSTALAR:
    echo.
    echo    1. Ejecuta este script de nuevo
    echo       O
    echo    2. Abre IntelliJ manualmente
    echo    3. File → Open → D:\Programación\ERP
    echo    4. Settings → Plugins → Instala "Lombok"
    echo    5. Settings → Annotation Processors → Enable
    echo    6. Ejecuta ErpLauncher.java
    echo.
    echo.
    echo 💡 ALTERNATIVA: Abrir manualmente
    echo.
    set /p abrir="¿Quieres abrir la carpeta del proyecto? (S/N): "
    if /i "%abrir%"=="S" (
        start "" "%~dp0"
        echo.
        echo ✅ Carpeta abierta. Abre IntelliJ y carga este proyecto.
    )
)

echo.
echo.
pause

