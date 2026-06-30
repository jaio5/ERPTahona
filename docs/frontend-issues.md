# Revisión Frontend — ERPTahona
*Revisión realizada: 2026-06-30 — navegación manual con Playwright/Chromium*

---

## Estado general por módulo

| Módulo | Estado | Notas |
|--------|--------|-------|
| Login / Logout | ✅ OK | |
| Dashboard | ⚠️ Bug | Alertas incorrectas para facturas en borrador |
| Planificador | ⚠️ Bug | Tildes faltantes en textos |
| Calendario | ✅ OK | |
| Clientes — lista y detalle | ✅ OK | |
| Artículos — lista y detalle | ✅ OK | |
| Facturas — lista | ✅ OK | |
| Facturas — detalle (por ID numérico) | ✅ OK | |
| Facturas — detalle (por número, ej. F-GEN-…) | ❌ Error 500 | Ver BUG-001 |
| Albaranes — lista y nuevo | ✅ OK | |
| Albaranes — detalle | ⚠️ Aviso | ALB-11 sin almacén (dato histórico) |
| Pedidos venta — lista | ✅ OK | |
| Pedidos venta — detalle (por ID numérico) | ⚠️ Bug | Total siempre 0,00 € (BUG-002) |
| Pedidos venta — detalle (por número, ej. PED-…) | ❌ Error 500 | Ver BUG-001 |
| Presupuestos — lista y detalle | ⚠️ Bug | Encoding roto en datos de BD (BUG-004) |
| Proveedores | ✅ OK | |
| Compras | ✅ OK | |
| Almacén / almacenes | ✅ OK | |
| Producción — recetas | ✅ OK | |
| Producción — órdenes | ⚠️ Aviso | OP-2026-0001 sin receta ni artículo (dato de prueba) |
| Producción — APPCC | ✅ OK | |
| Reparto — rutas y hojas de ruta | ✅ OK | |
| Finanzas — tesorería / extractos | ✅ OK | |
| Contabilidad — balance | ✅ OK | |
| Administración — usuarios | ✅ OK | |
| Administración — empresa, auditoría, backups | ✅ OK | |
| Fiscal — Modelo 347 | ✅ OK | |
| Fiscal — VeriFactu | ✅ OK | Sin evidencias (esperado en dev) |
| Informes — Ventas del mes | ⚠️ Bug | "JUNE 2026" en inglés + tabla incompleta (BUG-005, BUG-006) |
| Informes — Inventario | ⚠️ Aviso | Coste vacío para artículo "patatas" |
| Informes — Producción del mes | ✅ OK | |
| Informes — Reparto del mes | ✅ OK | Sin hojas de ruta este mes |
| Informes — Trazabilidad | ✅ OK | |
| Informes — Rentabilidad | ⚠️ Aviso | Cabeceras sin tilde: "CODIGO", "CATEGORIA" + encoding BUG-004 |
| Administración — Empresa | ✅ OK | |
| Administración — Auditoría | ✅ OK | |
| Administración — Búsqueda global | ✅ OK | |
| Administración — Backups | ✅ OK | |
| Administración — Importar datos | ✅ OK | |

---

## Bugs encontrados

### BUG-001 — Error 500 al acceder entidades por número de referencia (string ID)
**Severidad:** Alta  
**Módulos afectados:** Facturas, Pedidos venta (y posiblemente Albaranes, Presupuestos)

Al introducir en la URL el número de referencia en lugar del ID numérico, el servidor devuelve 500:
- `/web/facturas/F-GEN-2026-0006` → "Error interno del servidor"
- `/web/pedidos-venta/PED-2026-00001` → "Error interno del servidor"

**Causa probable:** El `@PathVariable` está tipado como `Long`. Spring intenta convertir el string `F-GEN-2026-0006` a `Long`, lanza `NumberFormatException`, y no hay handler para ese error → 500 en lugar de 404.

**Cómo reproducir:** Escribir la URL directamente en el navegador con el número de referencia (no el ID).

**Fix sugerido:** Añadir un `@ExceptionHandler(NumberFormatException.class)` en el `ControllerAdvice` global que devuelva 404, o cambiar los path variables afectados para aceptar `String` y hacer la búsqueda por número si no es numérico.

---

### BUG-002 — Total del pedido de venta siempre muestra 0,00 €
**Severidad:** Alta  
**Módulo afectado:** Pedidos venta

El pedido PED-2026-00001 tiene una línea: "patatas" × 54 uds × 1,20 € = **64,80 €**. Sin embargo:
- El panel lateral "TOTAL PEDIDO" muestra **0,00 €**
- El campo TOTAL de la cabecera muestra **—**
- El pie de la tabla de líneas "Total pedido" muestra **—**

**Causa probable:** El campo `total` de la entidad `Pedido` no se recalcula al guardar las líneas, o la plantilla Thymeleaf renderiza el total desde el campo persistido (que permanece a null/0) en lugar de calcularlo sobre la colección de líneas.

**Cómo reproducir:** Ir a `/web/pedidos-venta/1`.

