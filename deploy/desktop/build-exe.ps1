<#
.SYNOPSIS
    (Opcional) Compila iniciar-erp.ps1 y parar-erp.ps1 a .exe con icono.

.DESCRIPTION
    El doble clic en los .cmd ya arranca la app; esto solo genera .exe "de verdad"
    (con icono propio) para una experiencia mas de aplicacion de escritorio.
    Requiere el modulo ps2exe:  Install-Module ps2exe -Scope CurrentUser

.PARAMETER IconPath
    Ruta a un .ico opcional para el icono del ejecutable.
#>
param([string]$IconPath)

$ErrorActionPreference = "Stop"
if (-not (Get-Module -ListAvailable -Name ps2exe)) {
    Write-Host "Falta el modulo ps2exe. Instalalo con:" -ForegroundColor Yellow
    Write-Host "  Install-Module ps2exe -Scope CurrentUser" -ForegroundColor Yellow
    exit 1
}
Import-Module ps2exe

$common = @{ noConsole = $false; requireAdmin = $false }
if ($IconPath -and (Test-Path $IconPath)) { $common.iconFile = $IconPath }

Invoke-ps2exe -inputFile (Join-Path $PSScriptRoot "iniciar-erp.ps1") `
              -outputFile (Join-Path $PSScriptRoot "ERP Tahona.exe") @common -title "ERP Tahona"
Invoke-ps2exe -inputFile (Join-Path $PSScriptRoot "parar-erp.ps1") `
              -outputFile (Join-Path $PSScriptRoot "Detener ERP.exe") @common -title "Detener ERP"
Write-Host "Generados 'ERP Tahona.exe' y 'Detener ERP.exe' en deploy\desktop." -ForegroundColor Green
