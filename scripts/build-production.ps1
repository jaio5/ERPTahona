param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

$mavenArgs = @("clean", "package")
if ($SkipTests) {
    $mavenArgs += "-DskipTests"
}

& "$PSScriptRoot\..\mvnw.cmd" @mavenArgs
