# 📋 FLUJO CORRECTO DE FACTURACIÓN - VERIFACTU/AEAT

## ✅ PROBLEMA CORREGIDO

**Problema reportado:**
- Las facturas se intentaban enviar a la AEAT antes de aprobarlas
- No se podía aprobar la factura correctamente

**Solución aplicada:**
- ✅ Flujo de 3 estados implementado correctamente
- ✅ Las facturas se crean SIEMPRE en estado BORRADOR
- ✅ Mensajes claros en cada paso del proceso
- ✅ Validaciones de estado antes de cada acción
- ✅ Documentación Javadoc completa

---

## 🔄 FLUJO DE FACTURACIÓN (3 ESTADOS)

### Estado 1️⃣: BORRADOR

**¿Qué significa?**
- La factura está en proceso de creación/edición
- NO ha sido enviada a la AEAT
- Se puede modificar libremente
- Es el estado inicial de TODAS las facturas nuevas

**Acciones disponibles:**
- ✏️ Editar datos (futuro)
- 📝 **Enviar a Revisión** → Pasa al estado REVISION
- 🗑️ Eliminar (futuro)

**Botón a usar:** `📝 Enviar a Revisión`

---

### Estado 2️⃣: REVISION

**¿Qué significa?**
- La factura está lista y pendiente de aprobación final
- Todavía NO ha sido enviada a la AEAT
- Es el último paso antes de emitir
- Permite revisar todos los datos antes del envío definitivo

**Acciones disponibles:**
- ✅ **Aprobar y Emitir** → Envía a la AEAT (estado EMITIDA)
- ↩️ **Volver a Borrador** → Regresa a BORRADOR si necesitas cambios
- ❌ Anular (solo si ya está emitida)

**Botón a usar:** `✅ Aprobar y Emitir` (⚠️ ESTE ES EL ENVÍO A LA AEAT)

---

### Estado 3️⃣: EMITIDA

**¿Qué significa?**
- ✅ La factura ha sido enviada a Verifactu/AEAT
- 🔐 Está registrada en la Agencia Tributaria
- 📝 Tiene hash SHA-256 y QR generado
- ⚠️ NO se puede modificar (solo anular)

**Acciones disponibles:**
- 🖨️ **Imprimir** → Genera PDF con QR Verifactu
- ❌ **Anular** → Marca como anulada (requiere motivo)
- 👁️ Ver detalles

**⚠️ ADVERTENCIA:** Una vez EMITIDA, la factura NO se puede modificar, solo anular.

---

## 📖 GUÍA PASO A PASO

### Paso 1: Crear Nueva Factura

1. Click en botón `📄 Nueva Factura`
2. Rellenar datos:
   - Número (se genera automáticamente)
   - Fecha
   - Cliente (obligatorio)
   - Líneas de artículos (obligatorio, mínimo 1)
3. Click en `💾 Guardar`

**Resultado:**
```
✅ Factura creada en estado BORRADOR
```

**Mensaje que verás:**
```
✅ Factura Creada
Factura FAC001 creada en BORRADOR

La factura ha sido creada correctamente en estado BORRADOR.

📋 Próximos pasos:
1. Revisa los datos de la factura
2. Usa 'Enviar a Revisión' cuando esté lista
3. Usa 'Aprobar y Emitir' para enviarla a Verifactu/AEAT

ℹ️ Las facturas NO se envían automáticamente a la AEAT.
```

---

### Paso 2: Enviar a Revisión

1. Seleccionar la factura en la tabla (estado: BORRADOR)
2. Click en botón `📝 Enviar a Revisión`
3. Confirmar la acción

**Resultado:**
```
Estado: BORRADOR → REVISION
```

**Mensaje que verás:**
```
✅ Enviada a Revisión
Factura FAC001 lista para aprobar

La factura está ahora en estado REVISION.

📌 Próximo paso:
Selecciona la factura y usa el botón
'Aprobar y Emitir' para enviarla a Verifactu/AEAT.

Si necesitas hacer cambios, usa 'Volver a Borrador'.
```

