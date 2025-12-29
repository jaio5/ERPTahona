@echo off
chcp 65001 > nul
echo ═══════════════════════════════════════════════════════
echo   VERIFICACIÓN COMPLETA DEL SISTEMA ERP
echo ═══════════════════════════════════════════════════════
echo.

cd /d "%~dp0"

echo [1/5] Verificando conexión a MySQL...
mysql -u root -pIirne322* -e "SELECT 'MySQL OK' AS estado;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: No se puede conectar a MySQL
    echo    Verifica que MySQL esté corriendo
    pause
    exit /b 1
)
echo ✓ MySQL conectado correctamente
echo.

echo [2/5] Verificando base de datos 'tahona'...
mysql -u root -pIirne322* -e "USE tahona; SELECT 'Base de datos OK' AS estado;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: La base de datos 'tahona' no existe
    pause
    exit /b 1
)
echo ✓ Base de datos 'tahona' existe
echo.

echo [3/5] Verificando tabla 'users'...
mysql -u root -pIirne322* tahona -e "DESCRIBE users;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: La tabla 'users' no existe
    pause
    exit /b 1
)
echo ✓ Tabla 'users' existe
echo.

echo [4/5] Verificando usuario admin...
mysql -u root -pIirne322* tahona -e "SELECT username, enabled, role, bloqueado FROM users WHERE username='admin';"
if %ERRORLEVEL% NEQ 0 (
    echo ⚠ Usuario admin no encontrado, creando...
    Get-Content basesdedatos\verificar_y_crear_admin_tahona.sql | mysql -u root -pIirne322* tahona
)
echo ✓ Usuario admin verificado
echo.

echo [5/5] Verificando datos en la base de datos...
mysql -u root -pIirne322* tahona -e "SELECT 'Clientes' AS Tabla, COUNT(*) AS Registros FROM clientes UNION ALL SELECT 'Articulos', COUNT(*) FROM articulos UNION ALL SELECT 'Almacenes', COUNT(*) FROM almacenes UNION ALL SELECT 'Usuarios', COUNT(*) FROM users;"
echo.

echo ═══════════════════════════════════════════════════════
echo   ✓ VERIFICACIÓN COMPLETA
echo ═══════════════════════════════════════════════════════
echo.
echo Sistema listo para usar:
echo   • Usuario: admin
echo   • Contraseña: admin
echo   • Base de datos: tahona
echo.
echo Puedes iniciar la aplicación con:
echo   iniciar_app.bat
echo.

pause

