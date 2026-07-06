<#
.SYNOPSIS
    Valida un fichero .env ANTES de "docker compose up -d --build".

.DESCRIPTION
    Comprueba que el .env que se usará en el despliegue tiene todos los secretos
    obligatorios, sin placeholders de .env.example, con longitudes minimas, y avisa
    de la configuracion recomendada (TLS, cookie segura, VeriFactu). Es el mismo
    criterio que aplica el guard de arranque de la app (DatabaseStartupChecker),
    pero se ejecuta antes de construir, dando feedback inmediato.

.PARAMETER Path
    Ruta al fichero .env (por defecto: .env en el directorio actual).

.PARAMETER RequireVerifactu
    Exige tambien el certificado VeriFactu (para despliegues que ya emiten con
    validez fiscal). Sin este flag, VeriFactu se trata como opcional (solo aviso).

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File .\scripts\check-env-file.ps1
    powershell -ExecutionPolicy Bypass -File .\scripts\check-env-file.ps1 -Path .env -RequireVerifactu
#>
param(
    [string]$Path = ".env",
    [switch]$RequireVerifactu
)

$ErrorActionPreference = "Stop"

# Tokens de placeholder que NO deben llegar a produccion (mismos que el guard Java).
$PLACEHOLDER = 'change-me|changeme|cambia-esta|cambiaestaclave|base64-32-byte-key|mysql-host|<|>'

$fails = 0
$warns = 0

function Say-Ok   ($msg) { Write-Host "  [OK]   $msg" -ForegroundColor Green }
function Say-Fail ($msg) { Write-Host "  [FAIL] $msg" -ForegroundColor Red;    $script:fails++ }
function Say-Warn ($msg) { Write-Host "  [WARN] $msg" -ForegroundColor Yellow; $script:warns++ }

# ── Cargar el .env ─────────────────────────────────────────────────────────
if (-not (Test-Path -LiteralPath $Path)) {
    Write-Host "No existe el fichero '$Path'. Copia .env.example a .env y rellenalo." -ForegroundColor Red
    exit 2
}

$env = @{}
foreach ($line in Get-Content -LiteralPath $Path) {
    $t = $line.Trim()
    if ($t -eq "" -or $t.StartsWith("#")) { continue }
    $eq = $t.IndexOf("=")
    if ($eq -lt 1) { continue }
    $k = $t.Substring(0, $eq).Trim()
    $v = $t.Substring($eq + 1).Trim()
    # quitar comillas envolventes
    if ($v.Length -ge 2 -and (($v.StartsWith('"') -and $v.EndsWith('"')) -or ($v.StartsWith("'") -and $v.EndsWith("'")))) {
        $v = $v.Substring(1, $v.Length - 2)
    }
    $env[$k] = $v
}

Write-Host ""
Write-Host "Validando '$Path'..." -ForegroundColor Cyan
Write-Host ""

# ── Secretos obligatorios ──────────────────────────────────────────────────
function Check-Secret ($name, $minLen) {
    if (-not $env.ContainsKey($name) -or [string]::IsNullOrWhiteSpace($env[$name])) {
        Say-Fail "$name : ausente o vacio."
        return
    }
    $val = $env[$name]
    if ($val -imatch $PLACEHOLDER) {
        Say-Fail "$name : es un placeholder de ejemplo, ponlo real."
        return
    }
    if ($val.Length -lt $minLen) {
        Say-Fail "$name : demasiado corto (min $minLen caracteres)."
        return
    }
    Say-Ok "$name : correcto ($($val.Length) caracteres)."
}

Write-Host "Secretos obligatorios:" -ForegroundColor White
Check-Secret "MYSQL_ROOT_PASSWORD"     10
Check-Secret "MYSQL_PASSWORD"          10
Check-Secret "ADMIN_DEFAULT_PASSWORD"  12
Check-Secret "CIFRADO_AES_KEY"         32   # openssl rand -base64 32 -> 44 chars
Check-Secret "SECURITY_PBKDF2_SECRET"  32   # openssl rand -base64 32 -> 44 chars

