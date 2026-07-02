param(
    [switch]$RequireVerifactuAeat
)

$ErrorActionPreference = "Stop"

function Require-EnvValue {
    param(
        [string]$Name,
        [int]$MinLength = 1
    )

    $value = [Environment]::GetEnvironmentVariable($Name, "Process")
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Falta la variable de entorno $Name."
    }
    if ($value.Length -lt $MinLength) {
        throw "La variable $Name debe tener al menos $MinLength caracteres."
    }
    if ($value -match "change-me|changeme|base64-32-byte-key|mysql-host|<|>") {
        throw "La variable $Name contiene un placeholder, no un valor real."
    }
    return $value
}

function Ensure-Directory {
    param([string]$PathValue)
    if ([string]::IsNullOrWhiteSpace($PathValue)) {
        return
    }
    if (-not (Test-Path -LiteralPath $PathValue)) {
        New-Item -ItemType Directory -Path $PathValue | Out-Null
    }
}

$javaVersionOutput = cmd /c "java -version 2>&1"
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace(($javaVersionOutput -join "`n"))) {
    throw "Java no esta disponible en PATH."
}
if (($javaVersionOutput -join "`n") -notmatch 'version "([0-9]+)') {
    throw "No se pudo detectar la version de Java: $javaVersionOutput"
}
$javaMajor = [int]$Matches[1]
if ($javaMajor -lt 17 -or $javaMajor -ge 24) {
    throw "La aplicacion requiere JDK/JRE 17 a 23. Version detectada: $javaMajor"
}

$datasourceUrl = Require-EnvValue "SPRING_DATASOURCE_URL"
if (-not $datasourceUrl.ToLowerInvariant().StartsWith("jdbc:mysql:")) {
    throw "SPRING_DATASOURCE_URL debe ser una URL JDBC MySQL en produccion."
}

Require-EnvValue "SPRING_DATASOURCE_USERNAME" | Out-Null
Require-EnvValue "SPRING_DATASOURCE_PASSWORD" | Out-Null
Require-EnvValue "CIFRADO_AES_KEY" 24 | Out-Null
Require-EnvValue "SECURITY_PBKDF2_SECRET" 32 | Out-Null
Require-EnvValue "ADMIN_DEFAULT_PASSWORD" 12 | Out-Null

$logFile = [Environment]::GetEnvironmentVariable("ERP_LOG_FILE", "Process")
if (-not [string]::IsNullOrWhiteSpace($logFile)) {
    Ensure-Directory ([System.IO.Path]::GetDirectoryName($logFile))
}

$backupDirectory = [Environment]::GetEnvironmentVariable("ERP_BACKUP_DIRECTORY", "Process")
if (-not [string]::IsNullOrWhiteSpace($backupDirectory)) {
    Ensure-Directory $backupDirectory
}

& "$PSScriptRoot\check-verifactu-production.ps1" -RequireAeatEnabled:$RequireVerifactuAeat

Write-Host "Precheck general de produccion correcto."
