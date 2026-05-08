$ErrorActionPreference = "Stop"

$jar = Get-ChildItem -Path "$PSScriptRoot\..\target" -Filter "*.jar" |
    Where-Object { $_.Name -notlike "*.original" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "No se encontro ningun JAR en target. Ejecuta scripts\build-production.ps1 primero."
}

$env:SPRING_PROFILES_ACTIVE = "prod"

& java -jar $jar.FullName
