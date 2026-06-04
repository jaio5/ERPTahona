$ErrorActionPreference = "Stop"

$packageRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$envFile = Join-Path $packageRoot ".env.production.local"
if (Test-Path -LiteralPath $envFile) {
    Get-Content -LiteralPath $envFile | ForEach-Object {
        $line = $_.Trim()
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith("#")) { return }
        $separator = $line.IndexOf("=")
        if ($separator -lt 1) { return }
        $name = $line.Substring(0, $separator).Trim()
        $value = $line.Substring($separator + 1).Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

$env:SPRING_PROFILES_ACTIVE = "prod"

$jar = Join-Path $packageRoot "ERP-0.0.1.jar"
if (-not (Test-Path -LiteralPath $jar)) {
    throw "No se encontro ERP-0.0.1.jar en el paquete de produccion."
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  ERP TAHONA - Arranque en produccion" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "JAR: $jar"
Write-Host "Perfil: prod"
Write-Host ""

& java -jar $jar