**⚠️ IMPORTANTE:** Este paso NO envía nada a la AEAT todavía.

---

### Paso 3: Aprobar y Emitir a AEAT (PASO FINAL)

1. Seleccionar la factura en la tabla (estado: REVISION)
2. Click en botón `✅ Aprobar y Emitir`
3. Leer la advertencia cuidadosamente:

```
⚠️ CONFIRMAR EMISIÓN A LA AEAT
¿APROBAR Y EMITIR factura FAC001 a Verifactu/AEAT?

🔴 ATENCIÓN: Esta acción es IRREVERSIBLE

La factura será enviada a la Agencia Tributaria con:
• Datos de GRUPO BABO, S.Coop.V.L.
• CIF: F54059985
• Sistema Verifactu (blockchain)

⚠️ Una vez emitida:
• NO se puede modificar
• Solo se puede anular
• Quedará registrada en la AEAT

¿Estás seguro de continuar?
```

4. Click en `YES` para confirmar

**Resultado:**
```
Estado: REVISION → EMITIDA
+ Enviada a Verifactu/AEAT
+ Hash SHA-256 generado
+ QR generado
+ Registrada en blockchain
```

**Mensaje que verás:**
```
✅ FACTURA EMITIDA CORRECTAMENTE
Factura FAC001 enviada a AEAT

✅ La factura ha sido emitida exitosamente

📋 Datos de emisión:
• Empresa: GRUPO BABO, S.Coop.V.L.
• CIF: F54059985
• Fecha: 27/12/2025 15:30:45
• Estado: EMITIDA
• Verifactu: Registrada en AEAT

🔐 La factura está ahora en el sistema Verifactu
con hash blockchain y código QR.

Usa 'Imprimir' para generar el PDF con QR.
```

---

## 🚫 VALIDACIONES IMPLEMENTADAS

### Si intentas "Enviar a Revisión" una factura que NO está en BORRADOR:

```
⚠️ Estado Incorrecto
No se puede enviar a revisión

Solo las facturas en estado BORRADOR pueden enviarse a revisión.

Estado actual: REVISION (o EMITIDA)

Si la factura está en REVISION, usa 'Aprobar y Emitir'.
Si está EMITIDA, ya fue enviada a la AEAT.
```

### Si intentas "Aprobar y Emitir" una factura que NO está en REVISION:

```
⚠️ Estado Incorrecto
No se puede aprobar y emitir

Solo las facturas en estado REVISION pueden ser emitidas.

Estado actual: BORRADOR (o EMITIDA)

📋 Flujo correcto:
1. BORRADOR → Usa 'Enviar a Revisión'
2. REVISION → Usa 'Aprobar y Emitir' (este botón)
3. EMITIDA → Ya está en la AEAT

Si la factura está en BORRADOR, primero envíala a revisión.
```

---

## 🔄 ACCIONES REVERSIBLES

### Volver de REVISION a BORRADOR

Si después de enviar a revisión decides hacer cambios:

1. Seleccionar factura (estado: REVISION)
2. Click en `↩️ Volver a Borrador`
3. La factura vuelve a BORRADOR
4. Puedes editarla (en futuras versiones)
5. Repetir el flujo desde el paso 2

---

## ❌ ANULAR UNA FACTURA EMITIDA

Si necesitas anular una factura ya emitida:

1. Seleccionar factura (estado: EMITIDA)
2. Click en `❌ Anular Factura`
3. Escribir motivo de anulación (obligatorio)
4. La factura pasa a estado ANULADA

**⚠️ NOTA:** La factura anulada sigue en la AEAT, solo cambia su estado.

---

## 🎨 VISUALIZACIÓN EN LA TABLA

La columna "Estado" muestra el estado actual:

