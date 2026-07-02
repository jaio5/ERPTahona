@echo off
chcp 65001 > nul
cls

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo    🔐 CONFIGURACIÓN COMPLETA DE VERIFACTU - ERP PANADERÍA TAHONA
echo ═══════════════════════════════════════════════════════════════════════════
echo.

REM Verificar que estamos en el directorio correcto
if not exist "pom.xml" (
    echo ❌ ERROR: Este script debe ejecutarse desde la raíz del proyecto
    pause
    exit /b 1
)

echo.
echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  PASO 1: VERIFICAR REQUISITOS                                           │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

REM Verificar MySQL
echo Verificando conexión a MySQL...
mysql --version > nul 2>&1
if errorlevel 1 (
    echo ❌ MySQL no está disponible en el PATH
    echo    Por favor, instala MySQL o añádelo al PATH
    pause
    exit /b 1
)
echo ✅ MySQL disponible

REM Verificar Java
echo Verificando Java...
java -version > nul 2>&1
if errorlevel 1 (
    echo ❌ Java no está disponible
    pause
    exit /b 1
)
echo ✅ Java disponible

echo.
echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  PASO 2: CONFIGURAR BASE DE DATOS                                       │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

REM Leer credenciales desde entorno, sin valores reales en el repositorio.
if "%DB_USER%"=="" set DB_USER=%SPRING_DATASOURCE_USERNAME%
if "%DB_PASS%"=="" set DB_PASS=%SPRING_DATASOURCE_PASSWORD%
if "%DB_NAME%"=="" set DB_NAME=tahona

if "%DB_USER%"=="" (
    echo ❌ Define DB_USER o SPRING_DATASOURCE_USERNAME antes de ejecutar este script
    pause
    exit /b 1
)

if "%DB_PASS%"=="" (
    echo ❌ Define DB_PASS o SPRING_DATASOURCE_PASSWORD antes de ejecutar este script
    pause
    exit /b 1
)

echo Configurando empresa en base de datos...
mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; INSERT INTO empresa_config (nombre_empresa, cif, direccion, codigo_postal, poblacion, provincia, telefono, email, verifactu_habilitado, verifactu_nif_emisor, verifactu_nombre_sistema, verifactu_version_sistema, verifactu_id_dispositivo, activo) VALUES ('GRUPO BABO, S.Coop.V.L.', 'F54059985', 'C/ EJEMPLO, 123', '03001', 'Alicante', 'Alicante', '965123456', 'info@grupobabo.es', TRUE, 'F54059985', 'ERP Panadería Tahona', '1.0.0', 'DISP001', TRUE) ON DUPLICATE KEY UPDATE verifactu_habilitado=VALUES(verifactu_habilitado), verifactu_nif_emisor=VALUES(verifactu_nif_emisor), verifactu_nombre_sistema=VALUES(verifactu_nombre_sistema), verifactu_version_sistema=VALUES(verifactu_version_sistema);" 2>nul

if errorlevel 1 (
    echo ❌ Error configurando base de datos
    pause
    exit /b 1
)
echo ✅ Configuración de empresa actualizada

echo.
echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  PASO 3: VERIFICAR CERTIFICADO DIGITAL                                  │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

set CERT_PATH=src\main\resources\certs\mi_certificado.p12
set TARGET_CERT_PATH=target\classes\certs\mi_certificado.p12

