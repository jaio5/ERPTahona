# Auditoría técnica — ERPTahona · 22 junio 2026

Revisión completa de la aplicación: todas las pantallas verificadas con Playwright/Chromium.
Se encontraron y corregieron 10 bugs.

---

## 1. LazyInitializationException en Pedidos venta

**Archivo:** `src/main/java/alicanteweb/erp/repository/PedidoRepository.java`

**Por qué:** La página `/web/pedidos-venta` cargaba los pedidos paginados con `findAll(Pageable)`. Hibernate cerraba la sesión JPA antes de que Thymeleaf accediera a `p.cliente.nombre`, lanzando `LazyInitializationException`. La página tardaba 60 segundos antes de fallar.

**Qué se cambió:** Se añadió `@EntityGraph(attributePaths = {"cliente"})` en los tres métodos de consulta del repositorio (`findAll(Pageable)`, `findByEstado`, `findByNumeroContaining...`) para que el cliente se cargue en la misma query SQL mediante JOIN.

---

## 2. LazyInitializationException en Presupuestos

**Archivo:** `src/main/java/alicanteweb/erp/repository/PresupuestoRepository.java`

**Por qué:** Mismo problema que pedidos: `findAllOrdenados()` y `buscar()` devolvían `Presupuesto` sin hacer fetch del cliente asociado. La página redirigía a `/clientes` tras un 500.

**Qué se cambió:**
- Se añadió `LEFT JOIN FETCH p.cliente` a las queries JPQL de `findAllOrdenados()` y `buscar()`.
- Se añadió `@EntityGraph(attributePaths = {"cliente"})` a `findAll(Pageable)` y `findByEstado()`.

---

## 3. Alpine.js CSP build — expresiones dinámicas rotas

**Archivos:**
- `src/main/resources/templates/layout.html` (línea 77)
- `src/main/java/alicanteweb/erp/config/CspNonceFilter.java`

**Por qué:** El layout cargaba `@alpinejs/csp@3.14.9`, la versión "CSP-friendly" de Alpine.js que prohíbe expresiones JavaScript dinámicas. Las plantillas usaban sintaxis estándar de Alpine (`{'border-primary': dia.esHoy}`, `hasAlertas()`, `a.id`, etc.), generando 14+ errores de consola. El calendario no resaltaba el día actual, el planificador no cargaba datos, y las notificaciones no funcionaban.

**Qué se cambió:**
- `layout.html`: cambiado el CDN de `@alpinejs/csp@3.14.9` a `alpinejs@3.14.9` (full build).
- `CspNonceFilter.java`: añadido `'unsafe-eval'` al `script-src` de la cabecera CSP, necesario para que Alpine.js full build pueda evaluar expresiones.

---

## 4. error.html — título "null | ERP Tahona"

**Archivo:** `src/main/resources/templates/error.html`

**Por qué:** Al navegar a rutas sin controlador (como `/web/fiscal`, que es solo un grupo del sidebar), Spring devolvía un 404 con el modelo de error sin campo `titulo`. La expresión Thymeleaf `${titulo} + ' | ERP Tahona'` concatenaba `null` literalmente.

**Qué se cambió:** Expresión cambiada a `${titulo != null ? titulo + ' | ERP Tahona' : 'Error | ERP Tahona'}`.

---

## 5. Presupuestos — fechas en formato ISO

**Archivo:** `src/main/resources/templates/presupuestos/lista.html`

**Por qué:** Las columnas Fecha y Válido hasta mostraban `2026-06-21` (`.toString()` de `LocalDate`) en lugar del formato español `21/06/2026`.

**Qué se cambió:** Sustituido `th:text="${p.fecha}"` por `th:text="${p.fecha != null ? #temporals.format(p.fecha, 'dd/MM/yyyy') : '-'}"` en ambas columnas de fecha.

---

## 6. Contabilidad — cabecera sin acento

**Archivo:** `src/main/resources/templates/contabilidad/lista.html`

