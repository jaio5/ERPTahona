Write-Host "=====================================" -ForegroundColor Green
Write-Host "  ERP Panaderia Tahona" -ForegroundColor Green
Write-Host "  Arrancando aplicacion..." -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
`$scriptPath = Split-Path -Parent `$MyInvocation.MyCommand.Path
Set-Location `$scriptPath
mvn javafx:run
Write-Host ""
Write-Host "Presiona cualquier tecla para salir..." -ForegroundColor Yellow
`$null = `$Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
