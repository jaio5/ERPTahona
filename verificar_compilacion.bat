@echo off
echo ===========================================
echo VERIFICANDO COMPILACION DEL ERP
echo ===========================================
echo.

cd /d "D:\Programación\ERP"

echo Limpiando proyecto...
call mvn clean -q

echo.
echo Compilando proyecto...
call mvn compile -DskipTests -q

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo   COMPILACION EXITOSA - PROYECTO LISTO
    echo ============================================
    echo.
    echo El proyecto compila correctamente.
    echo Puede ejecutar con: mvn javafx:run
) else (
    echo.
    echo ============================================
    echo   ERROR EN COMPILACION
    echo ============================================
    echo.
    echo Ejecute: mvn compile -DskipTests
    echo para ver los errores detallados.
)

echo.
pause