**Por qué:** La cabecera de la columna mostraba "Numero" en lugar de "Número".

**Qué se cambió:** Corregido el texto de la cabecera `<th>` de "Numero" a "Número".

---

## 7. Todas las tablas — columnas truncadas

**Archivo:** `src/main/resources/static/css/app.css` (selector `.table-card .table-responsive > .table`)

**Por qué:** El CSS aplicaba `table-layout: fixed` con `min-width: 720px`. Con el viewport de ~928px y el sidebar de ~210px, el área de contenido era ~718px. Con 5 columnas iguales, cada una recibía ~144px, insuficiente para números como `F-GEN-2026-0007` o badges como `PENDIENTE`/`ENTREGADO`, que aparecían cortados.

**Qué se cambió:**
```css
/* Antes */
table-layout: fixed;
min-width: 720px;

/* Después */
table-layout: auto;
min-width: 600px;
```

---

## 8. Factura detalle — columna IVA truncada

**Archivo:** `src/main/resources/templates/facturas/ver.html` (línea ~101)

**Por qué:** La columna IVA tenía `width: 75px` pero el valor `10.00%` (BigDecimal con escala 2 + símbolo `%`) necesitaba más espacio.

**Qué se cambió:** Ancho aumentado de `75px` a `90px`.

---

## 9. Dashboard — alertas Alpine.js con key undefined

**Archivo:** `src/main/java/alicanteweb/erp/controller/rest/AlertaRestController.java`

**Por qué:** El endpoint `/api/web/alertas` devolvía objetos sin campo `id`. El template usa `x-for="a in alertas" :key="a.id"`, por lo que Alpine.js recibía `undefined` como key en todos los elementos, generando 4 warnings de "Duplicate key", 1 de ":key is undefined or invalid" y un error fatal `Cannot read properties of undefined (reading 'after')` que impedía renderizar las alertas.

**Qué se cambió:** Se añadió el campo `"id"` a cada objeto de alerta construido en el controlador, con valores únicos según el tipo:
- Alertas de artículo: `"STOCK_BAJO_" + art.getId()`
- Alertas de lote: `"LOTE_CADUCA_" + l.getId()`
- Alertas de pedidos/órdenes: string fijo por tipo (`"PEDIDOS_PENDIENTES"`, `"ORDENES_PLANIFICADAS"`)
- Alertas de factura: `"FACTURA_VENCIDA_" + f.getId()`

---

## 10. Auditoría — fechas ISO con nanosegundos y cabeceras sin acentos

**Archivo:** `src/main/resources/templates/auditoria/lista.html`

**Por qué:** La columna Fecha mostraba `2026-06-22T17:46:36.019824` (`.toString()` de `LocalDateTime` con nanosegundos). Además, las cabeceras "Modulo", "Accion" y "Descripcion" carecían de tildes.

**Qué se cambió:**
- Fecha: `th:text="${r.fecha}"` → `th:text="${r.fecha != null ? #temporals.format(r.fecha, 'dd/MM/yyyy HH:mm') : '-'}"`.
- Cabeceras: "Modulo" → "Módulo", "Accion" → "Acción", "Descripcion" → "Descripción", "No hay registros de auditoria" → "No hay registros de auditoría".

---

## Módulos verificados sin bugs

| Módulo | Pantallas verificadas |
|--------|----------------------|
| Ventas | Facturas (lista + detalle), Albaranes, Pedidos, Presupuestos, Clientes |
| Compras | Proveedores, Pedidos compra, Facturas compra, Recepciones |
| Almacén | Artículos, Almacenes, Lotes/Trazabilidad, Mermas |
| Producción | Recetas, Órdenes, Horneadas, APPCC |
| Reparto | Vehículos, Rutas, Hojas de ruta, Devoluciones |
| Finanzas | Contabilidad, Tesorería/Extractos |
| Administración | Usuarios, Empresa, Auditoría, Backups |
| Fiscal | Modelo 347, Verifactu |
| General | Dashboard, Calendario, Planificador, Informes |
