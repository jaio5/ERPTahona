# Análisis Frontend ERP Tahona — Plan de Corrección

> **Fecha:** 2026-06-25  
> **Plantillas revisadas:** 97  
> **CSS:** `app.css` (772 líneas)  
> **JS:** `app.js` (226 líneas)  
> **Fragmentos:** `menu.html`, `pagination.html`, `layout.html`

---

## 1. Resumen Ejecutivo

El frontend está construido sobre **Thymeleaf + Bootstrap 5 + Alpine.js** con un design system propio (`app.css`). Tras la revisión exhaustiva, se identifican **5 problemas críticos que rompen funcionalidad**, **3 problemas mayores que limitan usabilidad**, y **15 inconsistencias de diseño** que degradan la coherencia visual.

### Stack validado
- ✅ Design system CSS (`app.css`) con variables CSS bien definidas
- ✅ Badges de estado vía `badge-status` + `data-status`
- ✅ Botones ghost (`btn-ghost`) para acciones secundarias
- ✅ Protección anti-doble-click (`data-no-double-click`)
- ✅ Paginación reutilizable (`fragments/pagination.html`)
- ✅ Layout con sidebar, topbar y toast notifications
- ✅ Alpine.js con `x-cloak` en todas las vistas que lo usan

---

## 2. Problemas Críticos (No funcionan)

| # | Problema | Archivo | Línea(s) | Detalle |
|---|----------|---------|----------|---------|
| **C1** | Badges sin colores | `recepciones/ver.html` | 49-50 | Usa clases legacy `status-activo` / `status-pendiente` que `app.css` **no estiliza**. El CSS solo reconoce `data-status`. Resultado: badges grises sin color. |
| **C2** | Doble-click protection rota | `facturas/lista.html` | 120 | Usa `data-no-spinner="true"` (nombre antiguo). `app.js` línea 87 busca `dataset.noDoubleClick`, así que **la protección no se activa** en el botón "Anular". |
| **C3** | Doble-click protection desactivada | `facturas/lista.html` | 105, 112 | Usa `data-no-double-click="true"`. En `app.js`, la condición `!btn.dataset.noDoubleClick` evalúa `false` cuando el valor es `"true"`, así que **deshabilita la protección** en "Enviar a revisión" y "Emitir". |
| **C4** | Página de error rota | `error.html` | Todo | Usa clases Bootstrap (`bg-light`, `d-flex`, `align-items-center`, etc.) pero **no carga Bootstrap CSS** — solo carga `app.css`. Las clases no existen, la página se ve sin formato. |
| **C5** | Tabla sin scroll horizontal | `reportes/rentabilidad.html` | 38 | Tiene `table-card` pero **falta `table-responsive`**. En móvil la tabla se desborda del viewport. |
| **C6** | Protección anti-doble-click ausente | `importar/form.html` | 13, 21 | Botones "Importar clientes" y "Importar artículos" no tienen `data-no-double-click`. El usuario puede pulsar dos veces. |
| **C7** | Botón "Anular" sin protección | `facturas/ver.html` | 194 | El botón "Anular factura" dentro del formulario no tiene `data-no-double-click`. |

---

## 3. Problemas Mayores (Limitan usabilidad)

| # | Problema | Archivo(s) | Detalle |
|---|----------|-----------|---------|
| **M1** | Paginación ausente | `pedidos-compra/lista.html`, `ordenes-produccion/lista.html`, `facturas-compra/lista.html`, `usuarios/lista.html` | Si hay muchos registros, no hay navegación de páginas. Requiere verificar que el backend pase `page` al modelo. |
| **M2** | Sin búsqueda ni filtro | `pedidos-compra/lista.html`, `facturas-compra/lista.html` | No tienen `filter-bar` con campo de búsqueda ni selector de estado. |
| **M3** | Botones secundarios con Bootstrap | `dashboard.html`, `clientes/lista.html`, `articulos/ver.html`, 7 `ver.html`, 23 `formulario.html` | Usan `btn-outline-secondary` (Bootstrap) en vez de `btn-ghost` (design system). Visualmente distintos. |

