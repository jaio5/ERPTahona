param(
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [string]$OutputDirectory = "dist\erp-tahona-production"
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$outputPath = Join-Path $repoRoot $OutputDirectory
$outputParent = Split-Path -Parent $outputPath
if (-not (Test-Path -LiteralPath $outputParent)) {
    New-Item -ItemType Directory -Path $outputParent | Out-Null
}

$resolvedRepoRoot = [System.IO.Path]::GetFullPath($repoRoot.Path).TrimEnd('\') + '\'
$resolvedOutputPath = [System.IO.Path]::GetFullPath($outputPath)
if (-not $resolvedOutputPath.StartsWith($resolvedRepoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "El directorio de salida debe estar dentro del repositorio: $resolvedRepoRoot"
}

if (-not $SkipBuild) {
    $buildArgs = @()
    if ($SkipTests) {
        $buildArgs += "-SkipTests"
    }
    & "$PSScriptRoot\build-production.ps1" @buildArgs
}

$jar = Get-ChildItem -Path (Join-Path $repoRoot "target") -Filter "*.jar" |
    Where-Object { $_.Name -notlike "*.original" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "No se encontro ningun JAR en target. Ejecuta scripts\build-production.ps1 primero."
}

if (Test-Path -LiteralPath $outputPath) {
    Remove-Item -LiteralPath $outputPath -Recurse -Force
}

New-Item -ItemType Directory -Path $outputPath | Out-Null
New-Item -ItemType Directory -Path (Join-Path $outputPath "scripts") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $outputPath "docs") | Out-Null

Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $outputPath "ERP-0.0.1.jar")
Copy-Item -LiteralPath (Join-Path $repoRoot ".env.production.example") -Destination (Join-Path $outputPath ".env.production.example")
Copy-Item -LiteralPath (Join-Path $repoRoot "README.md") -Destination (Join-Path $outputPath "README.md")
Copy-Item -LiteralPath (Join-Path $repoRoot "README_RUN.md") -Destination (Join-Path $outputPath "README_RUN.md")

$scriptFiles = @(
    "load-env-file.ps1",
    "check-production-env.ps1",
    "check-verifactu-production.ps1",
    "check-production-readiness.ps1"
)

foreach ($script in $scriptFiles) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot $script) -Destination (Join-Path $outputPath "scripts\$script")
}

Get-ChildItem -LiteralPath (Join-Path $repoRoot "docs") -Filter "*.md" |
    Copy-Item -Destination (Join-Path $outputPath "docs")

$packageRunScript = @'
$ErrorActionPreference = "Stop"

$packageRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$envFile = Join-Path $packageRoot ".env.production.local"
if (Test-Path -LiteralPath $envFile) {
    & "$PSScriptRoot\load-env-file.ps1" -Path $envFile
}

$env:SPRING_PROFILES_ACTIVE = "prod"

& "$PSScriptRoot\check-production-env.ps1"

$jar = Join-Path $packageRoot "ERP-0.0.1.jar"
if (-not (Test-Path -LiteralPath $jar)) {
    throw "No se encontro ERP-0.0.1.jar en el paquete de produccion."
}

& java -jar $jar
'@

Set-Content -LiteralPath (Join-Path $outputPath "scripts\run-production.ps1") -Value $packageRunScript -Encoding UTF8

$manifest = @"
ERP Tahona - paquete de produccion
Generado: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
JAR: ERP-0.0.1.jar

Pasos en destino:
1. Copiar .env.production.example a .env.production.local.
2. Rellenar .env.production.local con valores reales.
3. Ejecutar scripts\check-production-readiness.ps1.
4. Ejecutar scripts\check-production-env.ps1.
5. Ejecutar scripts\check-verifactu-production.ps1.
6. Ejecutar scripts\run-production.ps1.
"@

Set-Content -LiteralPath (Join-Path $outputPath "MANIFEST.txt") -Value $manifest -Encoding UTF8

Write-Host "Paquete de produccion creado en: $outputPath"
