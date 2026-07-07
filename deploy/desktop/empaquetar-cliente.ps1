<#
.SYNOPSIS
    Genera el paquete de escritorio listo para entregar al cliente.

.DESCRIPTION
    (Lo ejecuta el desarrollador, no el cliente.) Construye la imagen Docker de la app,
    la exporta a un .tar y ensambla una carpeta autocontenida con el compose, el proxy,
    los lanzadores y la imagen. El cliente solo tiene que copiar esa carpeta y hacer doble
    clic en iniciar-erp.cmd (con Docker instalado). No necesita el codigo fuente, ni JDK,
    ni Maven, ni internet.

.PARAMETER OutputDirectory
    Carpeta de salida (relativa al repo). Por defecto: dist\erp-desktop.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\deploy\desktop\empaquetar-cliente.ps1
#>
param(
    [string]$OutputDirectory = "dist\erp-desktop"
)
$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$out = Join-Path $repoRoot $OutputDirectory
Set-Location $repoRoot

Write-Host "==> Construyendo la imagen erp-tahona:latest (docker compose build)..." -ForegroundColor Cyan
docker compose build
if ($LASTEXITCODE -ne 0) { throw "Fallo la construccion de la imagen." }

Write-Host "==> Preparando carpeta de salida: $out" -ForegroundColor Cyan
if (Test-Path $out) { Remove-Item -Recurse -Force $out }
New-Item -ItemType Directory -Path $out | Out-Null
New-Item -ItemType Directory -Path (Join-Path $out "deploy") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $out "certs")  | Out-Null

# compose sin la linea "build: ." (el cliente no compila, usa la imagen cargada)
Get-Content (Join-Path $repoRoot "docker-compose.yml") |
    Where-Object { $_ -notmatch '^\s*build:\s*\.\s*$' } |
    Set-Content (Join-Path $out "docker-compose.yml") -Encoding utf8

Copy-Item (Join-Path $repoRoot "deploy\Caddyfile")            (Join-Path $out "deploy\Caddyfile")
Copy-Item (Join-Path $PSScriptRoot "iniciar-erp.ps1")         $out
Copy-Item (Join-Path $PSScriptRoot "iniciar-erp.cmd")         $out
Copy-Item (Join-Path $PSScriptRoot "parar-erp.ps1")           $out
Copy-Item (Join-Path $PSScriptRoot "parar-erp.cmd")           $out
Set-Content (Join-Path $out "certs\.gitkeep") ""

$leeme = @"
ERP Tahona - Aplicacion de escritorio
=====================================

REQUISITO UNICO: tener Docker Desktop instalado y abierto.
(Descarga: https://www.docker.com/products/docker-desktop)

PARA ARRANCAR:  doble clic en  iniciar-erp.cmd
   - La primera vez genera las claves y muestra la contrasena inicial de 'admin'
     (anotala; se te pedira cambiarla al entrar).
   - Cuando termine, se abre el navegador en https://localhost
   - Si el navegador avisa del certificado, acepta continuar (es normal en modo local).

PARA DETENER:   doble clic en  parar-erp.cmd   (los datos se conservan)

Tus datos (BD, backups, PDFs) viven en Docker y sobreviven a apagados y actualizaciones.
"@
Set-Content (Join-Path $out "LEEME.txt") $leeme -Encoding utf8

Write-Host "==> Exportando la imagen a erp-tahona-image.tar (puede tardar)..." -ForegroundColor Cyan
docker save erp-tahona:latest -o (Join-Path $out "erp-tahona-image.tar")
if ($LASTEXITCODE -ne 0) { throw "Fallo la exportacion de la imagen." }

$sizeMB = [math]::Round((Get-Item (Join-Path $out "erp-tahona-image.tar")).Length / 1MB)
Write-Host ""
Write-Host "LISTO. Paquete en: $out" -ForegroundColor Green
Write-Host "  Imagen: erp-tahona-image.tar (${sizeMB} MB)" -ForegroundColor Green
Write-Host "  Comprime esa carpeta en un .zip y entregasela al cliente." -ForegroundColor Green