---

## 4. Inconsistencias de Diseño

### 4.1 Vistas de detalle (ver.html) — Sistema de campos inconsistente

El design system define `doc-field-label` + `doc-field-value` para vistas de detalle. Algunos templates lo usan, otros no.

| Template | Usa `doc-field-*` | Estado |
|----------|-------------------|--------|
| `albaranes/ver.html` | ✅ | OK |
| `articulos/ver.html` | ✅ | OK |
| `clientes/ver.html` | ✅ | OK |
| `facturas/ver.html` | ✅ | OK |
| `hojas-ruta/ver.html` | ✅ | OK |
| `ordenes-produccion/ver.html` | ✅ | OK |
| `pedidos-venta/ver.html` | ✅ | OK |
| `proveedores/ver.html` | ✅ | OK |
| `recepciones/ver.html` | ✅ | OK |
| `recetas/ver.html` | ✅ | OK |
| `almacenes/ver.html` | ❌ | **Pendiente** |
| `appcc/ver.html` | ❌ | **Pendiente** |
| `devoluciones/ver.html` | ❌ | **Pendiente** |
| `facturas-compra/ver.html` | ❌ | **Pendiente** |
| `horneadas/ver.html` | ❌ | **Pendiente** |
| `lotes/ver.html` | ❌ | **Pendiente** |
| `mermas/ver.html` | ❌ | **Pendiente** |
| `pedidos-compra/ver.html` | ❌ | **Pendiente** |
| `presupuestos/ver.html` | ❌ | **Pendiente** |
| `rutas/ver.html` | ❌ | **Pendiente** |
| `usuarios/ver.html` | ❌ | Usa `dl/dt/dd` (Bootstrap) en vez de `doc-field-*` |
| `vehiculos/ver.html` | ❌ | **Pendiente** |

### 4.2 Empty states sin icono

La clase `empty-state` en `app.css` espera un `<i class="bi bi-*"></i>` para el icono centrado. Algunos templates no lo incluyen.

| Template | Línea | Estado |
|----------|-------|--------|
| `rutas/ver.html` | 15 | ❌ Sin icono |
| `fiscal/verifactu-lista.html` | 23 | ❌ Sin icono |
| `fiscal/modelo347.html` | 38 | ❌ Sin icono |
| `lotes/ver.html` | 25, 32 | ❌ Sin icono |
| `reportes/rentabilidad.html` | 75 | ❌ No usa `empty-state` (usa `form-card text-center`) |

### 4.3 Botones `btn-outline-secondary` vs `btn-ghost`

`btn-ghost` es la clase del design system para acciones secundarias. `btn-outline-secondary` es Bootstrap. Hay 38+ usos de `btn-outline-secondary` que deberían ser `btn-ghost`.

**Ubicaciones de `btn-outline-secondary` (a convertir):**
- `dashboard.html` (7): botón "Informes"
- `clientes/lista.html` (7): botón "CSV"
- `articulos/ver.html` (12): botón "Editar"
- `pedidos-venta/ver.html` (129): botón "Editar"
- `ordenes-produccion/ver.html` (14, 139): botón "Editar"
- `facturas/ver.html` (163, 175): botón "Editar"
- `recetas/ver.html` (10): botón "Editar"
- `proveedores/ver.html` (13): botón "Editar"
- `clientes/ver.html` (14): botón "Editar"
- **Todos los `formulario.html` (23):** botón "Cancelar"
- **Todos los `formulario.html` con líneas (4):** botón "Añadir línea" (`addLineBtn`)

### 4.4 Tablas sin `table-responsive`

Algunos templates tienen tablas que no están envueltas en `table-responsive`, lo que causa desbordamiento en móvil.