**Fix sugerido:** Recalcular `pedido.total` en el servicio al guardar/actualizar líneas, igual que se hace en `FacturaService` con las líneas de factura.

---

### BUG-003 — Dashboard: alertas de "factura vencida" para facturas en estado BORRADOR
**Severidad:** Media  
**Módulo afectado:** Dashboard / Panel de trabajo

El dashboard muestra alertas de "Factura vencida" para facturas con estado **BORRADOR** que tienen una fecha de vencimiento pasada: F-GEN-2026-0002, 0004, 0005, 0007, 0008. Solo debería alertar de facturas EMITIDAS no cobradas con fecha de vencimiento pasada.

**Causa probable:** La query de alertas filtra por `fecha_vencimiento < NOW()` sin añadir la condición `AND estado != 'BORRADOR'` (o `AND estado = 'EMITIDA'`).

**Fix sugerido:** En `AlertaRestController` o el servicio de alertas, añadir filtro de estado en la consulta de facturas vencidas.

---

### BUG-004 — Encoding roto en datos de la base de datos
**Severidad:** Media  
**Módulo afectado:** Presupuestos (y posiblemente otros)

El presupuesto PRE-2026-00001 muestra **"Art❦culo de prueba"** en lugar de "Artículo de prueba". El carácter `í` aparece garbled.

**Causa probable:** Inconsistencia de charset entre la conexión JDBC y la base de datos MySQL. Los datos de prueba se insertaron con un charset diferente al configurado en la conexión (probablemente latin1 vs utf8mb4), o la cadena de conexión no especifica `characterEncoding=UTF-8`.

**Fix sugerido:** Verificar que la URL de conexión incluye `?characterEncoding=UTF-8&useUnicode=true` y que la BD y las tablas usan `utf8mb4`. También revisar los scripts SQL de datos de prueba para asegurarse de que los ficheros `.sql` de Flyway se guardan en UTF-8.

---

### BUG-005 — Informe "Ventas del mes": subtítulo del mes en inglés
**Severidad:** Baja  
**Módulo afectado:** Informes → Ventas del mes

El informe muestra **"JUNE 2026"** como subtítulo bajo el total facturado, en lugar de "JUNIO 2026".

**Causa probable:** Se usa `Month.getDisplayName()` o `DateTimeFormatter` sin pasar `Locale.forLanguageTag("es")` / `new Locale("es", "ES")`.

**Fix sugerido:** En el controlador o la plantilla Thymeleaf, formatear la fecha con locale español explícito, por ejemplo:
```java
month.getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase()
```

---

### BUG-006 — Informe "Ventas del mes": tabla "Ventas por día" sin columna de importe
**Severidad:** Media  
**Módulo afectado:** Informes → Ventas del mes

La tabla "Ventas por día" muestra los 30 días del mes en una columna "DÍA" pero la columna de importe no se renderiza — solo se ven los números de los días sin ningún valor de facturación a la derecha. La tabla parece truncada o la segunda columna se desborda fuera del viewport visible.

**Cómo reproducir:** Ir a `/web/reportes/ventas-mes` y bajar a la sección "Ventas por día".

**Fix sugerido:** Revisar la plantilla Thymeleaf de ventas-mes: la segunda columna (importe) probablemente tiene un problema de layout (overflow hidden, ancho incorrecto) o los datos del mapa no se están iterando correctamente en el template.

---

### BUG-007 — Planificador: tildes faltantes en textos de UI
**Severidad:** Baja  
**Módulo afectado:** Planificador

Dos textos de la vista del planificador tienen las tildes incorrectas:
- Card de resumen: **"Ordenes prod."** → debería ser "Órdenes prod."
- Cabecera de tabla: **"Articulo"** → debería ser "Artículo"

**Fix sugerido:** Buscar en la plantilla Thymeleaf del planificador y corregir los literales.

---

## Avisos (no son bugs, pero conviene revisar)

### AVISO-A — Albarán ALB-2026-000011 sin almacén asignado
El campo ALMACÉN del albarán ALB-2026-000011 muestra "—". Es probable que este albarán fue creado antes de que el campo fuera obligatorio, o que el almacén asociado fue eliminado. No es un bug de la UI, pero conviene decidir si el campo debe ser obligatorio en el formulario de nuevo albarán.

### AVISO-B — Orden de producción OP-2026-0001 sin receta ni artículo
La lista de órdenes de producción muestra "—" para RECETA y ARTÍCULO en OP-2026-0001. Es probable que sean datos de prueba incompletos creados manualmente. Considerar si la validación del formulario de nueva OP debe exigir al menos uno de estos campos.

### AVISO-C — Columna "Coste" vacía para artículo "patatas" en informe de inventario
El artículo "patatas" no muestra ningún valor en la columna "Coste" del informe de inventario (mientras otros artículos muestran "0" o un valor). Puede ser NULL en la BD en lugar de 0. La plantilla debería tratar NULL como "0" con `th:text="${coste ?: 0}"` o similar.

