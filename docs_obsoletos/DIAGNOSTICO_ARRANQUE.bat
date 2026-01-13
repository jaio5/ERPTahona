@echo off
echo.
echo ═══════════════════════════════════════════════════════════════
echo   DIAGNÓSTICO DE LA APLICACIÓN ERP
echo ═══════════════════════════════════════════════════════════════
echo.

echo [1/5] Verificando Java...
java -version
echo.

echo [2/5] Verificando Maven...
mvn -version
echo.

echo [3/5] Limpiando proyecto...
call mvn clean
echo.

echo [4/5] Compilando proyecto...
call mvn compile -DskipTests
echo.

echo [5/5] Intentando arrancar...
call mvn javafx:run
echo.

echo ═══════════════════════════════════════════════════════════════
echo   FIN DEL DIAGNÓSTICO
echo ═══════════════════════════════════════════════════════════════
pause