| Template | Tabla | Estado |
|----------|-------|--------|
| `reportes/rentabilidad.html` | Tabla de rentabilidad | ❌ Falta `table-responsive` |
| `clientes/ver.html` (facturas, albaranes) | Tablas internas | ✅ Ya tienen `table-responsive` |
| `planificador.html` | Tablas de planificación | ❌ Pendiente revisar |
| `tesoreria/conciliacion.html` | Tabla de conciliación | ❌ Pendiente revisar |
| `tesoreria/extractos.html` | Tablas de extractos | ❌ Pendiente revisar |
| `reportes/inventario.html` | Tablas de inventario | ❌ Pendiente revisar |

---

## 5. Plan de Acción por Fases

### Fase 1: Críticos (Funcionalidad rota)

- [x] **C1** `recepciones/ver.html`: Cambiar `class="badge-status status-activo"` → `class="badge-status" data-status="completado"`, y `class="badge-status status-pendiente"` → `class="badge-status" data-status="pendiente"` ✅
- [x] **C2** `facturas/lista.html` línea 120: `data-no-spinner="true"` → `data-no-double-click` ✅
- [x] **C3** `facturas/lista.html` líneas 105, 112: `data-no-double-click="true"` → `data-no-double-click` (atributo sin valor, para que JS lo detecte correctamente) ✅
- [x] **C4** `error.html`: Añadir `<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">` o reescribir estilos inline con el design system ✅
- [x] **C5** `reportes/rentabilidad.html`: Añadir `<div class="table-responsive">` dentro del `table-card` ✅
- [x] **C6** `importar/form.html`: Añadir `data-no-double-click` a los botones de importar ✅
- [x] **C7** `facturas/ver.html` línea 194: Añadir `data-no-double-click` al botón "Anular factura" ✅

### Fase 2: Paginación y Búsqueda

- [x] **M1** `pedidos-compra/lista.html`: Añadir `th:replace="fragments/pagination"` y `filter-bar` con búsqueda ✅
- [x] **M1** `ordenes-produccion/lista.html`: Añadir `th:replace="fragments/pagination"` (ya tiene `filter-bar`) ✅
- [x] **M1** `facturas-compra/lista.html`: Añadir `th:replace="fragments/pagination"` y `filter-bar` con búsqueda ✅
- [x] **M1** `usuarios/lista.html`: Añadir `th:replace="fragments/pagination"` (verificar backend pasa `page`) ✅
- [x] **M2** `pedidos-compra/lista.html`: Añadir `filter-bar` con buscador y selector de estado ✅
- [x] **M2** `facturas-compra/lista.html`: Añadir `filter-bar` con buscador y selector de estado ✅

### Fase 3: Vistas de detalle (ver.html) — Rediseño

- [x] `almacenes/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `appcc/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `devoluciones/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `facturas-compra/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `horneadas/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `lotes/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `mermas/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `pedidos-compra/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `presupuestos/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `rutas/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `vehiculos/ver.html`: Migrar a `doc-field-label` / `doc-field-value` ✅
- [x] `usuarios/ver.html`: Migrar de `dl/dt/dd` a `doc-field-label` / `doc-field-value` ✅

### Fase 4: Botones consistentes

- [x] `dashboard.html`: `btn-outline-secondary` → `btn-ghost` (botón "Informes") ✅
- [x] `clientes/lista.html`: `btn-outline-secondary` → `btn-ghost` (botón "CSV") ✅
- [x] `articulos/ver.html`: `btn-outline-secondary` → `btn-ghost` (botón "Editar") ✅
- [x] `pedidos-venta/ver.html`: `btn-outline-secondary` → `btn-ghost` (botón "Editar") ✅
- [x] `ordenes-produccion/ver.html`: `btn-outline-secondary` → `btn-ghost` (botón "Editar") ✅
- [x] `facturas/ver.html`: `btn-outline-secondary` → `btn-ghost` (botón "Editar") ✅
- [x] `recetas/ver.html`: `btn-outline-secondary` → `btn-ghost` (botón "Editar") ✅
- [x] `proveedores/ver.html`: `btn-outline-secondary` → `btn-ghost` (botón "Editar") ✅
- [x] `clientes/ver.html`: `btn-outline-secondary` → `btn-ghost` (botón "Editar") ✅
- [x] **Todos los `formulario.html` (23):** `btn-outline-secondary` → `btn-ghost` (botón "Cancelar") ✅
- [x] **Formularios con líneas (4):** `btn-outline-secondary` → `btn-ghost` (botón "Añadir línea") ✅
- [x] **Adicional:** `facturas/rectificativa.html` (Cancelar), `tesoreria/conciliacion.html` (JS), `importar/form.html` (importar) ✅

