# Issues de Interfaz — ERPTahona

Revisión visual completa realizada el 25/06/2026 con capturas de Chromium (Playwright).
Todas las pantallas capturadas en `screenshots/`.

---

## Prioridad ALTA — Afectan usabilidad directa

### ✅ UI-01 · Auditoría registra cada login DOS veces — RESUELTO
**Pantalla:** `/web/auditoria`
**Problema:** Cada login exitoso o fallido genera dos entradas en el log de auditoría. Se ven pares "Login exitoso" + "Usuario inició sesión" para el mismo timestamp. El error viene del flujo doble en `AutenticacionService` — llama a `auditoriaService.registrarLogin` Y adicionalmente el `SecurityConfig.erpAuthenticationSuccessHandler` también dispara otro registro.
**Fix:** Revisar `AutenticacionService.login()` y el `successHandler` en `SecurityConfig` — uno de los dos registros es redundante. Mantener solo el de `AutenticacionService.login()` y eliminar el registro duplicado en `erpAuthenticationSuccessHandler`.
**Archivos:**
- `src/main/java/alicanteweb/erp/service/AutenticacionService.java`
- `src/main/java/alicanteweb/erp/config/SecurityConfig.java` (método `erpAuthenticationSuccessHandler`)

---

### ✅ UI-02 · Dashboard — "Estado producción" vacío — RESUELTO
**Pantalla:** `/web/dashboard` (sección inferior derecha)
**Problema:** La tarjeta "Estado producción" aparece completamente vacía, sin datos ni indicadores. La tarjeta izquierda "Ventas últimos 6 meses" sí muestra datos. El usuario no tiene visibilidad del estado de producción desde el panel principal.
**Fix:** Implementar el contenido de la sección con: número de órdenes activas, último horneado, recetas más usadas, o un mensaje "Sin órdenes activas" si no hay datos.
**Archivo:** `src/main/resources/templates/dashboard.html` (buscar la sección `Estado producción`)

---

### ✅ UI-03 · `/web/albaranes/nueva` devuelve 500 en lugar de 404 — RESUELTO
**Pantalla:** `/web/albaranes/nueva`
**Problema:** La URL correcta es `/web/albaranes/nuevo` (masculino). Al visitar `/nueva`, Spring intenta hacer match con `@GetMapping("/{id}")` y falla al parsear "nueva" como `Long`, devolviendo un 500 Internal Server Error en lugar de un 404 limpio. Algún enlace o marcador guardado puede llevar a este camino.
**Fix:** Añadir un `@GetMapping("/nueva")` en `AlbaranWebController` que haga redirect a `/web/albaranes/nuevo`:
```java
@GetMapping("/nueva")
public String nuevaAlias() {
    return "redirect:/web/albaranes/nuevo";
}
```
**Archivo:** `src/main/java/alicanteweb/erp/controller/web/AlbaranWebController.java`

---

## Prioridad MEDIA — Incompletos o confusos para el usuario

### ✅ UI-04 · Página de error sin layout (sidebar ausente) — RESUELTO PARCIALMENTE
**Pantalla:** `/error`, cualquier URL inexistente
**Problema:** La página de error (`error.html`) es una plantilla standalone sin sidebar ni navegación. Cuando el usuario ve un error, no puede navegar a otras secciones. Solo tiene el botón "Volver al panel". Además usa Bootstrap desde CDN (bloqueado por CSP en producción) en lugar del CSS de la app.
**Fix A (rápido):** Añadir el link al CSS de la app y un botón de navegación más útil:
```html
<!-- En error.html, reemplazar la línea del CDN por: -->
<link href="/css/app.css" rel="stylesheet">
```
**Fix B (completo):** Hacer que la página de error use el layout de la app con sidebar. Crear `src/main/resources/templates/error-layout.html` usando `WebController.layout()` y poblarlo con el código del error.

**Nota:** El contenido del error (código, mensaje, URL) SÍ se renderiza correctamente cuando pasa por `WebErrorHandler`. El problema de atributos vacíos solo ocurre cuando Spring Boot maneja el error directamente (404 en rutas sin controlador):
- Spring Boot usa `${status}`, `${error}`, `${message}`, `${path}`
- Nuestro template usa `${codigo}`, `${mensaje}`, `${url}`, `${titulo}`

