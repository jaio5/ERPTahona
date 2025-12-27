# ========================================
# ERP Panadería Tahona - JavaFX Desktop
# Script PowerShell para ejecutar
# ========================================

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ERP PANADERÍA TAHONA - JAVAFX        ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

# Cambiar al directorio del script
Set-Location $PSScriptRoot

# Verificar Java
Write-Host "Verificando Java..." -ForegroundColor Cyan
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "  ✓ $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Java no encontrado" -ForegroundColor Red
    Write-Host "  Instala Java 17+ desde: https://adoptium.net/" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host ""

# Verificar Maven
Write-Host "Verificando Maven..." -ForegroundColor Cyan
try {
    $mavenVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "  ✓ $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Maven no encontrado" -ForegroundColor Red
    pause
    exit 1
}

Write-Host ""

# Compilar
Write-Host "Compilando aplicación JavaFX..." -ForegroundColor Cyan
$compileResult = mvn clean compile -DskipTests 2>&1 | Out-String

if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ Compilación exitosa" -ForegroundColor Green
} else {
    Write-Host "  ✗ Error en compilación" -ForegroundColor Red
    Write-Host $compileResult
    pause
    exit 1
}

Write-Host ""

# Ejecutar
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  INICIANDO APLICACIÓN JAVAFX...       ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "La ventana de la aplicación se abrirá en breve..." -ForegroundColor Yellow
Write-Host ""

mvn javafx:run

Write-Host ""
Write-Host "Aplicación cerrada." -ForegroundColor Gray
pause