# MySQL root y app no deberian coincidir
if ($env["MYSQL_ROOT_PASSWORD"] -and $env["MYSQL_PASSWORD"] -and
    $env["MYSQL_ROOT_PASSWORD"] -eq $env["MYSQL_PASSWORD"]) {
    Say-Warn "MYSQL_ROOT_PASSWORD y MYSQL_PASSWORD son iguales: usa contrasenas distintas."
}

# ── TLS / cookie segura ────────────────────────────────────────────────────
Write-Host ""
Write-Host "TLS / sesion:" -ForegroundColor White
if ($env.ContainsKey("SESSION_COOKIE_SECURE") -and $env["SESSION_COOKIE_SECURE"] -eq "false") {
    Say-Warn "SESSION_COOKIE_SECURE=false: solo si sirves SIN el proxy TLS (no recomendado)."
} else {
    Say-Ok "Cookie de sesion segura activa (por defecto tras el proxy Caddy)."
}
$domain = $env["ERP_DOMAIN"]
$tls = $env["ERP_TLS_MODE"]
if ([string]::IsNullOrWhiteSpace($domain) -or $domain -eq "localhost") {
    Say-Warn "ERP_DOMAIN sin definir o 'localhost': modo LAN. Instala la CA interna de Caddy en los clientes (ver RUNBOOK)."
} elseif ($tls -and $tls -ne "internal" -and $tls -notmatch '@') {
    Say-Warn "ERP_TLS_MODE='$tls' no parece un email para Let's Encrypt ni 'internal'."
} else {
    Say-Ok "TLS configurado para dominio '$domain' (modo '$tls')."
}

# ── VeriFactu ──────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "VeriFactu (facturacion electronica AEAT):" -ForegroundColor White
$certPath = $env["VERIFACTU_CERT_PATH"]
$aeatEnabled = ($env["VERIFACTU_AEAT_ENABLED"] -eq "true")
if ([string]::IsNullOrWhiteSpace($certPath)) {
    if ($RequireVerifactu) {
        Say-Fail "VERIFACTU_CERT_PATH ausente pero se exige VeriFactu (-RequireVerifactu)."
    } else {
        Say-Warn "Sin VERIFACTU_CERT_PATH: firma/remision AEAT deshabilitadas (OK para arrancar; obligatorio antes de emitir con validez fiscal)."
    }
} else {
    if (Test-Path -LiteralPath $certPath) { Say-Ok "Certificado presente: $certPath" }
    else { Say-Fail "VERIFACTU_CERT_PATH apunta a un fichero inexistente: $certPath" }
    if ([string]::IsNullOrWhiteSpace($env["VERIFACTU_CERT_PASSWORD"])) {
        Say-Fail "VERIFACTU_CERT_PASSWORD vacio pero hay certificado configurado."
    }
    if ($aeatEnabled -and ($env["VERIFACTU_AEAT_ENDPOINT"] -match "prewww")) {
        Say-Fail "VERIFACTU_AEAT_ENABLED=true con endpoint de PRUEBAS (prewww): sin validez fiscal. Usa el endpoint de produccion o desactiva la remision."
    }
}
if ($aeatEnabled) {
    Say-Warn "VERIFACTU_AEAT_ENABLED=true: confirma que validaste XML/QR/firma/envio contra el entorno de PRUEBAS de AEAT antes de emitir en real."
}

# ── Resumen ────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host ("-" * 60)
if ($fails -gt 0) {
    Write-Host "RESULTADO: $fails fallo(s), $warns aviso(s). NO despliegues aun." -ForegroundColor Red
    exit 1
} elseif ($warns -gt 0) {
    Write-Host "RESULTADO: 0 fallos, $warns aviso(s). Revisa los avisos y despliega si procede." -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "RESULTADO: todo correcto. Listo para 'docker compose up -d --build'." -ForegroundColor Green
    exit 0
}