**Fix adicional** — usar fallback en `error.html`:
```html
<h1 th:text="${codigo} ?: ${status}">500</h1>
<h4 th:text="${mensaje} ?: ${error}">Error interno</h4>
<p th:text="${url} ?: ${path}"></p>
```
**Archivo:** `src/main/resources/templates/error.html`

---

### ✅ UI-05 · Empresa — campos sin acentos y etiqueta incorrecta — RESUELTO
**Pantalla:** `/web/empresa`
**Problema:**
1. Tres labels sin acento: "Direccion", "Codigo postal", "Telefono"
2. El campo "VAT / NIF europeo" es confuso en contexto español (debería ser "CIF / NIF")
3. Faltan campos importantes para facturación: **IBAN / cuenta bancaria** (necesario para que aparezca en las facturas) y **logo de empresa**

**Fix — Cambios de texto en el template de empresa:**
```html
<!-- Cambiar: -->
<label>Direccion</label>          →  <label>Dirección</label>
<label>Codigo postal</label>      →  <label>Código postal</label>
<label>Telefono</label>           →  <label>Teléfono</label>
<label>VAT / NIF europeo</label>  →  <label>CIF / NIF</label>
```
**Archivo:** `src/main/resources/templates/empresa/formulario.html`

---

### ✅ UI-06 · Tesorería — sin botones para añadir movimientos — RESUELTO
**Pantalla:** `/web/tesoreria`
**Problema:** La página muestra tablas de movimientos de caja y banco, pero no hay ningún botón "+ Nuevo movimiento" para registrar entradas/salidas. El usuario no puede añadir movimientos desde esta pantalla.
**Fix:** Añadir botones de acción sobre cada tabla:
```html
<!-- Encima de cada tabla -->
<div class="d-flex justify-content-between align-items-center mb-2">
  <h5>Caja</h5>
  <a href="/web/tesoreria/caja/nuevo" class="btn btn-sm btn-primary">+ Movimiento caja</a>
</div>
```
**Archivos:**
- `src/main/resources/templates/tesoreria/index.html`
- Verificar que exista `TesoreriaWebController` con endpoints para nuevo movimiento

---

### ✅ UI-07 · Facturas — falta acción "Emitir" en listado — RESUELTO
**Pantalla:** `/web/facturas` (listado)
**Problema:** Las facturas BORRADOR solo muestran iconos de ver (👁️), editar (✏️) y anular (⊗), pero no hay un icono o acción rápida para "Emitir" directamente desde el listado. El usuario tiene que entrar en la factura y luego emitirla, creando pasos innecesarios.
**Fix:** Añadir un icono de "Emitir" (✔️ o ▶) condicionalmente en la fila de facturas BORRADOR:
```html
<th:block th:if="${factura.estado == 'BORRADOR'}">
  <form th:action="@{/web/facturas/{id}/emitir(id=${factura.id})}" method="post">
    <button type="submit" class="btn btn-xs btn-success" title="Emitir factura">▶</button>
  </form>
</th:block>
```
**Archivo:** `src/main/resources/templates/facturas/lista.html`

---

### ✅ UI-08 · Artículos — sin editar/eliminar en listado — RESUELTO
**Pantalla:** `/web/articulos` (listado)
**Problema:** La tabla de artículos solo muestra el icono "ver" (👁️), sin opciones de editar o eliminar. Hay que entrar al detalle del artículo para editarlo, lo que añade un click extra innecesario.
**Fix:** Añadir iconos de editar y eliminar análogos a los de clientes y facturas.
**Archivo:** `src/main/resources/templates/articulos/lista.html`

---

## Prioridad BAJA — Mejoras de pulido

### ✅ UI-09 · Albarán "nuevo" — Almacén preseleccionado como "Sin almacén" — RESUELTO
**Pantalla:** `/web/albaranes/nuevo`
**Problema:** El campo Almacén tiene "Sin almacén" como valor preseleccionado. Para una panadería, casi todos los albaranes deben ir asociados a un almacén. Dejar "Sin almacén" como defecto puede llevar a albaranes sin almacén asignado.
**Fix:** Preseleccionar el primer almacén disponible o añadir `required` al campo.
**Archivo:** `src/main/resources/templates/albaranes/formulario.html`

