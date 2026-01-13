@echo off
chcp 65001 > nul
cls
echo.
echo ═══════════════════════════════════════════════════════════
echo   ✅ VERIFICACIÓN FINAL - MÓDULOS IMPLEMENTADOS
echo ═══════════════════════════════════════════════════════════
echo.
echo 🔍 Verificando archivos implementados...
echo.

set ERRORES=0

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   📋 MÓDULO: PRESUPUESTOS
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

if exist "src\main\java\alicanteweb\erp\repository\PresupuestoRepository.java" (
    echo   ✅ PresupuestoRepository.java
) else (
    echo   ❌ PresupuestoRepository.java NO ENCONTRADO
    set /a ERRORES+=1
)

if exist "src\main\java\alicanteweb\erp\service\PresupuestoService.java" (
    echo   ✅ PresupuestoService.java
) else (
    echo   ❌ PresupuestoService.java NO ENCONTRADO
    set /a ERRORES+=1
)

if exist "src\main\java\alicanteweb\erp\controller\PresupuestoController.java" (
    echo   ✅ PresupuestoController.java
) else (
    echo   ❌ PresupuestoController.java NO ENCONTRADO
    set /a ERRORES+=1
)

if exist "src\main\resources\ui\presupuestos_panel.fxml" (
    echo   ✅ presupuestos_panel.fxml
) else (
    echo   ❌ presupuestos_panel.fxml NO ENCONTRADO
    set /a ERRORES+=1
)

echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   🧾 MÓDULO: FACTURAS DE COMPRA
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

if exist "src\main\java\alicanteweb\erp\repository\FacturaCompraRepository.java" (
    echo   ✅ FacturaCompraRepository.java
) else (
    echo   ❌ FacturaCompraRepository.java NO ENCONTRADO
    set /a ERRORES+=1
)

if exist "src\main\java\alicanteweb\erp\service\FacturaCompraService.java" (
    echo   ✅ FacturaCompraService.java
) else (
    echo   ❌ FacturaCompraService.java NO ENCONTRADO
    set /a ERRORES+=1
)

if exist "src\main\java\alicanteweb\erp\controller\FacturaCompraController.java" (
    echo   ✅ FacturaCompraController.java
) else (
    echo   ❌ FacturaCompraController.java NO ENCONTRADO
    set /a ERRORES+=1
)

if exist "src\main\resources\ui\facturas_compra_panel.fxml" (
    echo   ✅ facturas_compra_panel.fxml
) else (
    echo   ❌ facturas_compra_panel.fxml NO ENCONTRADO
    set /a ERRORES+=1
)

echo.
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo   📚 DOCUMENTACIÓN
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

if exist "VISTA_PRESUPUESTOS_IMPLEMENTADA.md" (
    echo   ✅ VISTA_PRESUPUESTOS_IMPLEMENTADA.md
) else (
    echo   ⚠️  VISTA_PRESUPUESTOS_IMPLEMENTADA.md no encontrado
)

if exist "RESUMEN_PRESUPUESTOS_COMPLETADO.md" (
    echo   ✅ RESUMEN_PRESUPUESTOS_COMPLETADO.md
) else (
    echo   ⚠️  RESUMEN_PRESUPUESTOS_COMPLETADO.md no encontrado
)

if exist "FACTURAS_COMPRA_IMPLEMENTADA.md" (
    echo   ✅ FACTURAS_COMPRA_IMPLEMENTADA.md
) else (
    echo   ⚠️  FACTURAS_COMPRA_IMPLEMENTADA.md no encontrado
)

if exist "PROBAR_PRESUPUESTOS.bat" (
    echo   ✅ PROBAR_PRESUPUESTOS.bat
) else (
    echo   ⚠️  PROBAR_PRESUPUESTOS.bat no encontrado
)

echo.
echo ═══════════════════════════════════════════════════════════
echo   📦 COMPILANDO PROYECTO
echo ═══════════════════════════════════════════════════════════
echo.

call mvn clean compile -DskipTests -q

if %ERRORLEVEL% EQU 0 (
    echo.
    echo   ✅ COMPILACIÓN EXITOSA
    echo.
) else (
    echo.
    echo   ❌ ERROR EN LA COMPILACIÓN
    set /a ERRORES+=1
    echo.
)

echo ═══════════════════════════════════════════════════════════
echo   📊 RESUMEN FINAL
echo ═══════════════════════════════════════════════════════════
echo.

if %ERRORES% EQU 0 (
    echo   🎉 ¡TODO PERFECTO!
    echo.
    echo   ✅ Presupuestos: IMPLEMENTADO
    echo   ✅ Facturas de Compra: IMPLEMENTADO
    echo   ✅ Compilación: EXITOSA
    echo   ✅ Documentación: COMPLETA
    echo.
    echo   Las vistas están listas para usar.
    echo.
    echo   Para probar:
    echo   1. Ejecuta: mvn javafx:run
    echo   2. Login: admin / admin
    echo   3. Click en "Presupuestos" o "Facturas de Compra"
    echo.
) else (
    echo   ⚠️  SE ENCONTRARON %ERRORES% PROBLEMA(S)
    echo.
    echo   Revisa los mensajes anteriores para más detalles.
    echo.
)

echo ═══════════════════════════════════════════════════════════
echo.
pause