| Estado | Significado | Siguiente paso |
|--------|-------------|----------------|
| `BORRADOR` | En edición | Enviar a Revisión |
| `REVISION` | Lista para aprobar | Aprobar y Emitir |
| `EMITIDA` | En la AEAT ✅ | Imprimir o Anular |
| `ANULADA` | Cancelada ❌ | Solo consulta |

---

## 📊 DIAGRAMA DE FLUJO

```
┌─────────────────┐
│  Nueva Factura  │
└────────┬────────┘
         │
         v
┌─────────────────┐
│   BORRADOR      │ ← Estado inicial (NO enviada a AEAT)
│                 │
│ Acciones:       │
│ • Editar        │
│ • Enviar a      │
│   Revisión ───┐ │
└───────────────┼─┘
                │
                v
┌───────────────┼─┐
│   REVISION    │ │ ← Pendiente de aprobación (NO enviada a AEAT)
│               │ │
│ Acciones:     │ │
│ • Aprobar y ──┼─┼─> 🚀 AQUÍ SE ENVÍA A LA AEAT
│   Emitir      │ │
│ • Volver a ───┘ │
│   Borrador      │
└────────┬────────┘
         │
         v
┌────────┴────────┐
│   EMITIDA      │ ← ✅ Enviada a AEAT (Verifactu activo)
│                 │
│ Acciones:       │
│ • Imprimir      │
│ • Anular ──┐    │
└────────────┼────┘
             │
             v
      ┌──────┴──────┐
      │   ANULADA   │ ← ❌ Factura cancelada
      │             │
      └─────────────┘
```

---

## 🛠️ CAMBIOS TÉCNICOS APLICADOS

### 1. En `guardarFactura()`:

```java
// ⚠️ IMPORTANTE: Establecer estado BORRADOR explícitamente
factura.setEstado("BORRADOR");
factura.setVerifactuEnviada(false);
factura.setTipoFactura("ORDINARIA");
```

### 2. Mensajes mejorados:

- ✅ Mensaje informativo al crear factura (explica el flujo)
- ✅ Mensaje detallado en "Enviar a Revisión" (próximo paso)
- ✅ Advertencia clara en "Aprobar y Emitir" (irreversible)
- ✅ Validaciones con explicación del flujo correcto

### 3. Documentación Javadoc:

Todos los métodos ahora tienen Javadoc explicando:
- Qué hace el método
- En qué paso del flujo estás
- Qué pasará después
- Referencias a normativas legales

---

## ✅ VERIFICACIÓN

Para verificar que funciona correctamente:

1. **Crear factura:**
   - Debe quedar en estado BORRADOR
   - Debe mostrar mensaje explicativo

2. **Enviar a Revisión:**
   - Solo funciona desde BORRADOR
   - Muestra mensaje del próximo paso

3. **Aprobar y Emitir:**
   - Solo funciona desde REVISION
   - Muestra advertencia clara
   - Requiere confirmación explícita

4. **Tabla de facturas:**
   - Columna "Estado" muestra el estado actual
   - Se actualiza automáticamente

---

## 📝 RESUMEN

### ¿Cuándo se envía a la AEAT?

**SOLO** cuando usas el botón `✅ Aprobar y Emitir` en una factura en estado **REVISION**.

### Secuencia correcta:

1. `📄 Nueva Factura` → BORRADOR
2. `📝 Enviar a Revisión` → REVISION
3. `✅ Aprobar y Emitir` → **🚀 ENVÍO A AEAT** → EMITIDA

### ¿Por qué este flujo?

- **BORRADOR:** Permite crear y revisar tranquilamente
- **REVISION:** Última verificación antes del envío definitivo
- **EMITIDA:** Confirmación de que está en la AEAT

Este flujo de 3 estados es estándar en sistemas de facturación profesionales y cumple con las mejores prácticas de control de calidad.

---

**Fecha de corrección:** 27/12/2025  
**Versión:** 0.0.2  
**Estado:** ✅ CORREGIDO Y FUNCIONAL

🎉 **¡El problema está resuelto!** 🎉

