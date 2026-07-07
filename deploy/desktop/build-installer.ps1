<#
.SYNOPSIS
    Compila el instalador de escritorio ERP-Tahona-Setup.exe (Inno Setup).

.DESCRIPTION
    (Lo ejecuta el desarrollador.) Ensambla el paquete del cliente si hace falta
    (empaquetar-cliente.ps1), localiza el compilador de Inno Setup (ISCC.exe) y
    genera dist\ERP-Tahona-Setup-<version>.exe: un unico instalador que explica al
    cliente como funciona, deja un icono de escritorio y detecta/ofrece Docker Desktop.

.PARAMETER Version
    Version que se muestra en el asistente y en el nombre del .exe. Por defecto 1.0.0.

.PARAMETER SkipPackage
    No reconstruye el paquete: reutiliza dist\erp-desktop si ya existe (mas rapido).

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\deploy\desktop\build-installer.ps1
.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\deploy\desktop\build-installer.ps1 -Version 1.2.0 -SkipPackage
#>
param(
    [string]$Version = "1.0.0",
    [switch]$SkipPackage
)
$ErrorActionPreference = "Stop"

$scriptDir = $PSScriptRoot
$repoRoot  = (Resolve-Path (Join-Path $scriptDir "..\..")).Path
$packageDir = Join-Path $repoRoot "dist\erp-desktop"
$outputDir  = Join-Path $repoRoot "dist"
$iss        = Join-Path $scriptDir "installer\ERPTahona.iss"
$icon       = Join-Path $scriptDir "installer\erp-icono.ico"

function Fail($msg) { Write-Host ""; Write-Host "ERROR: $msg" -ForegroundColor Red; exit 1 }

# --- 0. Icono (generarlo si falta) ---
if (-not (Test-Path $icon)) {
    Write-Host "==> Generando el icono de la aplicacion..." -ForegroundColor Cyan
    & (Join-Path $scriptDir "installer\crear-icono.ps1")
}

# --- 1. Paquete del cliente (compose + lanzadores + imagen .tar) ---
$needPackage = -not (Test-Path (Join-Path $packageDir "erp-tahona-image.tar"))
if ($SkipPackage -and -not $needPackage) {
    Write-Host "==> Reutilizando el paquete existente: $packageDir" -ForegroundColor Cyan
} else {
    if ($SkipPackage -and $needPackage) {
        Write-Host "==> -SkipPackage indicado pero no hay paquete; se construye igualmente." -ForegroundColor Yellow
    }
    Write-Host "==> Ensamblando el paquete del cliente (esto construye y exporta la imagen)..." -ForegroundColor Cyan
    & (Join-Path $scriptDir "empaquetar-cliente.ps1")
}
if (-not (Test-Path (Join-Path $packageDir "erp-tahona-image.tar"))) {
    Fail "No se genero el paquete en $packageDir. Revisa empaquetar-cliente.ps1."
}

# --- 2. Localizar el compilador de Inno Setup (ISCC.exe) ---
$candidates = @(
    "$env:LOCALAPPDATA\Programs\Inno Setup 6\ISCC.exe",
    "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe",
    "$env:ProgramFiles\Inno Setup 6\ISCC.exe",
    "${env:ProgramFiles(x86)}\Inno Setup 5\ISCC.exe"
)
$iscc = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $iscc) {
    $onPath = (Get-Command ISCC.exe -ErrorAction SilentlyContinue).Source
    if ($onPath) { $iscc = $onPath }
}
if (-not $iscc) {
    Write-Host ""
    Write-Host "No se encontro Inno Setup (ISCC.exe)." -ForegroundColor Yellow
    Write-Host "Instalalo una sola vez con:" -ForegroundColor Yellow
    Write-Host "  winget install --id JRSoftware.InnoSetup -e" -ForegroundColor White
    Write-Host "o descargalo de https://jrsoftware.org/isdl.php y vuelve a ejecutar este script." -ForegroundColor Yellow
    Fail "Inno Setup no disponible."
}
Write-Host "==> Inno Setup: $iscc" -ForegroundColor Cyan

# --- 3. Compilar el instalador ---
Write-Host "==> Compilando el instalador (version $Version)..." -ForegroundColor Cyan
& $iscc `
    "/DMyAppVersion=$Version" `
    "/DMyPackageDir=$packageDir" `
    "/DMyOutputDir=$outputDir" `
    $iss
if ($LASTEXITCODE -ne 0) { Fail "Fallo la compilacion del instalador (ISCC devolvio $LASTEXITCODE)." }

$setup = Join-Path $outputDir "ERP-Tahona-Setup-$Version.exe"
if (-not (Test-Path $setup)) { Fail "ISCC termino pero no se encuentra $setup." }

$sizeMB = [math]::Round((Get-Item $setup).Length / 1MB)
Write-Host ""
Write-Host "LISTO. Instalador generado:" -ForegroundColor Green
Write-Host "  $setup  (${sizeMB} MB)" -ForegroundColor Green
Write-Host "  Entrega ese unico .exe al cliente: doble clic e instala." -ForegroundColor Green
