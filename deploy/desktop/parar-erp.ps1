<#
.SYNOPSIS
    Detiene el stack de ERP Tahona sin borrar datos.
.DESCRIPTION
    Ejecuta "docker compose stop": para los contenedores pero CONSERVA los volumenes
    (base de datos, backups, PDFs, certificados). Los datos siguen ahi al volver a iniciar.
#>
$ErrorActionPreference = "Stop"

$dir = $PSScriptRoot
$composeDir = $null
for ($i = 0; $i -lt 5 -and $dir; $i++) {
    if (Test-Path (Join-Path $dir "docker-compose.yml")) { $composeDir = $dir; break }
    $dir = Split-Path -Parent $dir
}
if (-not $composeDir) { Write-Host "No encuentro docker-compose.yml." -ForegroundColor Red; exit 1 }
Set-Location $composeDir
# Mismo nombre de proyecto fijo que iniciar-erp.ps1, para operar sobre el stack correcto.
$env:COMPOSE_PROJECT_NAME = "erp-tahona"

Write-Host "==> Deteniendo ERP Tahona (los datos se conservan)..." -ForegroundColor Cyan
docker compose stop
Write-Host "    Detenido. Vuelve a arrancar con 'iniciar-erp.cmd'." -ForegroundColor Green
Start-Sleep -Seconds 2