### Fase 5: Empty states

- [x] `rutas/ver.html`: Añadir `<i class="bi bi-geo-alt"></i>` al empty state ✅
- [x] `fiscal/verifactu-lista.html`: Añadir `<i class="bi bi-shield-check"></i>` al empty state ✅
- [x] `fiscal/modelo347.html`: Añadir `<i class="bi bi-file-earmark-spreadsheet"></i>` al empty state ✅
- [x] `lotes/ver.html`: Añadir `<i class="bi bi-arrow-left-right"></i>` al empty state ✅
- [x] `reportes/rentabilidad.html`: Convertir empty state a `<div class="empty-state">` con icono ✅

### Fase 6: Verificación final

- [x] Ejecutar `mvnw test` y confirmar que todos los tests pasan ✅ (215 tests, 0 fallos)
- [x] Revisar que todos los `ver.html` con tablas internas tengan `table-responsive` ✅
- [x] Revisar que todos los `lista.html` con paginación tengan `filter-bar` + `table-responsive` + `empty-state` con icono ✅
- [x] Revisar que todos los botones de submit tengan `data-no-double-click` ✅
- [x] Revisar que todos los badges de estado usen `data-status` en vez de clases legacy ✅
- [x] Estandarizar paginación fiscal (`verifactu-lista.html`) a usar `fragments/pagination` ✅
- [x] Eliminar todos los `btn-outline-secondary` restantes del proyecto ✅

---

## 6. Notas de implementación

### Convenciones a seguir

1. **Badges de estado:**
   ```html
   <!-- Correcto -->
   <span class="badge-status" data-status="activo">Activo</span>
   <!-- Incorrecto -->
   <span class="badge-status status-activo">Activo</span>
   ```

2. **Botones de acción secundaria:**
   ```html
   <!-- Correcto -->
   <a class="btn btn-ghost btn-sm">Volver</a>
   <!-- Incorrecto -->
   <a class="btn btn-outline-secondary btn-sm">Volver</a>
   ```

3. **Protección anti-doble-click:**
   ```html
   <!-- Correcto (sin valor) -->
   <button data-no-double-click>Guardar</button>
   <!-- Incorrecto (con valor, desactiva la protección) -->
   <button data-no-double-click="true">Guardar</button>
   <!-- Incorrecto (nombre antiguo) -->
   <button data-no-spinner="true">Guardar</button>
   ```

4. **Tablas de listado:**
   ```html
   <!-- Correcto -->
   <div class="table-card">
       <div class="table-responsive">
           <table class="table table-hover mb-0">
   ```

5. **Empty state:**
   ```html
   <!-- Correcto -->
   <td colspan="6" class="empty-state">
       <i class="bi bi-people"></i>
       <div>No hay registros</div>
   </td>
   ```

6. **Vista de detalle (ver.html):**
   ```html
   <!-- Correcto -->
   <div class="col-sm-4">
       <span class="doc-field-label">Nombre</span>
       <div class="doc-field-value" th:text="${obj.nombre}">—</div>
   </div>
   ```

---

## 7. Métricas de calidad (objetivo)

| Métrica | Actual | Objetivo |
|---------|--------|----------|
| Plantillas con `badge-status` + `data-status` | ~80% | 100% |
| Plantillas con `btn-ghost` para acciones secundarias | ~65% | 100% |
| Plantillas con `table-responsive` donde aplica | ~85% | 100% |
| Plantillas con `empty-state` + icono | ~70% | 100% |
| Botones submit con `data-no-double-click` | ~90% | 100% |
| `ver.html` con diseño `doc-field-*` | ~55% | 100% |
| Tests pasando | 215 | 215+ |
