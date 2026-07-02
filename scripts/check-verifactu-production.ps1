param(
    [switch]$RequireAeatEnabled
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
    if ($value -match "change-me|changeme|<|>") {
        throw "La variable $Name contiene un placeholder, no un valor real."
    }
    return $value
}

$certPath = Require-EnvValue "VERIFACTU_CERT_PATH"
$certPassword = Require-EnvValue "VERIFACTU_CERT_PASSWORD"
$keyAlias = Require-EnvValue "VERIFACTU_KEY_ALIAS"
Require-EnvValue "VERIFACTU_KEY_PASSWORD" | Out-Null

if (-not (Test-Path -LiteralPath $certPath)) {
    throw "No existe el certificado VERI*FACTU: $certPath"
}

$aeatEnabled = [Environment]::GetEnvironmentVariable("VERIFACTU_AEAT_ENABLED", "Process")
if ($RequireAeatEnabled -and $aeatEnabled -ne "true") {
    throw "VERIFACTU_AEAT_ENABLED debe ser true para iniciar funcionamiento VERI*FACTU."
}

if ($aeatEnabled -eq "true") {
    Require-EnvValue "VERIFACTU_AEAT_ENDPOINT" | Out-Null
}

$keytool = Join-Path $env:JAVA_HOME "bin\keytool.exe"
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath $keytool)) {
    $keytool = "keytool"
}

$aliasOutput = & $keytool -list -v -storetype PKCS12 -keystore $certPath -storepass $certPassword -alias $keyAlias 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo leer el alias '$keyAlias' en el certificado. Salida de keytool: $aliasOutput"
}

Write-Host "VERI*FACTU precheck correcto: certificado y alias disponibles."

