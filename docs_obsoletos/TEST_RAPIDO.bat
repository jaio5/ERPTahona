@echo off
chcp 65001 >nul
cls

echo.
echo ================================================================
echo   TEST RAPIDO - ERP PANADERIA TAHONA
echo ================================================================
echo.

cd /d "D:\Programación\ERP"

echo Verificando requisitos...
echo.

REM Verificar Java
echo [1/4] Java...
java -version 2>nul
if %errorlevel% neq 0 (
    echo    ERROR: Java NO instalado
    echo    Instala Java 17 o superior
    pause
    exit /b 1
) else (
    echo    OK: Java instalado
)

REM Verificar Maven
echo [2/4] Maven...
mvn -version 2>nul | findstr "Apache Maven" >nul
if %errorlevel% neq 0 (
    echo    ERROR: Maven NO instalado
    echo    Instala Maven 3.6 o superior
    pause
    exit /b 1
) else (
    echo    OK: Maven instalado
)

REM Verificar MySQL
echo [3/4] MySQL...
netstat -an | findstr ":3306" >nul
if %errorlevel% neq 0 (
    echo    ADVERTENCIA: MySQL no detectado en puerto 3306
    echo    Asegurate de que MySQL este corriendo
    set /p continuar="Continuar de todas formas? (S/N): "
    if /i not "%continuar%"=="S" exit /b 1
) else (
    echo    OK: MySQL detectado
)

REM Verificar base de datos
echo [4/4] Base de datos tahona...
mysql -u root -pIirne322* -e "USE tahona;" 2>nul
if %errorlevel% neq 0 (
    echo    ADVERTENCIA: Base de datos 'tahona' no encontrada
    echo    Crea la BD e importa tahonaerp.sql
    set /p continuar="Continuar de todas formas? (S/N): "
    if /i not "%continuar%"=="S" exit /b 1
) else (
    echo    OK: Base de datos 'tahona' existe
)

echo.
echo ================================================================
echo   TODOS LOS REQUISITOS OK - Arrancando aplicacion...
echo ================================================================
echo.
echo CREDENCIALES DE ACCESO:
echo    Usuario:    admin
echo    Contrasena: admin
echo.
echo La aplicacion se esta iniciando...
echo    (Esto puede tardar 30-40 segundos)
echo.
echo ================================================================
echo.

mvn javafx:run

echo.
echo ================================================================
echo   La aplicacion ha finalizado
echo ================================================================
echo.
pause