### AVISO-D — Informe Rentabilidad: cabeceras de tabla sin tilde
Las columnas "CODIGO" y "CATEGORIA" en `/web/reportes/rentabilidad` deberían ser "CÓDIGO" y "CATEGORÍA". Cosmético, mismo patrón que BUG-007.

### AVISO-E — URLs directas con número de referencia no están documentadas/expuestas
No existe ruta pública para `/web/stock`, `/web/extracto-bancario` ni `/web/balance`. Las URLs correctas son `/web/almacenes`, `/web/tesoreria/extractos` y `/web/contabilidad/balance`. Si se usan estas rutas en emails, documentación o enlaces externos, conviene añadir redirects o documentar las URLs correctas.

---

---

## Bugs VeriFactu / Facturación (revisión 2026-06-30)

### BUG-008 — PDF de facturas: error 500 al descargar ✅ CORREGIDO
**Severidad:** Alta  
**Módulo afectado:** Facturas → Descargar PDF

El endpoint `GET /web/facturas/{id}/pdf` devolvía 500 para todas las facturas. El PDF de albaranes sí funcionaba.

**Causas (tres):)**
1. **`#numbers.parseDecimal` no existe en Thymeleaf 3.1.3.** El template `pdf/factura.html` usaba `linea.descuento.compareTo(#numbers.parseDecimal('0','POINT'))` y `factura.retencionIrpf.compareTo(#numbers.parseDecimal('0','POINT'))`. Este método fue eliminado en Thymeleaf 3.x. Cuando `retencionIrpf != null` (valor `0.00` por `@ColumnDefault`), Thymeleaf lanzaba `TemplateProcessingException`.
2. **`linea.subtotal` no existe** en `FacturaLinea` (el método se llama `getTotal()`). El template debía usar `linea.total`.
3. **Columna Precio usaba `linea.precio` en lugar de `linea.precioUnitario`**, mostrando 0,00 para líneas con sólo `precioUnitario` configurado.

**Fix aplicado:** `src/main/resources/templates/pdf/factura.html`
- `compareTo(#numbers.parseDecimal(...))` → `.signum() > 0`
- `linea.subtotal` → `linea.total`
- `linea.precio` → `linea.precioUnitario`

---

### BUG-009 — Rectificativa: campo de formulario incorrecto ✅ CORREGIDO
**Severidad:** Alta  
**Módulo afectado:** Facturas → Crear rectificativa

El form `rectificativa.html` tenía `name="motivoRectificacion"` pero el controller espera `@RequestParam String motivo` → `MissingServletRequestParameterException` → 500.

Además, el botón de submit usaba `onsubmit="return confirm(...)"` (violación CSP).

**Fix aplicado:**
- `name="motivoRectificacion"` → `name="motivo"` + `id="motivo"`
- `onsubmit` inline eliminado → `data-confirm="..."` en el botón (procesado por `app.js`)

---

### BUG-010 — Rectificativa: `registrarAnulacionLocal` falla XSD sin VeriFactu configurado ✅ CORREGIDO (pendiente restart)
**Severidad:** Alta  
**Módulo afectado:** Facturas → Crear rectificativa

Al anular la factura original durante la creación de una rectificativa, `VerifactuService.registrarAnulacionLocal` llamaba incondicionalmente a `generarRegistroAnulacionXml`. Sin el NIF del emisor VeriFactu configurado (dev), el XML generado tiene `<NIF></NIF>` vacío, que falla la validación XSD → `IllegalStateException` → rollback de toda la transacción.

**Fix aplicado:** `VerifactuService.registrarAnulacionLocal` — verifica si existe empresa y si `verifactuNifEmisor` está configurado antes de intentar generar el XML; usa hash simplificado si no.

⚠️ **Requiere restart del servidor** para activarse (cambio de clase Java, sin DevTools).

---

## Prioridad de corrección

| Prioridad | Bug | Impacto | Estado |
|-----------|-----|---------|--------|
| 🔴 Alta | BUG-001 Error 500 por string ID | Rompe navegación desde emails/búsquedas | Pendiente |
| 🔴 Alta | BUG-002 Total pedido 0,00€ | Dato financiero incorrecto | Pendiente |
| 🔴 Alta | BUG-008 PDF facturas 500 | Descarga de facturas inoperativa | ✅ Corregido |
| 🔴 Alta | BUG-009 Campo formulario rectificativa | Creación de rectificativas imposible | ✅ Corregido |
| 🔴 Alta | BUG-010 registrarAnulacionLocal XSD | Transacción de rectificativa aborta | ✅ Corregido (restart) |
| 🟡 Media | BUG-003 Alertas de BORRADOR | Ruido en dashboard para el usuario | Pendiente |
| 🟡 Media | BUG-004 Encoding roto | Datos ilegibles en pantalla | Pendiente |
| 🟡 Media | BUG-006 Tabla ventas por día sin importe | Informe incompleto | Pendiente |
| 🟢 Baja | BUG-005 Mes en inglés | Cosmético / i18n | Pendiente |
| 🟢 Baja | BUG-007 Tildes faltantes | Cosmético / ortografía | Pendiente |
