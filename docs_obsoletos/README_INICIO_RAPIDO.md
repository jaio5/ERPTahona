# ✅ APLICACIÓN ERP LISTA PARA USAR

## 🎉 Estado Actual

**✅ COMPILACIÓN EXITOSA**  
**✅ APLICACIÓN ARRANCANDO CORRECTAMENTE**  
**✅ TODOS LOS ERRORES CORREGIDOS**

---

## 🚀 Cómo Iniciar la Aplicación

### Opción 1: Script de Arranque Rápido (RECOMENDADO)
```batch
ARRANCAR.bat
```

Este script:
1. ✅ Inicia MySQL automáticamente
2. ✅ Compila el proyecto
3. ✅ Arranca la aplicación JavaFX

### Opción 2: Arranque Manual
```batch
mvn javafx:run
```

---

## 🔑 Credenciales de Acceso

```
Usuario:     admin
Contraseña:  admin
```

---

## 📊 Inicializar el Plan Contable (IMPORTANTE)

**Antes de usar el módulo de contabilidad**, ejecuta:

```batch
INIT_PLAN_CONTABLE.bat
```

Este script cargará 28 cuentas contables esenciales:
- **430** - Clientes
- **700** - Ventas de mercaderías
- **477** - IVA repercutido
- **600** - Compras de mercaderías
- **472** - IVA soportado
- **400** - Proveedores
- **572** - Bancos c/c
- **570** - Caja
- Y 20 cuentas adicionales

---

## 📁 Archivos Creados

### Corrección de Errores
- ✅ `CORRECCION_ERRORES_COMPILACION.md` - Documentación completa de los cambios

### Scripts SQL
- ✅ `init_plan_contable.sql` - Script para inicializar el Plan General Contable

### Scripts de Arranque
- ✅ `ARRANCAR.bat` - Arranque rápido de la aplicación
- ✅ `INIT_PLAN_CONTABLE.bat` - Inicializar Plan Contable

---

## 🔧 Cambios Técnicos Realizados

### 1. AsientoAutomaticoService.java
```
✅ Cambiado: AsientoContableLinea → LineaAsiento
✅ Cambiado: PlanContableRepository → PlanCuentasRepository
✅ Cambiado: PlanContable → PlanCuentas
✅ Actualizado: Todos los métodos de generación de asientos
```

### 2. BackupService.java
```
✅ Agregado: Manejo de IOException en lambda
✅ Corregido: listarBackups() ahora maneja errores correctamente
```

---

## 📋 Módulos Disponibles

### ✅ Módulos Operativos
- **Clientes** - Gestión completa de clientes
- **Artículos** - Gestión de productos y stock
- **Facturas** - Emisión de facturas de venta
- **Proveedores** - Gestión de proveedores
- **Facturas Compra** - Registro de compras
- **Albaranes** - Gestión de albaranes
- **Presupuestos** - Creación de presupuestos
- **Caja** - Movimientos de caja
- **Contabilidad** - Asientos contables automáticos
- **Auditoría** - Registro de acciones
- **Usuarios** - Gestión de usuarios
- **Verifactu** - Integración con AEAT (simulado)

---

## 🔍 Verificación del Sistema

### Base de Datos
```sql
-- Verificar conexión
USE tahona;

-- Ver usuarios
SELECT username, enabled, bloqueado FROM users;

-- Ver clientes
SELECT COUNT(*) FROM clientes;

-- Ver artículos
SELECT COUNT(*) FROM articulos;

-- Ver plan contable (después de ejecutar INIT_PLAN_CONTABLE.bat)
SELECT codigo, nombre, tipo FROM plan_cuentas ORDER BY codigo;
```

### Logs de la Aplicación
Los logs se mostrarán en la consola al arrancar. Busca:
- ✅ `Contexto de Spring Boot inicializado correctamente`
- ✅ `Pantalla de login cargada correctamente`
- ⚠️ Si ves "Usuario bloqueado", ejecuta `DESBLOQUEAR_ADMIN.bat`

---

## 🐛 Solución de Problemas

### La aplicación no arranca
1. **Verifica MySQL:**
   ```batch
   sc query MySQL80
   ```
   Si no está corriendo: `net start MySQL80`

2. **Verifica la base de datos:**
   ```sql
   SHOW DATABASES LIKE 'tahona';
   ```

3. **Verifica las credenciales** en `src/main/resources/application.properties`

### No puedo hacer login
1. Ejecuta `DESBLOQUEAR_ADMIN.bat`
2. O manualmente:
   ```sql
   UPDATE users SET bloqueado=0, intentos_fallidos=0 WHERE username='admin';
   ```

### Errores al generar asientos contables
1. Ejecuta `INIT_PLAN_CONTABLE.bat` para cargar el plan contable
2. Verifica que existen las cuentas básicas:
   ```sql
   SELECT codigo FROM plan_cuentas WHERE codigo IN ('430','700','477','600','472','400','572','570');
   ```

---

## 📊 Funcionalidades de Contabilidad

### Asientos Automáticos
El sistema genera automáticamente asientos contables para:

1. **Factura de Venta**
   - DEBE: 430 (Clientes)
   - HABER: 700 (Ventas)
   - HABER: 477 (IVA Repercutido)

2. **Cobro de Factura**
   - DEBE: 572 (Banco)
   - HABER: 430 (Clientes)

3. **Factura de Compra**
   - DEBE: 600 (Compras)
   - DEBE: 472 (IVA Soportado)
   - HABER: 400 (Proveedores)

4. **Pago a Proveedor**
   - DEBE: 400 (Proveedores)
   - HABER: 572 (Banco)

5. **Movimientos de Caja**
   - Ingresos: DEBE Caja / HABER Ingresos
   - Gastos: DEBE Gastos / HABER Caja

---

## 📈 Próximos Pasos

1. ✅ **Arrancar la aplicación** con `ARRANCAR.bat`
2. ✅ **Inicializar el Plan Contable** con `INIT_PLAN_CONTABLE.bat`
3. ✅ **Probar el login** (admin/admin)
4. ✅ **Explorar los módulos** disponibles
5. ✅ **Crear registros de prueba** (clientes, artículos, facturas)
6. ✅ **Verificar la generación automática de asientos**

---

## 💡 Consejos

- **Backups automáticos**: El sistema hace backup diario a las 2:00 AM
- **Auditoría**: Todas las acciones quedan registradas
- **Verifactu**: Actualmente en modo simulado (no envía a AEAT real)
- **Plan Contable**: Basado en el PGC de España

---

## 📞 Documentación Adicional

- `CORRECCION_ERRORES_COMPILACION.md` - Detalles técnicos de las correcciones
- `INDICE_DOCUMENTACION.md` - Índice de toda la documentación
- `GUIA_USO_MODULOS.md` - Guía de uso de cada módulo

---

**🎉 ¡La aplicación está completamente funcional y lista para usar!**

Última actualización: 13 de enero de 2026