if not exist "%CERT_PATH%" (
    echo.
    echo ⚠️  ADVERTENCIA: No se encontró certificado digital
    echo.
    echo    Ubicación esperada: %CERT_PATH%
    echo.
    echo    📝 Para obtener un certificado digital:
    echo.
    echo    OPCIÓN A - CERTIFICADO REAL (Producción)
    echo    ========================================
    echo    1. Ir a: https://www.sede.fnmt.gob.es/certificados
    echo    2. Solicitar "Certificado de Persona Jurídica" para GRUPO BABO
    echo    3. Seguir el proceso de video-identificación
    echo    4. Descargar e instalar el certificado
    echo    5. Exportar como .p12 desde el navegador
    echo    6. Copiar a: %CERT_PATH%
    echo.
    echo    OPCIÓN B - CERTIFICADO DE PRUEBA (Desarrollo)
    echo    =============================================
    echo    1. Ir a: https://www.agenciatributaria.es/AEAT.internet/Inicio/La_Agencia_Tributaria/Campanas/Verifactu/
    echo    2. Solicitar acceso al entorno de preproducción
    echo    3. Descargar certificado de prueba
    echo    4. Copiar a: %CERT_PATH%
    echo.
    echo    OPCIÓN C - CONTINUAR SIN CERTIFICADO (Solo evidencias locales)
    echo    ==============================================================
    echo    La aplicación funcionará pero NO enviará a la AEAT.
    echo    Solo guardará evidencias localmente.
    echo.
    choice /C SAC /N /M "¿Qué deseas hacer? [S]alir [A]yudar [C]ontinuar sin certificado: "
    if errorlevel 3 (
        echo.
        echo ℹ️  Continuando sin certificado...
        echo    verifactu.aeat.enabled = false
        echo.
    ) else if errorlevel 2 (
        echo.
        echo Abriendo guía de configuración...
        start "" "documentacion\GUIA_CONFIGURACION_AEAT.md"
        pause
        exit /b 0
    ) else (
        exit /b 0
    )
) else (
    echo ✅ Certificado encontrado: %CERT_PATH%

    REM Verificar tamaño del certificado
    for %%A in ("%CERT_PATH%") do set CERT_SIZE=%%~zA
    echo    Tamaño: %CERT_SIZE% bytes

    if %CERT_SIZE% LSS 1000 (
        echo.
        echo ⚠️  ADVERTENCIA: El certificado parece demasiado pequeño
        echo    Puede que no sea un certificado válido
        echo.
    )

    REM Copiar a target si no existe
    if not exist "target\classes\certs" mkdir "target\classes\certs"
    copy /Y "%CERT_PATH%" "%TARGET_CERT_PATH%" >nul 2>&1
)

echo.
echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  PASO 4: VERIFICAR CONFIGURACIÓN                                        │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

echo Verificando application.properties...
findstr /C:"verifactu.aeat.enabled" src\main\resources\application.properties >nul
if errorlevel 1 (
    echo ⚠️  No se encontró configuración de verifactu
) else (
    echo ✅ Configuración de verifactu encontrada
)

echo.
echo Estado actual de la configuración:
echo ────────────────────────────────────────
type src\main\resources\application.properties | findstr /B "verifactu"

echo.
echo ┌─────────────────────────────────────────────────────────────────────────┐
echo │  PASO 5: DIAGNÓSTICO FINAL                                              │
echo └─────────────────────────────────────────────────────────────────────────┘
echo.

echo Ejecutando diagnóstico de base de datos...
mysql -u %DB_USER% -p%DB_PASS% -e "USE %DB_NAME%; SELECT '✅ Base de datos:', DATABASE(); SELECT '✅ Empresa configurada:', COUNT(*) as total FROM empresa_config WHERE activo=TRUE; SELECT '✅ Clientes disponibles:', COUNT(*) as total FROM clientes WHERE activo=TRUE; SELECT '✅ Usuario admin:', CASE WHEN COUNT(*)>0 THEN 'OK' ELSE 'NO EXISTE' END as estado FROM users WHERE username='admin';" 2>nul

echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo    ✅ CONFIGURACIÓN COMPLETADA
echo ═══════════════════════════════════════════════════════════════════════════
echo.
echo    📝 SIGUIENTE PASO:
echo.
if exist "%CERT_PATH%" (
    echo    1. Edita src\main\resources\application.properties
    echo    2. Actualiza la contraseña del certificado:
    echo       verifactu.keystore.password=TU_PASSWORD_AQUI
    echo    3. Actualiza el alias (normalmente el CIF en minúsculas):
    echo       verifactu.key.alias=f54059985
    echo    4. Para PRODUCCIÓN, cambia:
    echo       verifactu.aeat.enabled=true
    echo    5. Ejecuta: mvn spring-boot:run
) else (
    echo    1. Obtén un certificado digital (ver opciones arriba)
    echo    2. Cópialo a: %CERT_PATH%
    echo    3. Ejecuta este script nuevamente
    echo    O
    echo    4. Ejecuta sin certificado (solo evidencias locales):
    echo       mvn spring-boot:run
)
echo.
echo    📖 Ver documentación completa:
echo       documentacion\GUIA_CONFIGURACION_AEAT.md
echo.
echo ═══════════════════════════════════════════════════════════════════════════
echo.

pause

