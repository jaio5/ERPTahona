@echo off
REM Script para arrancar ERP en Windows en ventana separada y guardar logs en run.log
REM Ejecuta desde el directorio del repo: tools\run_windows.cmd
cd /d "%~dp0\.."
echo Iniciando ERP desde: %CD%
start "ERP" cmd /c "java -jar target\ERP-0.0.1-SNAPSHOT.jar > "%CD%\run.log" 2>&1"
echo Aplicacion iniciada en ventana separada. Revisa %CD%\run.log para logs.
pause