---

### UI-10 · Facturas — "Resumen" de línea poco visible
**Pantalla:** `/web/facturas/nueva` y `/web/albaranes/nuevo`
**Problema:** La sección de resumen de totales (BASE / IVA / TOTAL) tiene el texto "Resumen" / "Total del albarán" como título colapsable, pero visualmente no queda claro que es una sección desplegable. Los totales BASE=0,00€ e IVA=0,00€ son difíciles de distinguir del fondo azul.
**Fix:** Aumentar el contraste del texto en el resumen o separar visualmente las columnas BASE / IVA / TOTAL con bordes o colores de acento.

---

### UI-11 · Login — error message poco prominente
**Pantalla:** `/web/login?error`
**Problema:** Al fallar el login, el mensaje de error (si existe) no es visible visualmente prominente. Revisar si el mensaje "Credenciales incorrectas" se muestra con suficiente contraste.
**Fix:** Asegurarse de que el bloque de error usa `alert alert-danger` de Bootstrap con icono.
**Archivo:** `src/main/resources/templates/login.html`

---

### ✅ UI-12 · Clientes — columna de dirección útil en listado — RESUELTO
**Pantalla:** `/web/clientes`
**Problema:** La tabla de clientes no muestra la población/ciudad. Para identificar clientes rápidamente (especialmente si hay varios con el mismo nombre), sería útil ver la ciudad.
**Fix:** Añadir columna "Ciudad" entre Teléfono y Email, mostrando `cliente.poblacion`.

---

### ✅ UI-13 · Auditoría — "MÓDULO" vacío en algunos registros — RESUELTO
**Pantalla:** `/web/auditoria`
**Problema:** Los registros de "Usuario inició sesión" tienen la columna MÓDULO vacía. Esto hace que la tabla sea inconsistente visualmente.
**Fix:** Al registrar el login exitoso en `AutenticacionService`, pasar `"AUTENTICACION"` como módulo en ambas llamadas (o resolver el issue UI-01 que elimina el duplicado).

---

### UI-14 · Búsqueda de texto resaltado en Almacenes
**Pantalla:** `/web/almacenes`
**Problema:** Los textos "Principal" y "UI" aparecen resaltados en naranja/azul en la tabla de almacenes aunque no hay ningún filtro activo. Posiblemente un `mark` tag o resaltado de búsqueda no limpiado correctamente.
**Fix:** Revisar si el template de almacenes aplica resaltado de búsqueda (`<mark>`) sin condición de búsqueda activa.
**Archivo:** `src/main/resources/templates/almacenes/lista.html`

---

## Rutas con nombres inconsistentes (nomenclatura)

| URL rota / confusa | URL correcta | Motivo |
|---|---|---|
| `/web/produccion/ordenes` | `/web/ordenes-produccion` | 404 → sin handler |
| `/web/albaranes/nueva` | `/web/albaranes/nuevo` | 500 → match `/{id}` |
| `/web/movimientos-stock` | No existe | Sin controlador web |
| `/web/stock` | No existe | Sin controlador web |

**Fix general:** Añadir redirects en un `LegacyRedirectController` para las URLs alternativas más probables.

---

---

## Segunda revisión — Workflow completo (25/06/2026)

### ✅ UI-15 · Facturas — flujo BORRADOR→REVISIÓN bloqueado — RESUELTO
**Problema:** `aprobarYEmitir()` requería estado REVISION pero no existía ningún endpoint ni botón para pasar de BORRADOR a REVISION. Las facturas nunca podían emitirse.
**Fix:** Añadido `pasarARevision()` en FacturaService, endpoint `POST /{id}/revision`, y botones diferenciados: ojo-amarillo (→REVISION) para BORRADOR, send-verde (→EMITIDA) para REVISION.

