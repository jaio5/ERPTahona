# 📚 Índice de Documentación - ERP Panadería Tahona

**Última actualización:** 13 de enero de 2026

---

## 📖 Documentación Principal

### 1. **README.md** (Raíz del proyecto)
Guía principal con:
- Inicio rápido
- Instalación
- Configuración
- Estructura del proyecto
- Últimas correcciones

---

## 📁 Carpeta `docs/`

### Estado del Proyecto

| Archivo | Descripción |
|---------|-------------|
| **ESTADO_FORMULARIOS.md** | Lista completa de todos los formularios y su estado |
| **FORMULARIOS_CORREGIDOS_FINAL.md** | Resumen ejecutivo de correcciones aplicadas |
| **README_CORRECCIONES.md** | Resumen breve de las últimas correcciones |

### Correcciones Técnicas

| Archivo | Descripción |
|---------|-------------|
| **PROBLEMA_RESUELTO.md** | Última corrección aplicada (cálculo de precios) |
| **CORRECCION_CALCULO_PRECIOS.md** | Detalle técnico del problema del cursor |
| **CONTROLADORES_CORREGIDOS_FINAL.md** | Correcciones en controladores |
| **APLICACION_CORREGIDA.md** | Correcciones de compilación |

### Análisis

| Archivo | Descripción |
|---------|-------------|
| **ANALISIS_FORMULARIOS_PROBLEMAS.md** | Análisis completo de problemas detectados |
| **RESUMEN_CORRECCION.md** | Resumen de todas las correcciones |
| **INDICE_DOCUMENTACION.md** | Este archivo |

---

## 📁 Carpeta `scripts/`

### Scripts SQL

| Archivo | Propósito |
|---------|-----------|
| **init_plan_contable.sql** | Inicializar el plan contable español (28 cuentas) |
| **agregar_campos_articulo.sql** | Migración: agregar campos a artículos |
| **agregar_campos_cliente.sql** | Migración: agregar campos a clientes |

### Scripts de Utilidad

| Archivo | Propósito |
|---------|-----------|
| **INIT_PLAN_CONTABLE.bat** | Ejecutar init_plan_contable.sql automáticamente |

---

## 📁 Carpeta `basesdedatos/`

Backups de la base de datos y scripts de inicialización.

---

## 🗂️ Carpeta `docs_obsoletos/`

Documentación histórica y obsoleta movida aquí para mantener el proyecto limpio.

**Nota:** Esta carpeta contiene más de 80 archivos de documentación antigua que ya no son necesarios pero se conservan por historial.

---

## 🚀 Scripts de Arranque

En la raíz del proyecto:

| Script | Descripción |
|--------|-------------|
| **ARRANCAR.bat** | Arrancar la aplicación (recomendado) |
| **ABRIR_EN_INTELLIJ.bat** | Abrir proyecto en IntelliJ IDEA |
| **INIT_PLAN_CONTABLE.bat** | Inicializar plan contable (primera vez) |

---

## 📊 Estado de los Módulos

### ✅ Completamente Funcionales (7)
1. Clientes
2. Artículos
3. Proveedores
4. Facturas
5. Almacenes
6. Usuarios
7. Auditoría

### ⚠️ Parcialmente Implementados (8)
1. Albaranes - Lista funciona, formularios pendientes
2. Presupuestos - Lista funciona, formularios pendientes
3. Pedidos Venta - Lista funciona, formularios pendientes
4. Pedidos Compra - Lista funciona, formularios pendientes
5. Caja - Lista funciona, formularios pendientes
6. Asientos Contables - Automáticos funcionan, manual pendiente
7. Plan Contable - Carga con script, formulario crear cuenta pendiente
8. VeriFacTu - Simulado, integración real pendiente

**Total:** 47% de formularios completos (7 de 15)

---

## 🔍 Cómo Encontrar Información

### Para arrancar la aplicación:
➡️ **README.md** (raíz) → Sección "Inicio Rápido"

### Para ver qué módulos funcionan:
➡️ **docs/ESTADO_FORMULARIOS.md**

### Para entender las últimas correcciones:
➡️ **docs/README_CORRECCIONES.md** (resumen)  
➡️ **docs/FORMULARIOS_CORREGIDOS_FINAL.md** (completo)

### Para solucionar problemas de compilación:
➡️ **docs/APLICACION_CORREGIDA.md**

### Para ver problemas técnicos resueltos:
➡️ **docs/PROBLEMA_RESUELTO.md**  
➡️ **docs/CORRECCION_CALCULO_PRECIOS.md**

---

## 📝 Convenciones de Documentación

- **✅** = Completamente funcional / Resuelto
- **⚠️** = Parcialmente implementado / Necesita atención
- **❌** = No implementado / Error crítico
- **🔴** = Alta prioridad
- **🟡** = Media prioridad
- **🟢** = Baja prioridad

---

## 🗑️ Limpieza Realizada

**Fecha:** 13 de enero de 2026

Se movieron **80+ archivos obsoletos** a `docs_obsoletos/`:
- Documentación duplicada
- Guías antiguas
- Scripts de prueba obsoletos
- Archivos de log antiguos
- Borradores y versiones anteriores

**Resultado:** Proyecto limpio y organizado con solo la documentación esencial.

---

**Nota:** Si necesitas consultar documentación histórica, revisa la carpeta `docs_obsoletos/`.

