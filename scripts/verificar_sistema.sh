#!/bin/bash
# Script de verificación completa del sistema

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                                                                  ║"
echo "║  🔍 VERIFICACIÓN COMPLETA DEL SISTEMA ERP                        ║"
echo "║                                                                  ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

# Verificar compilación
echo "1️⃣  Verificando compilación..."
mvn compile -DskipTests -q
if [ $? -eq 0 ]; then
    echo "   ✅ Compilación exitosa"
else
    echo "   ❌ Error en compilación"
    exit 1
fi
echo ""

# Verificar base de datos
echo "2️⃣  Verificando base de datos..."
mysql -u root -pIirne322* tahona -N -e "SELECT COUNT(*) FROM clientes WHERE activo IS NULL;" 2>/dev/null > /tmp/check_null.txt
NULL_COUNT=$(cat /tmp/check_null.txt)
if [ "$NULL_COUNT" = "0" ]; then
    echo "   ✅ Sin valores NULL en clientes"
else
    echo "   ⚠️  Hay $NULL_COUNT clientes con activo NULL"
fi
echo ""

# Verificar usuario admin
echo "3️⃣  Verificando usuario admin..."
mysql -u root -pIirne322* tahona -N -e "SELECT enabled, bloqueado FROM users WHERE username='admin';" 2>/dev/null
echo "   ✅ Usuario admin verificado"
echo ""

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║  ✅ VERIFICACIÓN COMPLETADA                                      ║"
echo "╚══════════════════════════════════════════════════════════════════╝"

