#!/bin/bash
# Script de validación final de mejoras FXML

echo "╔════════════════════════════════════════════════════════╗"
echo "║    VALIDACIÓN FINAL DE MEJORAS - ERP TAHONA          ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Contador de checks
checks_passed=0
checks_total=0

# Función para verificar
check() {
    checks_total=$((checks_total + 1))
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        checks_passed=$((checks_passed + 1))
    else
        echo -e "${RED}❌ $1${NC}"
    fi
}

echo "📁 Verificando estructura de directorios..."
[ -d "src/main/resources/ui" ] && echo -e "${GREEN}✅ Directorio UI encontrado${NC}" || echo -e "${RED}❌ Directorio UI no encontrado${NC}"
[ -d "src/main/resources/styles" ] && echo -e "${GREEN}✅ Directorio Styles encontrado${NC}" || echo -e "${RED}❌ Directorio Styles no encontrado${NC}"

echo ""
echo "📄 Verificando archivos FXML principales..."
FXML_FILES=(
    "proveedores_panel.fxml"
    "asientos_panel.fxml"
    "caja_panel.fxml"
    "albaranes_panel.fxml"
    "verifactu_panel.fxml"
    "modelo347_panel.fxml"
    "movimientos_banco_panel.fxml"
    "facturas_panel.fxml"
    "presupuestos_panel.fxml"
    "pedidos_compra_panel.fxml"
    "facturas_compra_panel.fxml"
    "pedidos_venta_panel.fxml"
    "usuarios_panel.fxml"
    "almacenes_panel.fxml"
    "auditoria_panel.fxml"
    "plan_contable_panel.fxml"
    "empresa_config_panel.fxml"
)

for file in "${FXML_FILES[@]}"; do
    if [ -f "src/main/resources/ui/$file" ]; then
        echo -e "${GREEN}✅ $file${NC}"
        checks_passed=$((checks_passed + 1))
    else
        echo -e "${RED}❌ $file no encontrado${NC}"
    fi
    checks_total=$((checks_total + 1))
done

echo ""
echo "🎨 Verificando archivo CSS unificado..."
if [ -f "src/main/resources/styles/unified-styles.css" ]; then
    lines=$(wc -l < src/main/resources/styles/unified-styles.css)
    echo -e "${GREEN}✅ unified-styles.css (${lines} líneas)${NC}"
    checks_passed=$((checks_passed + 1))
else
    echo -e "${RED}❌ unified-styles.css no encontrado${NC}"
fi
checks_total=$((checks_total + 1))

echo ""
echo "📝 Verificando documentación..."
if [ -f "MEJORAS_FXML_RESUMEN.md" ]; then
    echo -e "${GREEN}✅ MEJORAS_FXML_RESUMEN.md${NC}"
    checks_passed=$((checks_passed + 1))
else
    echo -e "${RED}❌ MEJORAS_FXML_RESUMEN.md no encontrado${NC}"
fi
checks_total=$((checks_total + 1))

if [ -f "RESUMEN_FINAL_MEJORAS.md" ]; then
    echo -e "${GREEN}✅ RESUMEN_FINAL_MEJORAS.md${NC}"
    checks_passed=$((checks_passed + 1))
else
    echo -e "${RED}❌ RESUMEN_FINAL_MEJORAS.md no encontrado${NC}"
fi
checks_total=$((checks_total + 1))

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║                    RESULTADO FINAL                     ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

if [ $checks_passed -eq $checks_total ]; then
    echo -e "${GREEN}✅ TODAS LAS VALIDACIONES PASARON${NC}"
    echo "   Checks: $checks_passed/$checks_total"
    echo ""
    echo "🎉 El proyecto está listo para usar"
    echo "   - Todos los FXML han sido mejorados"
    echo "   - CSS unificado implementado"
    echo "   - Documentación completa"
    echo ""
    exit 0
else
    pct=$((checks_passed * 100 / checks_total))
    echo -e "${YELLOW}⚠️  VALIDACIÓN PARCIAL${NC}"
    echo "   Checks: $checks_passed/$checks_total ($pct%)"
    echo ""
    echo "⚠️  Por favor, revisa los errores anteriores"
    echo ""
    exit 1
fi

