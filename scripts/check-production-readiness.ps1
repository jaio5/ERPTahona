# ERP Tahona - Verificación de preparación para producción
# Ejecutar: powershell -ExecutionPolicy Bypass -File .\scripts\check-production-readiness.ps1

$ErrorActionPreference = "Continue"
$total = 0; $ok = 0; $fail = 0

function Check($label, $condition, $detail) {
    $script:total++
    if ($condition) {
        Write-Host "  [OK]  $label" -ForegroundColor Green
        $script:ok++
    } else {
        Write-Host "  [FAIL] $label" -ForegroundColor Red
        if ($detail) { Write-Host "         $detail" -ForegroundColor DarkYellow }
        $script:fail++
    }
}

Clear-Host
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  ERP TAHONA - VERIFICACIÓN DE PRODUCCIÓN" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Fecha : $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
Write-Host "Host  : $env:COMPUTERNAME"
Write-Host ""

# --- 1. JAVA ---
Write-Host "--- JAVA ---" -ForegroundColor Yellow
$javaVer = (java -version 2>&1 | Select-Object -First 1)
Check "Java 17+ (requerido JDK 17-23)" ($javaVer -match 'version "(17|21|22|23)') "Versión detectada: $javaVer"

# --- 2. BASE DE DATOS ---
Write-Host "--- BASE DE DATOS ---" -ForegroundColor Yellow
$mysql = Get-Command mysql -ErrorAction SilentlyContinue
Check "MySQL client disponible" ($mysql -ne $null) "Necesario para backups con mysqldump"

$dbUrl = $env:DB_URL
$dbUser = $env:DB_USERNAME
$dbPass = $env:DB_PASSWORD

if ($env:SPRING_PROFILES_ACTIVE -eq "prod") {
    Check "DB_URL configurada (producción)" ($dbUrl -and $dbUrl -match 'mysql') "URL: $dbUrl"
    Check "DB_USERNAME configurada" ($dbUser -ne $null) ""
    Check "DB_PASSWORD configurada" ($dbPass -ne $null) ""
    Check "DB_PASSWORD no es valor por defecto" ($dbPass -ne "admin" -and $dbPass -ne "password" -and $dbPass.Length -ge 8) "Longitud: $($dbPass.Length) caracteres"
} else {
    Check "Perfil producción activo" $false "SPRING_PROFILES_ACTIVE='$env:SPRING_PROFILES_ACTIVE'. En prod debe ser 'prod'"
}

# --- 3. VERIFACTU ---
Write-Host "--- VERIFACTU ---" -ForegroundColor Yellow
$ksPath = $env:VERIFACTU_KEYSTORE_PATH
if (-not $ksPath) { $ksPath = "verifactu-keystore.p12" }

Check "Ruta keystore configurada" ($env:VERIFACTU_KEYSTORE_PATH -ne $null) ""
Check "Archivo keystore existe" (Test-Path -LiteralPath $ksPath) "Ruta: $ksPath"
Check "Permisos de lectura keystore" ((Test-Path -LiteralPath $ksPath) -and ((Get-Item -LiteralPath $ksPath).Attributes -band [System.IO.FileAttributes]::ReadOnly) -eq 0) ""
Check "Keystore password configurado" ($env:VERIFACTU_KEYSTORE_PASSWORD -ne $null) ""

# --- 4. SEGURIDAD ---
Write-Host "--- SEGURIDAD ---" -ForegroundColor Yellow
Check "AES_SECRET_KEY configurada" ($env:AES_SECRET_KEY -ne $null) ""
Check "AES_SECRET_KEY longitud >= 32" (($env:AES_SECRET_KEY).Length -ge 32) "Longitud: $(if ($env:AES_SECRET_KEY) { ($env:AES_SECRET_KEY).Length } else { 0 })"
Check "PBKDF2_SECRET configurado" ($env:PBKDF2_SECRET -ne $null) ""
Check "PBKDF2_SECRET longitud >= 16" (($env:PBKDF2_SECRET).Length -ge 16) "Longitud: $(if ($env:PBKDF2_SECRET) { ($env:PBKDF2_SECRET).Length } else { 0 })"

# --- 5. ARCHIVOS Y DIRECTORIOS ---
Write-Host "--- SISTEMA DE ARCHIVOS ---" -ForegroundColor Yellow
Check "Directorio impresiones/" (Test-Path -LiteralPath "impresiones" -PathType Container) ""
Check "Directorio backups/ existe" (Test-Path -LiteralPath "backups" -PathType Container) ""

$jar = Get-ChildItem -Path "dist\erp-tahona-production" -Filter "ERP-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jar) {
    Check "JAR de producción encontrado" $true "Ruta: $($jar.FullName)"
} else {
    Check "JAR de producción encontrado" (Test-Path -LiteralPath "target\ERP-*.jar") "En target/ (compilado local)"
}

# --- 6. MÓDULOS DE NEGOCIO ---
Write-Host "--- FUENTES (nuevos módulos) ---" -ForegroundColor Yellow
@(
    "src\main\java\alicanteweb\erp\entities\Receta.java",
    "src\main\java\alicanteweb\erp\entities\OrdenProduccion.java",
    "src\main\java\alicanteweb\erp\entities\Lote.java",
    "src\main\java\alicanteweb\erp\entities\Vehiculo.java",
    "src\main\java\alicanteweb\erp\entities\HojaRuta.java",
    "src\main\java\alicanteweb\erp\entities\Devolucion.java",
    "src\main\java\alicanteweb\erp\service\VerifactuHardeningService.java",
    "src\main\resources\db\migration\V10__modulos_produccion_reparto.sql",
    "src\main\resources\db\migration\V11__permisos_nuevos_modulos.sql"
) | ForEach-Object {
    $name = $_ -replace '.*\\', ''
    Check "Archivo: $name" (Test-Path -LiteralPath $_) ""
}

# --- RESUMEN ---
Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  RESULTADO: $ok OK / $fail FALLOS / $total TOTAL" -ForegroundColor $(if ($fail -eq 0) { "Green" } else { "Red" })
Write-Host "=============================================" -ForegroundColor Cyan

if ($fail -gt 0) {
    Write-Host "CONCLUSIÓN: NO APTA PARA PRODUCCIÓN" -ForegroundColor Red
    Write-Host "  Corrija los $fail fallos antes de desplegar." -ForegroundColor Yellow
    exit 1
} else {
    Write-Host "CONCLUSIÓN: APTA PARA PRODUCCIÓN" -ForegroundColor Green
    Write-Host "  Todas las verificaciones superadas." -ForegroundColor Green
    exit 0
}
