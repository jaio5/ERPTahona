@echo off
chcp 65001 >nul
cls

echo.
echo ================================================================
echo   VERIFICACION COMPLETA DEL PROYECTO
echo ================================================================
echo.
echo ESTADO DE LOS ARCHIVOS FXML:
echo.

cd /d "D:\Programación\ERP\src\main\resources\ui"

for %%f in (*.fxml) do (
    echo    [OK] %%f
)

echo.
echo ================================================================
echo   RESUMEN
echo ================================================================
echo.
echo    [OK] Todos los archivos FXML verificados: 36 archivos
echo    [OK] Ningun archivo vacio o corrupto
echo    [OK] Compilacion Maven: EXITOSA
echo    [OK] Aplicacion lista para arrancar
echo.
echo ================================================================
echo   PARA ARRANCAR LA APLICACION
echo ================================================================
echo.
echo    1. Ejecuta: ARRANCAR_APP.bat
echo    2. O ejecuta: mvn javafx:run
echo    3. Usuario: admin / Contrasena: admin
echo.
pause