### ✅ UI-16 · error.html — expresión Thymeleaf inválida causaba timeout — RESUELTO
**Problema:** `th:text="${codigo} ?: ${status} ?: '500'"` usa doble `?:` que Thymeleaf no soporta. Al producirse cualquier error 500, el servidor intentaba renderizar error.html que también fallaba, causando timeout infinito en el browser.
**Fix:** Reescrito con `th:with` anidados: `th:with="cod=${codigo} ?: ${status}"` + `th:text="${cod} ?: '500'"`.

### ✅ UI-17 · Pedidos venta — botón "Confirmar" usaba GET en lugar de POST — RESUELTO
**Problema:** `<a href="/confirmar">` enviaba GET pero el endpoint es `@PostMapping`. El botón no hacía nada visible al usuario. Además el número en el listado no era un enlace, y el estado inicial `PENDIENTE` no coincidía con la condición `BORRADOR` del template.
**Fix:** Cambiado a `<form method="post">`, número → enlace, condición actualizada a `PENDIENTE`, añadido botón editar en listado.

### ✅ UI-18 · Pedidos venta — LazyInitializationException al cargar detalle — RESUELTO
**Problema:** `PedidoRepository.findById()` heredado cargaba `cliente` y `pedidoLineas` como lazy proxies. Con `open-in-view=false`, el acceso en la vista lanzaba `LazyInitializationException` → timeout.
**Fix:** Sobrescrito `findById()` con `@EntityGraph(attributePaths = {"cliente", "pedidoLineas", "pedidoLineas.articulo"})`.

### ✅ UI-19 · Presupuestos — botón "Aprobar" usaba GET en lugar de POST — RESUELTO
**Problema:** `<a href="/aprobar">` → GET. El endpoint es `@PostMapping`. El presupuesto nunca podía aprobarse desde la UI.
**Fix:** Cambiado a `<form method="post">`. Número en listado → enlace clickable.

### ✅ UI-20 · Órdenes de producción — sin transiciones de estado en la UI — RESUELTO
**Problema:** `OrdenProduccionService` tenía `iniciarProduccion()`, `finalizarProduccion()` y `cancelarProduccion()` pero el controlador web no los exponía. El detalle solo mostraba "Editar".
**Fix:** Añadidos endpoints `POST /{id}/iniciar`, `POST /{id}/finalizar`, `POST /{id}/cancelar`. Template actualizado con botones contextuales: PLANIFICADA→(Iniciar + Cancelar), EN_CURSO→(Finalizar con campos cantidad/merma + Cancelar).

### ✅ UI-21 · Pedidos compra — sin transiciones de estado — RESUELTO
**Problema:** No había forma de avanzar un pedido de compra de BORRADOR a ENVIADO o RECIBIDO.
**Fix:** Añadido endpoint `POST /{id}/estado`. Detalle muestra "Enviar pedido" para BORRADOR y "Marcar recibido" para ENVIADO. Número en listado → enlace.

---

## Checklist de implementación

- [x] UI-01: Eliminar registro duplicado de login
- [x] UI-02: Dashboard estado producción
- [x] UI-03: Alias `/web/albaranes/nueva`
- [x] UI-04: `error.html` con CSS de app
- [x] UI-05: Labels empresa con acentos
- [x] UI-06: Botones Tesorería
- [x] UI-07: Acción Emitir en listado facturas
- [x] UI-08: Editar en listado artículos
- [x] UI-09: Almacén preseleccionado en albarán
- [x] UI-10: Contraste sección Resumen
- [x] UI-11: Mensaje error login visible
- [x] UI-12: Columna ciudad en clientes
- [x] UI-13: Módulo AUTENTICACION en auditoría
- [ ] UI-14: Resaltado en tabla de almacenes (falso positivo — spell-check del navegador)
- [x] UI-15: Flujo BORRADOR→REVISIÓN→EMITIDA en facturas
- [x] UI-16: error.html expresión Thymeleaf inválida
- [x] UI-17: Pedido venta confirmar GET→POST + estado PENDIENTE
- [x] UI-18: LazyInitializationException en detalle pedido venta
- [x] UI-19: Presupuesto aprobar GET→POST
- [x] UI-20: Órdenes producción transiciones de estado
- [x] UI-21: Pedidos compra transiciones de estado
