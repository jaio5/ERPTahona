<#
.SYNOPSIS
    Lanzador de escritorio de ERP Tahona: levanta el stack Docker y abre el navegador.

.DESCRIPTION
    Pensado para el equipo del cliente. Al ejecutarlo:
      1. Comprueba que Docker esta instalado y arrancado (arranca Docker Desktop si hace falta).
      2. En el PRIMER arranque genera un fichero .env con secretos aleatorios fuertes
         (no se regenera si ya existe, para no invalidar datos cifrados ni contrasenas).
      3. Carga la imagen pre-construida (erp-tahona-image.tar) si aun no esta.
      4. Levanta el stack (docker compose up -d) y espera a que la app este "healthy".
      5. Abre el navegador en la aplicacion.

    No necesita JDK, Maven ni internet si la imagen viene pre-construida en el paquete.
    Nota: solo caracteres ASCII, para que funcione bajo Windows PowerShell 5.1.
#>
$ErrorActionPreference = "Stop"

function Write-Step($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "    $msg" -ForegroundColor Green }
function Write-Warn2($msg){ Write-Host "    $msg" -ForegroundColor Yellow }
function Fail($msg)       { Write-Host ""; Write-Host "ERROR: $msg" -ForegroundColor Red; Read-Host "Pulsa Enter para cerrar"; exit 1 }

# Ejecuta un comando docker "de sondeo" (info, inspect...) devolviendo exito/salida SIN
# abortar el script. En Windows PowerShell 5.1 (el que invoca iniciar-erp.cmd), con
# $ErrorActionPreference='Stop', redirigir el stderr de un comando nativo (2>$null / *>$null)
# convierte cualquier mensaje de error (p.ej. "No such image" cuando la imagen aun no se ha
# cargado) en un error TERMINANTE que mataria el script. Bajamos el nivel solo aqui.
function Invoke-DockerQuiet {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$DockerArgs)
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $out = & docker @DockerArgs 2>$null
        return [pscustomobject]@{ Ok = ($LASTEXITCODE -eq 0); Output = $out }
    } finally {
        $ErrorActionPreference = $prev
    }
}

# Inserta o actualiza claves KEY=valor en un fichero .env conservando el resto.
function Set-EnvValues([string]$path, [System.Collections.IDictionary]$kv) {
    $lines = @()
    if (Test-Path $path) { $lines = @(Get-Content -LiteralPath $path) }
    foreach ($key in $kv.Keys) {
        $newline = "$key=$($kv[$key])"
        $pattern = "^\s*$([regex]::Escape($key))="
        $idx = -1
        for ($i = 0; $i -lt $lines.Count; $i++) { if ($lines[$i] -match $pattern) { $idx = $i; break } }
        if ($idx -ge 0) { $lines[$idx] = $newline } else { $lines += $newline }
    }
    [System.IO.File]::WriteAllText($path, ($lines -join "`n") + "`n", (New-Object System.Text.UTF8Encoding($false)))
}

# --- Localizar la carpeta con docker-compose.yml (subiendo desde el script) ---
$dir = $PSScriptRoot
$composeDir = $null
for ($i = 0; $i -lt 5 -and $dir; $i++) {
    if (Test-Path (Join-Path $dir "docker-compose.yml")) { $composeDir = $dir; break }
    $dir = Split-Path -Parent $dir
}
if (-not $composeDir) { Fail "No encuentro docker-compose.yml junto al lanzador." }
Set-Location $composeDir
# Nombre de proyecto Compose fijo e independiente de la carpeta de instalacion. Asi los
# volumenes (BD, backups...) son deterministas ("erp-tahona_*") y no chocan con otros stacks
# del mismo equipo (p.ej. un repo de desarrollo llamado ERPTahona daria el mismo proyecto).
$env:COMPOSE_PROJECT_NAME = "erp-tahona"
Write-Step "Carpeta de la aplicacion: $composeDir"

# --- 1. Docker disponible y arrancado ---
Write-Step "Comprobando Docker..."
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Fail "Docker no esta instalado. Instala Docker Desktop desde https://www.docker.com/products/docker-desktop y vuelve a ejecutar."
}
if (-not (Invoke-DockerQuiet 'info').Ok) {
    Write-Warn2 "El motor de Docker no responde. Intentando arrancar Docker Desktop..."
    $dd = Join-Path $env:ProgramFiles "Docker\Docker\Docker Desktop.exe"
    if (Test-Path $dd) { Start-Process $dd | Out-Null } else { Fail "Docker Desktop no encontrado. Arrancalo manualmente y reintenta." }
    $deadline = (Get-Date).AddMinutes(3)
    $dockerOk = $false
    do {
        Start-Sleep -Seconds 5
        $dockerOk = (Invoke-DockerQuiet 'info').Ok
        Write-Host "    ...esperando a Docker" -ForegroundColor DarkGray
    } while (-not $dockerOk -and (Get-Date) -lt $deadline)
    if (-not $dockerOk) { Fail "Docker no arranco a tiempo. Abre Docker Desktop y reintenta." }
}
Write-Ok "Docker operativo."

