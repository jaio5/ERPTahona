@echo off
chcp 65001 > nul
cls

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo    ✅ VERIFICACIÓN FINAL DEL SISTEMA ERP
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM Verificar directorio
if not exist "pom.xml" (
    echo ❌ ERROR: Ejecuta este script desde la raíz del proyecto
    pause
    exit /b 1
)

set ERRORES=0

echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  1. COMPILACIÓN                                                         │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

echo Compilando el proyecto...
call mvn clean compile -q
if errorlevel 1 (
    echo ❌ Error en compilación
    set /a ERRORES+=1
) else (
    echo ✅ Compilación exitosa
)
echo.

echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  2. BASE DE DATOS                                                       │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

set DB_USER=root
set DB_PASS=Iirne322*
set DB_NAME=tahona

echo Verificando conexión a MySQL...
mysql -u %DB_USER% -p%DB_PASS% -e "SELECT 1;" 2>nul
if errorlevel 1 (
    echo ❌ No se puede conectar a MySQL
    set /a ERRORES+=1
) else (
    echo ✅ Conexión a MySQL OK
)

echo Verificando base de datos tahona...
mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT 1;" 2>nul
if errorlevel 1 (
    echo ❌ Base de datos 'tahona' no existe
    set /a ERRORES+=1
) else (
    echo ✅ Base de datos 'tahona' existe
)
echo.

echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  3. TABLAS Y DATOS                                                      │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

for /f %%i in ('mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT COUNT(*) FROM users;" -s -N 2^>nul') do set USER_COUNT=%%i
for /f %%i in ('mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT COUNT(*) FROM clientes;" -s -N 2^>nul') do set CLIENT_COUNT=%%i
for /f %%i in ('mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT COUNT(*) FROM articulos;" -s -N 2^>nul') do set ARTICULO_COUNT=%%i
for /f %%i in ('mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT COUNT(*) FROM facturas;" -s -N 2^>nul') do set FACTURA_COUNT=%%i
for /f %%i in ('mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT COUNT(*) FROM empresa_config;" -s -N 2^>nul') do set EMPRESA_COUNT=%%i

echo Usuarios: %USER_COUNT%
if %USER_COUNT% GTR 0 (
    echo ✅ Hay usuarios en el sistema
) else (
    echo ⚠️  No hay usuarios creados
    set /a ERRORES+=1
)

echo Clientes: %CLIENT_COUNT%
if %CLIENT_COUNT% GTR 0 (
    echo ✅ Hay clientes en el sistema
) else (
    echo ⚠️  No hay clientes
)

echo Artículos: %ARTICULO_COUNT%
if %ARTICULO_COUNT% GTR 0 (
    echo ✅ Hay artículos en el sistema
) else (
    echo ⚠️  No hay artículos
)

echo Facturas: %FACTURA_COUNT%
echo Configuración empresa: %EMPRESA_COUNT%

if %EMPRESA_COUNT% GTR 0 (
    echo ✅ Hay configuración de empresa
) else (
    echo ⚠️  No hay configuración de empresa
    echo    Ejecuta: scripts\configurar_verifactu.bat
)
echo.

echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  4. ARCHIVOS NECESARIOS                                                 │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

if exist "src\main\resources\application.properties" (
    echo ✅ application.properties existe
) else (
    echo ❌ application.properties NO existe
    set /a ERRORES+=1
)

if exist "target\classes" (
    echo ✅ Proyecto compilado (target\classes existe)
) else (
    echo ⚠️  Proyecto no compilado
)

if exist "src\main\resources\certs\mi_certificado.p12" (
    echo ✅ Certificado digital encontrado
    for %%A in ("src\main\resources\certs\mi_certificado.p12") do set CERT_SIZE=%%~zA
    if !CERT_SIZE! LSS 1000 (
        echo    ⚠️  Advertencia: Certificado muy pequeño (!CERT_SIZE! bytes^)
        echo    Puede que no sea un certificado válido
    ) else (
        echo    Tamaño: !CERT_SIZE! bytes
    )
) else (
    echo ⚠️  Certificado digital NO encontrado
    echo    Para producción, necesitas un certificado de la FNMT
    echo    Ver: documentacion\VERIFACTU_COMPLETO.md
)
echo.

echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  5. CONFIGURACIÓN VERIFACTU                                             │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

findstr /C:"verifactu.aeat.enabled=true" src\main\resources\application.properties >nul
if errorlevel 1 (
    echo ⚠️  Verifactu DESHABILITADO (verifactu.aeat.enabled=false)
    echo    Las facturas NO se enviarán a la AEAT
    echo    Solo se guardarán evidencias locales
) else (
    echo ✅ Verifactu HABILITADO

    findstr /C:"prewww2.aeat.es" src\main\resources\application.properties >nul
    if errorlevel 1 (
        echo    🚀 Entorno: PRODUCCIÓN
        echo    ⚠️  CUIDADO: Las facturas se enviarán a la AEAT REAL
    ) else (
        echo    🧪 Entorno: PREPRODUCCIÓN (pruebas)
    )
)
echo.

echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  6. DOCUMENTACIÓN                                                       │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

set DOC_COUNT=0
if exist "documentacion\VERIFACTU_COMPLETO.md" set /a DOC_COUNT+=1
if exist "documentacion\GUIA_USUARIO.md" set /a DOC_COUNT+=1
if exist "documentacion\GUIA_CONFIGURACION_AEAT.md" set /a DOC_COUNT+=1
if exist "documentacion\INSTALACION.md" set /a DOC_COUNT+=1
if exist "ESTADO_FINAL_COMPLETO.md" set /a DOC_COUNT+=1

echo Documentos encontrados: %DOC_COUNT%/5
if %DOC_COUNT% GEQ 5 (
    echo ✅ Documentación completa
) else (
    echo ⚠️  Falta documentación
)
echo.

echo ═══════════════════════════════════════════════════════════════════════════
if %ERRORES% EQU 0 (
    echo    ✅ VERIFICACIÓN COMPLETADA - TODO OK
) else (
    echo    ⚠️  VERIFICACIÓN COMPLETADA CON %ERRORES% PROBLEMA(S^)
)
echo ═══════════════════════════════════════════════════════════════════════════
echo.

echo 📝 RESUMEN:
echo.
if %ERRORES% EQU 0 (
    echo    ✅ El sistema está listo para ejecutarse
    echo    ✅ Base de datos configurada correctamente
    echo    ✅ Código compilado sin errores
    echo.
    echo    🚀 PARA EJECUTAR:
    echo       mvn javafx:run
    echo.
    echo    🔐 LOGIN:
    echo       Usuario: admin
    echo       Contraseña: admin
    echo.
) else (
    echo    ⚠️  Hay %ERRORES% problema(s) que requieren atención
    echo.
    echo    📖 CONSULTA:
    echo       - ESTADO_FINAL_COMPLETO.md
    echo       - documentacion\INSTALACION.md
    echo.
)

if not exist "src\main\resources\certs\mi_certificado.p12" (
    echo    📝 NOTA IMPORTANTE:
    echo    ────────────────────────────────────────────────────────────
    echo    Para usar Verifactu en producción necesitas:
    echo    1. Certificado digital de la FNMT
    echo    2. Ejecutar: scripts\configurar_verifactu.bat
    echo    3. Ver guía: documentacion\VERIFACTU_COMPLETO.md
    echo.
)

echo ═══════════════════════════════════════════════════════════════════════════
echo.

pause

