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
#>
$ErrorActionPreference = "Stop"

function Write-Step($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "    $msg" -ForegroundColor Green }
function Write-Warn2($msg){ Write-Host "    $msg" -ForegroundColor Yellow }
function Fail($msg)       { Write-Host ""; Write-Host "ERROR: $msg" -ForegroundColor Red; Read-Host "Pulsa Enter para cerrar"; exit 1 }

# ── Localizar la carpeta con docker-compose.yml (subiendo desde el script) ──
$dir = $PSScriptRoot
$composeDir = $null
for ($i = 0; $i -lt 5 -and $dir; $i++) {
    if (Test-Path (Join-Path $dir "docker-compose.yml")) { $composeDir = $dir; break }
    $dir = Split-Path -Parent $dir
}
if (-not $composeDir) { Fail "No encuentro docker-compose.yml junto al lanzador." }
Set-Location $composeDir
Write-Step "Carpeta de la aplicacion: $composeDir"

# ── 1. Docker disponible y arrancado ───────────────────────────────────────
Write-Step "Comprobando Docker..."
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Fail "Docker no esta instalado. Instala Docker Desktop desde https://www.docker.com/products/docker-desktop y vuelve a ejecutar."
}
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Warn2 "El motor de Docker no responde. Intentando arrancar Docker Desktop..."
    $dd = Join-Path $env:ProgramFiles "Docker\Docker\Docker Desktop.exe"
    if (Test-Path $dd) { Start-Process $dd | Out-Null } else { Fail "Docker Desktop no encontrado. Arrancalo manualmente y reintenta." }
    $deadline = (Get-Date).AddMinutes(3)
    do {
        Start-Sleep -Seconds 5
        docker info *> $null
        Write-Host "    ...esperando a Docker" -ForegroundColor DarkGray
    } while ($LASTEXITCODE -ne 0 -and (Get-Date) -lt $deadline)
    if ($LASTEXITCODE -ne 0) { Fail "Docker no arranco a tiempo. Abre Docker Desktop y reintenta." }
}
Write-Ok "Docker operativo."

# ── 2. .env: generar en el primer arranque (nunca sobrescribir) ─────────────
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
    Write-Host "  ┌───────────────────────────────────────────────────────────┐" -ForegroundColor Magenta
    Write-Host "  │  CONTRASENA INICIAL DE ADMIN (anotala, se pedira cambiarla) │" -ForegroundColor Magenta
    Write-Host ("  │      usuario: admin     contrasena: {0,-20}│" -f $adminPass) -ForegroundColor Magenta
    Write-Host "  └───────────────────────────────────────────────────────────┘" -ForegroundColor Magenta
    Write-Host ""
} else {
    Write-Ok ".env existente: se conservan los secretos actuales."
}

# ── 3. Imagen pre-construida ────────────────────────────────────────────────
Write-Step "Comprobando la imagen de la aplicacion..."
docker image inspect erp-tahona:latest *> $null
if ($LASTEXITCODE -ne 0) {
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

# ── 4. Levantar el stack y esperar salud ───────────────────────────────────
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

# ── 5. Abrir el navegador ──────────────────────────────────────────────────
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