# --- 2. .env: generar en el primer arranque (nunca sobrescribir) ---
$envPath = Join-Path $composeDir ".env"
if (-not (Test-Path $envPath)) {
    Write-Step "Primer arranque: generando .env con secretos aleatorios..."
    function New-B64([int]$n=32) {
        $b = New-Object 'System.Byte[]' $n
        [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
        [Convert]::ToBase64String($b)
    }
    function New-Pass([int]$len=18) {
        $ab = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789'
        -join (1..$len | ForEach-Object { $ab[(Get-Random -Maximum $ab.Length)] })
    }
    $adminPass = New-Pass 16
    $lines = @(
        "# Generado automaticamente en el primer arranque. NO lo compartas.",
        "MYSQL_ROOT_PASSWORD=$(New-Pass 24)",
        "MYSQL_PASSWORD=$(New-Pass 24)",
        "ADMIN_DEFAULT_PASSWORD=$adminPass",
        "CIFRADO_AES_KEY=$(New-B64 32)",
        "SECURITY_PBKDF2_SECRET=$(New-B64 32)",
        "ERP_DOMAIN=localhost",
        "ERP_TLS_MODE=internal",
        "SESSION_COOKIE_SECURE=true"
    )
    [System.IO.File]::WriteAllText($envPath, ($lines -join "`n") + "`n", (New-Object System.Text.UTF8Encoding($false)))
    Write-Ok ".env creado."
    Write-Host ""
    Write-Host "  ============================================================" -ForegroundColor Magenta
    Write-Host "   CONTRASENA INICIAL DE ADMIN (anotala, se pedira cambiarla)" -ForegroundColor Magenta
    Write-Host ("      usuario: admin     contrasena: {0}" -f $adminPass) -ForegroundColor Magenta
    Write-Host "  ============================================================" -ForegroundColor Magenta
    Write-Host ""
} else {
    Write-Ok ".env existente: se conservan los secretos actuales."
}

# --- 2b. VeriFactu: aplicar la configuracion elegida en el instalador (verifactu.conf) ---
# El instalador (opcional) deja verifactu.conf con el certificado y el modo. Aqui se
# traslada al .env que lee docker-compose. Es idempotente: se puede reejecutar.
$confPath = Join-Path $composeDir "verifactu.conf"
if (Test-Path $confPath) {
    $conf = @{}
    foreach ($l in Get-Content -LiteralPath $confPath) {
        if ($l -match '^\s*#') { continue }
        if ($l -match '^\s*([^=]+?)\s*=\s*(.*)$') { $conf[$matches[1].Trim()] = $matches[2].Trim() }
    }
    if ($conf['CERT_FILE']) {
        $certFile = Join-Path (Join-Path $composeDir "certs") $conf['CERT_FILE']
        if (-not (Test-Path $certFile)) {
            Write-Warn2 "verifactu.conf apunta a un certificado que no existe en certs\: $($conf['CERT_FILE']). Se omite VeriFactu."
        } else {
            $vf = [ordered]@{
                "VERIFACTU_CERT_PATH"     = "certs/$($conf['CERT_FILE'])"
                "VERIFACTU_CERT_PASSWORD" = $conf['CERT_PASSWORD']
                "VERIFACTU_KEY_ALIAS"     = $(if ($conf['KEY_ALIAS']) { $conf['KEY_ALIAS'] } else { "mi_certificado" })
            }
            if ($conf['MODE'] -eq "produccion" -and $conf['AEAT_ENDPOINT']) {
                $vf["VERIFACTU_AEAT_ENABLED"]  = "true"
                $vf["VERIFACTU_AEAT_ENDPOINT"] = $conf['AEAT_ENDPOINT']
                Write-Step "VeriFactu en modo PRODUCCION (remision real a la AEAT)."
            } else {
                $vf["VERIFACTU_AEAT_ENABLED"]  = "false"
                Write-Step "VeriFactu en modo PRUEBAS (firma y QR activos; sin remision a la AEAT)."
            }
            Set-EnvValues $envPath $vf
            Write-Ok "Certificado VeriFactu aplicado al .env."
        }
    }
}

# --- 3. Imagen pre-construida ---
Write-Step "Comprobando la imagen de la aplicacion..."
if (-not (Invoke-DockerQuiet 'image' 'inspect' 'erp-tahona:latest').Ok) {
    $tar = Join-Path $composeDir "erp-tahona-image.tar"
    if (Test-Path $tar) {
        Write-Warn2 "Cargando imagen desde erp-tahona-image.tar (puede tardar)..."
        docker load -i $tar
        if ($LASTEXITCODE -ne 0) { Fail "No se pudo cargar la imagen." }
    } else {
        Fail "No existe la imagen 'erp-tahona:latest' ni el fichero erp-tahona-image.tar. Pide el paquete completo."
    }
}
Write-Ok "Imagen lista."

# --- 4. Levantar el stack y esperar salud ---
Write-Step "Levantando la aplicacion (MySQL + app + proxy)..."
docker compose up -d
if ($LASTEXITCODE -ne 0) { Fail "Fallo al levantar el stack. Revisa 'docker compose logs'." }

Write-Step "Esperando a que la aplicacion este lista..."
$deadline = (Get-Date).AddMinutes(4)
$estado = ""
do {
    Start-Sleep -Seconds 4
    $estado = (docker inspect --format '{{.State.Health.Status}}' erp-app 2>$null)
    Write-Host "    estado: $estado" -ForegroundColor DarkGray
} while ($estado -ne "healthy" -and (Get-Date) -lt $deadline)
if ($estado -ne "healthy") { Fail "La aplicacion no arranco a tiempo. Revisa 'docker compose logs app'." }
Write-Ok "Aplicacion lista."

# --- 5. Abrir el navegador ---
$dominio = "localhost"
$m = Select-String -Path $envPath -Pattern '^ERP_DOMAIN=(.+)$'
if ($m) { $dominio = $m.Matches[0].Groups[1].Value.Trim() }
$url = "https://$dominio"
Write-Host ""
Write-Ok "ERP Tahona en marcha:  $url"
Write-Warn2 "Si el navegador avisa del certificado (CA interna en modo LAN), acepta continuar."
Start-Process $url
Write-Host ""
Write-Host "Para detener la aplicacion usa 'parar-erp.cmd'." -ForegroundColor Gray
Start-Sleep -Seconds 3
