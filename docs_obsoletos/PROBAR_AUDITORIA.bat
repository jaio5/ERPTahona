@echo off
chcp 65001 > nul
cls
echo ================================================================
echo   🔧 PRUEBA DE VISTA DE AUDITORÍA
echo ================================================================
echo.
echo 📋 Pasos que se ejecutarán:
echo   1. Verificar archivos
echo   2. Insertar datos de prueba
echo   3. Compilar proyecto
echo   4. Arrancar aplicación
echo.
pause

echo.
echo ================================================================
echo   PASO 1: Verificando archivos...
echo ================================================================
if exist "src\main\resources\ui\auditoria_panel.fxml" (
    echo ✅ auditoria_panel.fxml existe
) else (
    echo ❌ auditoria_panel.fxml NO existe
    pause
    exit /b 1
)

if exist "src\main\java\alicanteweb\erp\controller\AuditoriaController.java" (
    echo ✅ AuditoriaController.java existe
) else (
    echo ❌ AuditoriaController.java NO existe
    pause
    exit /b 1
)

echo.
echo ================================================================
echo   PASO 2: Insertando datos de prueba...
echo ================================================================
mysql -u root -pIirne322* tahona -e "INSERT IGNORE INTO auditoria_acciones (usuario_nombre, tipo_accion, fecha, descripcion, modulo, resultado) VALUES ('admin', 'LOGIN', NOW(), 'Login de prueba', 'AUTENTICACION', 'EXITO');"
if %ERRORLEVEL% EQU 0 (
    echo ✅ Datos de prueba insertados
) else (
    echo ⚠️ Advertencia: No se pudieron insertar datos de prueba
)

echo.
echo ================================================================
echo   PASO 3: Compilando proyecto...
echo ================================================================
call mvn clean compile -DskipTests -q
if %ERRORLEVEL% EQU 0 (
    echo ✅ Compilación exitosa
) else (
    echo ❌ Error en compilación
    pause
    exit /b 1
)

echo.
echo ================================================================
echo   PASO 4: Arrancando aplicación...
echo ================================================================
echo.
echo 🎯 INSTRUCCIONES:
echo   1. Espera a que cargue la aplicación
echo   2. Login: admin / admin
echo   3. Click en menú "💼 Más"
echo   4. Click en "📋 Auditoría"
echo   5. Observa si la vista carga correctamente
echo.
echo Si ves un error, copia TODO el texto de la consola
echo.
pause

call mvn javafx:run

echo.
echo ================================================================
echo   Aplicación cerrada
echo ================================================================
pause

