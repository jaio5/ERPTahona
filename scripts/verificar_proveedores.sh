#!/bin/bash
# Script de verificación del módulo de proveedores - Bash version

echo ""
echo "════════════════════════════════════════════════════════════════════"
echo "  VERIFICACIÓN DEL MÓDULO DE PROVEEDORES"
echo "════════════════════════════════════════════════════════════════════"
echo ""

# Array de archivos a verificar
files=(
    "src/main/resources/ui/proveedores_panel.fxml"
    "src/main/resources/ui/proveedor_form.fxml"
    "src/main/java/alicanteweb/erp/controller/ProveedorController.java"
    "src/main/java/alicanteweb/erp/controller/ProveedorFormController.java"
    "src/main/java/alicanteweb/erp/service/ProveedorService.java"
    "src/main/java/alicanteweb/erp/repository/ProveedorRepository.java"
    "src/main/java/alicanteweb/erp/entities/Proveedor.java"
)

echo "[1/3] Verificando archivos necesarios..."
missing=0

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        size=$(wc -c < "$file")
        echo "  ✓ $file ($size bytes)"
    else
        echo "  ✗ $file - NO ENCONTRADO"
        missing=$((missing + 1))
    fi
done

echo ""
echo "[2/3] Verificando que los archivos no están vacíos..."
for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        size=$(wc -c < "$file")
        if [ $size -eq 0 ]; then
            echo "  ✗ $file - ARCHIVO VACÍO"
            missing=$((missing + 1))
        fi
    fi
done

if [ $missing -eq 0 ]; then
    echo "  ✓ Todos los archivos tienen contenido"
fi

echo ""
echo "[3/3] Compilando proyecto..."
mvn clean compile -q -DskipTests

if [ $? -eq 0 ]; then
    echo "  ✓ Compilación exitosa"
    echo ""
    echo "════════════════════════════════════════════════════════════════════"
    echo "  ✅ VERIFICACIÓN COMPLETADA - TODO CORRECTO"
    echo "════════════════════════════════════════════════════════════════════"
    echo ""
    echo "Para ejecutar la aplicación:"
    echo "  mvn javafx:run"
    echo ""
    exit 0
else
    echo "  ✗ Error en compilación"
    echo ""
    echo "════════════════════════════════════════════════════════════════════"
    echo "  ❌ VERIFICACIÓN FALLIDA"
    echo "════════════════════════════════════════════════════════════════════"
    echo ""
    exit 1
fi

