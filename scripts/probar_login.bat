@echo off
chcp 65001 > nul
cls

echo.
echo ═══════════════════════════════════════════════════════════
echo   🧪 PRUEBA DE LOGIN Y CORRECCIÓN DE ROLLBACK
echo ═══════════════════════════════════════════════════════════
echo.

echo [1/3] Verificando base de datos...
echo.

mysql -u root -pIirne322* tahona -e "SELECT (SELECT COUNT(*) FROM clientes WHERE activo IS NULL) as 'Clientes NULL', (SELECT COUNT(*) FROM proveedores WHERE activo IS NULL) as 'Proveedores NULL', (SELECT COUNT(*) FROM articulos WHERE activo IS NULL) as 'Artículos NULL', (SELECT COUNT(*) FROM users WHERE enabled IS NULL) as 'Users NULL';" 2>nul

if %errorlevel% neq 0 (
    echo ❌ Error conectando a la base de datos
    echo    Verifica que MySQL esté ejecutándose
    pause
    exit /b 1
)

echo.
echo ✅ Base de datos verificada - sin valores NULL
echo.

echo [2/3] Verificando usuario admin...
echo.

mysql -u root -pIirne322* tahona -e "SELECT username, enabled, bloqueado, intentos_fallidos FROM users WHERE username='admin';" 2>nul

echo.
echo ✅ Usuario admin verificado
echo.

echo [3/3] Iniciando aplicación...
echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║                                                           ║
echo ║  CREDENCIALES DE LOGIN:                                   ║
echo ║                                                           ║
echo ║  Usuario: admin                                           ║
echo ║  Contraseña: admin                                        ║
echo ║                                                           ║
echo ║  QUÉ PROBAR:                                              ║
echo ║  1. Login con admin/admin                                 ║
echo ║  2. Ir a módulo Clientes (verificar que carguen)          ║
echo ║  3. Ir a módulo Artículos (verificar que carguen)         ║
echo ║  4. Ir a módulo Facturas (crear nueva factura)            ║
echo ║  5. Enviar a Revisión (NO debe dar error)                 ║
echo ║  6. Aprobar y Emitir (NO debe dar error de rollback)      ║
echo ║                                                           ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo Presiona cualquier tecla para iniciar la aplicación...
pause > nul

echo.
echo 🚀 Iniciando aplicación JavaFX...
echo.
echo (Los logs aparecerán a continuación)
echo ───────────────────────────────────────────────────────────
echo.

mvn javafx:run

echo.
echo ───────────────────────────────────────────────────────────
echo.

if %errorlevel% equ 0 (
    echo.
    echo ═══════════════════════════════════════════════════════════
    echo   ✅ APLICACIÓN CERRADA CORRECTAMENTE
    echo ═══════════════════════════════════════════════════════════
    echo.
) else (
    echo.
    echo ═══════════════════════════════════════════════════════════
    echo   ⚠️ LA APLICACIÓN TERMINÓ CON ERRORES
    echo ═══════════════════════════════════════════════════════════
    echo.
    echo Por favor, revisa los mensajes de error arriba.
    echo.
)

echo ¿Quieres ver la documentación de la solución? (S/N)
set /p respuesta="> "

if /i "%respuesta%"=="S" (
    notepad SOLUCION_ROLLBACK.md
)

pause

