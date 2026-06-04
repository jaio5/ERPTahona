param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot "..\.env.production.local"
if (Test-Path -LiteralPath $envFile) {
    & "$PSScriptRoot\load-env-file.ps1" -Path $envFile
}

$mavenArgs = @("clean", "package")
if ($SkipTests) {
    $mavenArgs += "-DskipTests"
}

& "$PSScriptRoot\..\mvnw.cmd" @mavenArgs
