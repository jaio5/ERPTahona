$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot "..\.env.production.local"
if (Test-Path -LiteralPath $envFile) {
    & "$PSScriptRoot\load-env-file.ps1" -Path $envFile
}

$jar = Get-ChildItem -Path "$PSScriptRoot\..\target" -Filter "*.jar" |
    Where-Object { $_.Name -notlike "*.original" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "No se encontro ningun JAR en target. Ejecuta scripts\build-production.ps1 primero."
}

$env:SPRING_PROFILES_ACTIVE = "prod"

& "$PSScriptRoot\check-production-env.ps1"

& java -jar $jar.FullName
