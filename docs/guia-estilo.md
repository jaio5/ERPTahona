# Guía de estilo de la interfaz — ERP Tahona

Esta guía es la referencia única para maquetar pantallas. Todas las clases que aparecen aquí están
definidas en `src/main/resources/static/css/app.css`. Si un patrón no está en la guía, no se inventa
uno nuevo en la plantilla: se añade primero a `app.css` y a esta guía.

> Alcance: plantillas de `src/main/resources/templates/` **excepto** `templates/pdf/` (documentos
> fiscales impresos, con su propio CSS).

---

## 1. Dirección visual

- **Tinta sobre papel de harina.** Fondo de página gris cálido muy claro (`--color-bg`), tarjetas blancas
  y texto en tinta casi negra. La acción principal es **azul tinta** (`--accent`), sobria y con mucho
  contraste en una tablet del obrador.
- **Carmín de la casa como firma.** El carmín de la tarjeta del obrador (`--brand-carmin`) se usa
  **solo** en la marca (logotipo, login) y en el filete de los títulos de sección de formulario
  (`.form-section-title`), igual que los epígrafes subrayados de la tarjeta. **Nunca** indica estado ni
  se usa en botones.
- **Los estados tienen su propia paleta** (ok, aviso, peligro, info, neutro, anulado), separada del
  acento. Cada estado lleva además una forma: punto de color en el badge, contorno discontinuo en los
  anulados, icono en avisos. Nunca se comunica un estado solo con color.
- **Modo oscuro completo** por tokens: noche de horno (azul muy oscuro) con texto color harina. Se elige
  con el botón sol/luna de la barra superior (se guarda en `localStorage`, clave `erp_tema`); sin elección,
  sigue al sistema.
- **Legible con harina en los dedos.** En pantallas táctiles (`pointer: coarse`) el texto base sube a
  15 px y todos los controles a 40–44 px de alto automáticamente. No hay que hacer nada en la plantilla
  salvo no fijar alturas ni tamaños de letra en línea.

---

## 2. Tokens (no usar colores literales)

| Grupo | Tokens | Uso |
|---|---|---|
| Neutros | `--color-bg`, `--color-surface`, `--color-surface-2`, `--color-surface-3` | fondo de página, tarjeta, franja/cabecera de tabla, hover/pulsado |
| Bordes | `--color-border`, `--color-border-strong`, `--color-border-input` | separadores, cabecera de tabla, borde de campos (3:1) |
| Texto | `--color-text`, `--color-text-2`, `--color-text-muted`, `--color-text-faint` | principal, etiquetas, secundario (AA), **solo iconos** |
| Acento | `--accent`, `--accent-hover`, `--accent-soft`, `--accent-soft-text`, `--accent-ring`, `--on-accent` | botón primario, foco, selección, fondos suaves |
| Enlaces | `--color-link`, `--color-link-hover` | enlaces de texto |
| Marca | `--brand-carmin`, `--brand-carmin-rule` | logotipo y filete de sección, nada más |
| Estados | `--color-{success,warning,danger,info,neutral}` + `-bg`, `-text`, `-border` | badges, alertas, iconos de estado |
| Gráficos | `--chart-bar`, `--chart-grid`, `--chart-axis` | Chart.js y barras HTML |
| Tipo | `--font-sans`, `--font-marca`, `--text-xs` 12 · `--text-sm` 13 · `--text-base` 14 · `--text-md` 15 · `--text-lg` 17 · `--text-xl` 20 · `--text-2xl` 26 · `--text-3xl` 32 | |
| Espacio | `--space-1` 4 · `-2` 8 · `-3` 12 · `-4` 16 · `-5` 20 · `-6` 24 · `-8` 32 · `-10` 40 · `-12` 48 | |
| Radios | `--radius-sm` 4 · `--radius` 6 · `--radius-md` 8 · `--radius-lg` 10 · `--radius-xl` 14 · `--radius-full` | |
| Sombras | `--shadow-sm`, `--shadow`, `--shadow-md`, `--shadow-lg` | |

Alias heredados que siguen funcionando (no usar en código nuevo): `--brand-color` (= `--accent`),
`--brand-hover`, `--brand-light`.

Las variables de Bootstrap (`--bs-body-bg`, `--bs-link-color`, `--bs-primary-rgb`, `--bs-*-bg-subtle`…)
ya están conectadas a estos tokens, así que `.alert-*`, `.table`, `.modal`, `.pagination`,
`.form-control`, `.text-muted`, `.text-success/-warning/-danger/-info/-primary`, `.bg-light`,
`.table-light` y `.text-body` siguen el tema solos.

---

## 3. Reglas

1. **Nada de `style="…"` en línea.** Excepciones justificadas (y comentadas en la plantilla):
   - un ancho calculado en JS (p. ej. la barra de `.hbar-fill`);
   - `min-width` de una columna de tabla concreta cuando la tabla lo necesita para no romper (`<th style="min-width:130px">`);
   - nada más. Colores, tamaños de letra, márgenes y paddings salen de clases.
2. **Mostrar/ocultar desde JS:** usar el atributo `hidden` (`el.hidden = true/false`), no `style.display`.
3. **Alpine:** nunca combinar `x-show` con `d-block`, `d-flex`, `d-grid`, `d-inline-*` en el mismo elemento
   (llevan `!important` y anulan `x-show`). Poner el `x-show` en un envoltorio o usar las clases de
   componente de esta guía, que no llevan `!important`.
4. **Cifras a la derecha y tabulares.** Toda celda o cabecera con importe, cantidad, porcentaje o recuento
   lleva `.cifra` (alinea a la derecha, `tabular-nums`, sin salto de línea). En tablas: `th.cifra` y
   `td.cifra`. Las fechas van a la izquierda.
5. **Importes formateados en español:** `#numbers.formatCurrency(x)` o
   `#numbers.formatDecimal(x,1,'POINT',2,'COMMA') + ' €'`. Nunca `.` como separador decimal.
6. **Iconos:** solo `bootstrap-icons` (`<i class="bi bi-…">`). Todo icono decorativo lleva
   `aria-hidden="true"`. Sin emojis ni glifos (⚠, ✓, ←) como iconos.
7. **Botones solo-icono:** `class="btn btn-ghost btn-sm btn-icon"` + `aria-label` con verbo y objeto
   (`aria-label="Editar factura F-2026-0012"`) + `title` igual de corto ("Editar").
8. **Un solo botón primario por pantalla** (la acción que termina la tarea: Guardar, Nueva factura,
   Emitir). El resto: `btn-outline-secondary`, `btn-outline-primary` o `btn-ghost`.
9. **Acciones destructivas:** `btn-outline-danger` (en ficha) o `btn-ghost btn-sm btn-icon text-danger`
   (en fila), siempre con `data-confirm="¿…?"` y `data-no-double-click` si es un POST.
10. **Encabezados:** un `<h1>` por pantalla dentro de `.page-header-title` (se admite `<h4>` heredado, se ve
    igual). Títulos de tarjeta: `<h2 class="card-heading">` o `<h2 class="table-card-title">`.
11. **Textos de interfaz** en español de España, en frase normal (no TODO MAYÚSCULAS; las mayúsculas de
    etiquetas pequeñas las pone el CSS). Botones con verbo: «Guardar cliente», «Emitir factura».
12. **No cambiar** `name=`, `th:field`, `id` usados por JS, atributos Alpine, ni textos que comprueben los
    tests (buscar con `grep -rn "texto" src/test` antes de tocar un texto).
13. **No usar** `text-primary` para decorar iconos de título, ni `fw-600`, ni colores Bootstrap como
    decoración (`text-warning` en un rayo, `text-danger` en una llama). Los iconos de título heredan
    `--color-text-muted` de `.card-heading`.

---

## 4. Patrones

### 4.1 Estructura de una pantalla

El layout (`layout.html`) ya pinta barra lateral, barra superior con migas, conmutador de tema y toasts.
La vista solo aporta el fragmento `content`:

```html
<div th:fragment="content">
    <div class="page-header">…</div>          <!-- 4.2 -->
    <form class="filter-bar mb-3">…</form>    <!-- 4.3, solo listados -->
    <div class="table-card">…</div>           <!-- 4.4 -->
</div>
```

Los mensajes `exito` / `error` del controlador ya salen como toast desde el layout: **no** repetir
`<div th:if="${exito}" class="alert …">` en la vista.

### 4.2 Cabecera de página — `.page-header`

```html
<div class="page-header">
    <div class="page-header-title">
        <h1 th:text="${titulo}">Facturas</h1>
        <p>Facturas emitidas a clientes</p>                <!-- opcional -->
    </div>
    <div class="page-header-actions">
        <a th:href="@{/web/facturas/exportar}" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-download me-1" aria-hidden="true"></i>Exportar
        </a>
        <a th:href="@{/web/facturas/nueva}" class="btn btn-primary btn-sm">
            <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Nueva factura
        </a>
    </div>
</div>
```

En una ficha, la línea secundaria lleva el estado y la fecha:

```html
<div class="page-header-title">
    <h1 th:text="'Factura ' + ${factura.numero}">Factura</h1>
    <p>
        <span class="badge-status" th:attr="data-status=${factura.estado?.toLowerCase()}"
              th:text="${factura.estado}">EMITIDA</span>
        <span th:text="${#temporals.format(factura.fecha,'dd/MM/yyyy')}">01/10/2026</span>
    </p>
</div>
```

Orden de botones en `.page-header-actions`: secundarios primero, primario al final (derecha).
«Volver» es `btn btn-ghost btn-sm` con `bi-arrow-left` y va el primero.

### 4.3 Barra de filtros — `.filter-bar`

```html
<form class="filter-bar mb-3" method="get" th:action="@{/web/facturas}">
    <div class="filter-bar-search">
        <label for="facturas-search" class="visually-hidden">Buscar facturas</label>
        <i class="bi bi-search" aria-hidden="true"></i>
        <input id="facturas-search" type="search" name="q" class="form-control form-control-sm"
               placeholder="Número o cliente…" th:value="${q}" autocomplete="off">
    </div>
    <div class="filter-bar-filters">
        <input type="date" name="fechaDesde" class="form-control form-control-sm" aria-label="Fecha desde">
        <input type="date" name="fechaHasta" class="form-control form-control-sm" aria-label="Fecha hasta">
        <select name="estado" class="form-select form-select-sm" aria-label="Estado" data-autosubmit>…</select>
    </div>
    <div class="filter-bar-actions">
        <button class="btn btn-outline-secondary btn-sm"><i class="bi bi-funnel me-1" aria-hidden="true"></i>Filtrar</button>
        <a th:href="@{/web/facturas}" class="btn btn-ghost btn-sm">Limpiar</a>
        <span class="filter-bar-count" th:if="${page != null}"><span th:text="${page.totalElements}">0</span> facturas</span>
    </div>
</form>
```

- Los anchos de los filtros los pone `.filter-bar-filters` (≈11rem, fechas ≈9,75rem). **Quitar** los
  `style="width:150px"` de inputs y selects.
- En móvil la barra se apila sola.
- Si el listado no tiene buscador propio, `app.js` añade uno: no hace falta escribirlo.
- Colocada como primer hijo de `.table-card`, la barra pierde el marco y queda como franja de la tarjeta.

### 4.4 Tarjeta de tabla — `.table-card`

```html
<div class="table-card">
    <div class="table-card-header">                         <!-- opcional -->
        <h2 class="table-card-title">Líneas de factura</h2>
        <a href="…" class="btn btn-ghost btn-sm">Ver todas</a>
    </div>
    <div class="table-responsive">
        <table class="table table-hover mb-0">
            <thead>
                <tr>
                    <th>Número</th>
                    <th>Fecha</th>
                    <th>Cliente</th>
                    <th class="cifra">Total</th>
                    <th>Estado</th>
                    <th><span class="visually-hidden">Acciones</span></th>
                </tr>
            </thead>
            <tbody>
                <tr th:if="${#lists.isEmpty(facturas)}">
                    <td colspan="6" class="empty-state">…</td>      <!-- 4.6 -->
                </tr>
                <tr th:each="f : ${facturas}">
                    <td><a th:href="@{/web/facturas/{id}(id=${f.id})}" th:text="${f.numero}">F-1</a></td>
                    <td class="text-muted" th:text="${#temporals.format(f.fecha,'dd/MM/yyyy')}">01/10/2026</td>
                    <td th:text="${f.cliente?.nombre}">Cliente</td>
                    <td class="cifra fw-medium" th:text="${#numbers.formatCurrency(f.total)}">0,00 €</td>
                    <td><span class="badge-status" th:attr="data-status=${f.estado?.toLowerCase()}" th:text="${f.estado}">-</span></td>
                    <td>
                        <div class="actions">…</div>                 <!-- 4.5 -->
                    </td>
                </tr>
            </tbody>
            <tfoot>                                              <!-- opcional: totales -->
                <tr><td colspan="3" class="text-end fw-semibold">Total</td><td class="cifra fw-bold">…</td><td colspan="2"></td></tr>
            </tfoot>
        </table>
    </div>
    <div class="table-card-footer">…</div>                     <!-- opcional: resumen -->
</div>
<div th:replace="~{fragments/pagination :: paginacion(${page}, '/web/facturas')}"></div>
```

- Sustituye al patrón antiguo `<div class="d-flex align-items-center justify-content-between px-4 py-3 border-separator"><span class="fw-semibold">…</span></div>` → `.table-card-header` + `.table-card-title`.
- Las celdas no parten línea (elipsis a 320 px). Para descripciones largas: `td.td-wrap`.
- Los enlaces dentro de celdas ya salen en color de enlace y peso medio: no añadir `text-decoration-none fw-medium`.
- Cabecera ordenable: `<th><a href="…">Número <i class="bi bi-sort-down" aria-hidden="true"></i></a></th>`; en columnas de cifras, `th.cifra` alinea el enlace a la derecha.

### 4.5 Acciones de fila — `.actions`

```html
<div class="actions">
    <a th:href="@{/web/facturas/{id}(id=${f.id})}" class="btn btn-ghost btn-sm btn-icon"
       th:attr="aria-label='Ver factura ' + ${f.numero}" title="Ver">
        <i class="bi bi-eye" aria-hidden="true"></i>
    </a>
    <a th:href="@{/web/facturas/{id}/editar(id=${f.id})}" class="btn btn-ghost btn-sm btn-icon"
       th:attr="aria-label='Editar factura ' + ${f.numero}" title="Editar">
        <i class="bi bi-pencil" aria-hidden="true"></i>
    </a>
    <form th:action="@{/web/facturas/{id}/anular(id=${f.id})}" method="post">
        <button class="btn btn-ghost btn-sm btn-icon text-danger" data-no-double-click
                data-confirm="¿Anular esta factura?"
                th:attr="aria-label='Anular factura ' + ${f.numero}" title="Anular">
            <i class="bi bi-x-circle" aria-hidden="true"></i>
        </button>
    </form>
</div>
```

Orden fijo: Ver · Editar · acciones de flujo (enviar, emitir: `text-success`/`text-warning`) · Imprimir ·
destructiva (`text-danger`) al final. Iconos fijos: ver `bi-eye`, editar `bi-pencil`, imprimir/PDF
`bi-printer`/`bi-file-earmark-pdf`, anular `bi-x-circle`, eliminar `bi-trash3`, emitir `bi-send-check`,
duplicar `bi-copy`, descargar `bi-download`. Sustituye a `<div class="d-flex gap-1 justify-content-end">`.

### 4.6 Estado vacío — `.empty-state`

```html
<!-- Dentro de una tabla -->
<td colspan="6" class="empty-state">
    <i class="bi bi-receipt" aria-hidden="true"></i>
    <div class="empty-state-title">No hay facturas</div>
    <small class="empty-state-hint" th:if="${q != null and !q.isEmpty()}">Prueba con otros términos de búsqueda</small>
    <a th:href="@{/web/facturas/nueva}" class="btn btn-outline-primary btn-sm">
        <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>Nueva factura
    </a>
</td>

<!-- Dentro de una tarjeta o panel pequeño -->
<div class="empty-state empty-state-compact">…mismo contenido…</div>
```

El título dice qué falta («No hay facturas»), la pista dice cómo cambiarlo. El botón es outline, no
primario (el primario ya está en la cabecera).

### 4.7 Badges de estado — `.badge-status[data-status]` / `.badge-estado-*`

Dos formas equivalentes:

```html
<!-- Preferida en listados y fichas de entidades con estado: mapeo automático por valor -->
<span class="badge-status" th:attr="data-status=${f.estado?.toLowerCase()}" th:text="${f.estado}">EMITIDA</span>

<!-- Cuando el estado se calcula en la vista (booleanos, resultados) -->
<span class="badge-estado badge-estado-ok">Activo</span>
```

`data-status` acepta el valor en mayúsculas o minúsculas. Mapeo:

| Familia (clase) | Aspecto | Estados (`data-status`) |
|---|---|---|
| `.badge-estado-neutro` | gris, punto gris | BORRADOR, RECTIFICADA, CONSUMIDO, EXPIRADO, GASTO |
| `.badge-estado-info` | azul verdoso | REVISION, PLANIFICADA, CONFIRMADO, CONFIRMADA, ENVIADO, EN_PROCESO, EN_CURSO, PARCIAL, SERVIDO_PARCIAL, RECIBIDO_PARCIAL, GENERADA |
| `.badge-estado-ok` | verde | EMITIDA, PAGADA, COBRADA, ENTREGADO, FACTURADO, CONTABILIZADA, SERVIDO, RECIBIDO, ACEPTADO, CONVERTIDO, FINALIZADA, COMPLETADO, COMPLETADA, VERIFICADO, ACTIVO, OK, CONFORME, EXITO |
| `.badge-estado-aviso` | ámbar | PENDIENTE, INCIDENCIA, BLOQUEADO, AJUSTE |
| `.badge-estado-peligro` | rojo | VENCIDA, VENCIDAS, IMPAGADA, CADUCADO, RETIRADO, RECHAZADO, RECHAZADA, ERROR, NOK |
| `.badge-estado-anulado` | sin relleno, contorno discontinuo, punto hueco | ANULADA, ANULADO, CANCELADO, CANCELADA |

Booleanos: activo → `badge-estado-ok` «Activo»; inactivo → `badge-estado-neutro` «Inactivo» (no usar
`data-status="anulada"` para un cliente inactivo: inactivo no es anulado). Resultado de control APPCC
OK/NOK → `data-status` con el valor tal cual.

Un estado nuevo se añade **a la familia** en `app.css` (bloque «BADGES DE ESTADO») y a esta tabla.
No usar `.badge text-bg-*` de Bootstrap para estados de documento; `.badge` queda para recuentos
(p. ej. «3» en una pestaña).

### 4.8 Ficha de documento (ver.html) — `.doc-card`

Dos columnas en escritorio: documento a la izquierda (8/12), total y acciones a la derecha (4/12).

```html
<div class="row g-4">
    <div class="col-lg-8">
        <div class="doc-card mb-4">
            <div class="doc-card-header">
                <div class="doc-header-icon" aria-hidden="true"><i class="bi bi-receipt"></i></div>
                <div>
                    <div class="doc-card-title" th:text="${factura.cliente?.nombre}">Cliente</div>
                    <div class="doc-card-meta" th:text="${factura.cliente?.cif}">B12345678</div>
                </div>
            </div>
            <div class="doc-summary">
                <div><span class="doc-field-label">Base imponible</span><div class="doc-field-value">100,00 €</div></div>
                <div><span class="doc-field-label">Total IVA</span><div class="doc-field-value">10,00 €</div></div>
                <div><span class="doc-field-label">Fecha</span><div class="doc-field-value">01/10/2026</div></div>
            </div>
            <div class="doc-total mt-3">
                <span class="doc-total-label">Total</span>
                <span class="doc-total-amount">110,00 €</span>
            </div>
        </div>
        <div class="table-card">…líneas (4.4) con columnas de cifras en .cifra…</div>
    </div>
    <div class="col-lg-4">
        <div class="form-card total-card-big mb-3">
            <div class="label">Total factura</div>
            <div class="amount">110,00 €</div>
            <span class="badge-status" data-status="emitida">EMITIDA</span>
        </div>
        <div class="form-card">
            <h2 class="card-heading mb-3">Acciones</h2>
            <div class="action-stack">
                <a href="…" class="btn btn-outline-secondary"><i class="bi bi-pencil me-2" aria-hidden="true"></i>Editar factura</a>
                <form method="post" action="…"><button class="btn btn-primary" data-no-double-click><i class="bi bi-send me-2" aria-hidden="true"></i>Emitir factura</button></form>
                <div class="action-stack-sep"></div>
                <a href="…" class="btn btn-outline-secondary"><i class="bi bi-file-earmark-pdf me-2" aria-hidden="true"></i>Descargar PDF</a>
                <form method="post" action="…"><button class="btn btn-outline-danger" data-confirm="¿Anular esta factura?" data-no-double-click><i class="bi bi-x-circle me-2" aria-hidden="true"></i>Anular factura</button></form>
            </div>
        </div>
    </div>
</div>
```

- Fichas de maestros (cliente, artículo, proveedor) con muchos pares etiqueta/valor: tabla `.doc-table`
  dentro de un contenedor `.doc-fields` (pasa a 4 columnas cuando la tarjeta es ancha):
  ```html
  <div class="doc-fields">
      <table class="doc-table">
          <tr><th>CIF</th><td th:text="${cliente.cif}">B12345678</td><th>Teléfono</th><td>965 000 000</td></tr>
      </table>
  </div>
  ```
- `doc-header-icon` admite `.success`, `.warning`, `.danger` para reflejar el estado.
- Sustituye: `style="font-size:1.5rem;color:var(--brand-color)"` → `.doc-total-amount`;
  `<div class="d-grid gap-2">` de acciones → `.action-stack`; `<div class="doc-field-label mb-3">Acciones</div>` → `<h2 class="card-heading mb-3">Acciones</h2>`.
- El primario de la pila es la siguiente acción del flujo (Emitir, Confirmar). Si no hay, ninguno.

### 4.9 Formularios — `.form-card`, `.form-section`, `.form-actions`

```html
<div class="form-card form-card-narrow">            <!-- narrow = 760 px, formularios de maestros -->
    <form th:action="@{/web/clientes}" method="post" class="needs-validation" novalidate>
        <section class="form-section">
            <h2 class="form-section-title"><i class="bi bi-info-circle" aria-hidden="true"></i>Datos generales</h2>
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label" for="codigo">Código *</label>
                    <input type="text" id="codigo" name="codigo" class="form-control" required>
                    <div class="invalid-feedback">El código es obligatorio.</div>
                </div>
            </div>
        </section>
        <section class="form-section">
            <h2 class="form-section-title"><i class="bi bi-geo-alt" aria-hidden="true"></i>Dirección</h2>
            <p class="form-section-hint">Se imprime en facturas y albaranes.</p>   <!-- opcional -->
            …
        </section>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary"><i class="bi bi-floppy me-1" aria-hidden="true"></i>Guardar cliente</button>
            <a th:href="@{/web/clientes}" class="btn btn-ghost">Cancelar</a>
        </div>
    </form>
</div>
```

- `.form-section-title` lleva el filete carmín (firma de la casa). Una sección = un título.
- `.form-section-divider` (heredado) equivale a `.form-section` + separación; en código nuevo usar `.form-section`.
- Sustituye `style="max-width:760px"` → `.form-card-narrow`; documentos con líneas usan `.form-card form-card-wide`.
- Campos obligatorios: « *» al final de la etiqueta + `required` + `.invalid-feedback` con frase completa.
- Ayuda bajo un campo: `<div class="form-text">…</div>`.
- Botones del pie: primario primero, «Cancelar» como `btn-ghost` a su derecha.

### 4.10 Líneas de documento y totalizador — `.lines-table`, `.total-box`

```html
<div class="table-responsive">
    <table class="lines-table">
        <thead><tr>
            <th>Artículo</th>
            <th class="col-qty cifra">Cantidad</th>
            <th class="col-price cifra">Precio</th>
            <th class="col-dto cifra">Dto. %</th>
            <th class="col-iva cifra">IVA %</th>
            <th class="col-sub cifra">Subtotal</th>
            <th class="col-del"><span class="visually-hidden">Quitar</span></th>
        </tr></thead>
        <tbody>… <td class="line-subtotal">…</td>
                  <td><button type="button" class="btn btn-ghost btn-sm btn-icon btn-remove-line text-danger" aria-label="Quitar línea"><i class="bi bi-trash3" aria-hidden="true"></i></button></td> …</tbody>
    </table>
</div>
<div class="total-box">
    <div class="total-breakdown">
        <div class="total-breakdown-item"><span class="tbl">Base</span><span class="tbv">100,00 €</span></div>
        <div class="total-breakdown-item"><span class="tbl">IVA</span><span class="tbv">10,00 €</span></div>
        <div class="total-breakdown-item highlight"><span class="tbl">Total</span><span class="tbv">110,00 €</span></div>
    </div>
</div>
```

Inputs numéricos de línea: añadir `.text-end` al `<input>`.

### 4.11 Indicadores — `.stat-card`, `.kpi-card`

```html
<!-- Cifra del día; si enlaza a un listado filtrado, es un <a> -->
<a class="stat-card d-block h-100 text-decoration-none" th:href="@{/web/facturas(estado='VENCIDAS')}">
    <div class="stat-label"><i class="bi bi-hourglass-bottom" aria-hidden="true"></i>Facturas vencidas</div>
    <div class="stat-value">4</div>
    <div class="stat-sub text-danger"><i class="bi bi-exclamation-triangle-fill me-1" aria-hidden="true"></i>1.234,50 € pendiente</div>
</a>

<!-- Cifra con icono -->
<div class="kpi-card">
    <div class="kpi-icon bg-primary-light" aria-hidden="true"><i class="bi bi-receipt-cutoff"></i></div>
    <div class="kpi-body">
        <div class="kpi-value">128</div>
        <div class="kpi-label">Facturas totales</div>
        <div class="kpi-trend up"><i class="bi bi-check-circle-fill" aria-hidden="true"></i>En curso</div>
    </div>
</div>
```

`kpi-icon` admite `bg-primary-light`, `bg-success-light`, `bg-warning-light`, `bg-danger-light`,
`bg-info-light`. Úsalos por significado (peligro solo si la cifra pide atención), no para dar variedad.
Las filas de indicadores van en `row g-3` con columnas iguales (`col-sm-6 col-lg-3`) y `h-100`.

Paneles con título dentro del dashboard o de una ficha: `.form-card` + `<h2 class="card-heading">`; las
listas de enlaces dentro de un panel son `<nav class="link-list" aria-label="…">` con `<a>` directos
(sustituye a `list-group list-group-flush` con utilidades).

Gráficos: el lienzo va dentro de `.chart-box` (alto fijo 220 px); colores de `--chart-bar`,
`--chart-grid`, `--chart-axis` leídos con `getComputedStyle` y repintados en el evento `erp:tema`.
Barras horizontales HTML: `.hbar` > `.hbar-label` + `.hbar-track` > `.hbar-fill` + `.hbar-value`.

### 4.12 Avisos y toasts

```html
<!-- Aviso persistente dentro de la página (no para el resultado de guardar: eso es un toast) -->
<div class="alert alert-warning d-flex align-items-start gap-2" role="status">
    <i class="bi bi-exclamation-triangle-fill mt-1" aria-hidden="true"></i>
    <div><strong>Factura rectificativa</strong> de F-2026-0003.</div>
</div>
```

Iconos fijos: éxito `bi-check-circle-fill`, info `bi-info-circle-fill`, aviso
`bi-exclamation-triangle-fill`, error `bi-exclamation-circle-fill`. Errores con `role="alert"`, el resto
`role="status"`. Los toasts los pinta `layout.html` a partir de los flash `exito` y `error`; el de error
no se oculta solo.

### 4.13 Paginación

```html
<div th:replace="~{fragments/pagination :: paginacion(${page}, '/web/facturas')}"></div>
```

Debajo de la `.table-card`. No se maqueta a mano.

### 4.14 Modales

Bootstrap estándar (`.modal`, `.modal-header`, `.modal-title`, `.modal-body`, `.modal-footer`); ya siguen
los tokens. Pie: primario a la derecha, «Cancelar» `btn-ghost` a su izquierda. `.modal-title` es un `<h2>`.

### 4.15 Página de estado (sin layout)

Para páginas fuera del layout (error, acceso denegado): `<main class="status-page">` >
`.status-card` con `.status-icon` (o `.status-code`), `.status-title`, `.status-text`, `.status-actions`.

---

## 5. Lista de clases de componente

Layout: `skip-link`, `app-layout`, `sidebar`, `sidebar-brand`, `sidebar-brand-icon`, `sidebar-section`,
`sidebar-link`, `sidebar-link-suelto`, `sidebar-group-toggle`, `sidebar-chevron`, `sidebar-footer`,
`sidebar-footer-user`, `user-avatar`, `user-name`, `user-role`, `sidebar-overlay`, `sidebar-toggle-btn`,
`main-content`, `topbar-global`, `topbar-tools`, `theme-toggle`, `tema-icono-oscuro`, `tema-icono-claro`.

Cabecera: `page-header`, `page-header-title`, `page-header-sub`, `page-header-actions`
(heredado: `topbar`, `topbar-actions`).

Tarjetas: `form-card`, `form-card-wide`, `form-card-narrow`, `doc-card`, `card-title-row`, `card-heading`,
`link-list`.

Indicadores: `stat-card`, `stat-label`, `stat-value`, `stat-sub`, `kpi-card`, `kpi-icon`
(+ `bg-primary-light`, `bg-success-light`, `bg-warning-light`, `bg-danger-light`, `bg-info-light`),
`kpi-body`, `kpi-value`, `kpi-label`, `kpi-trend` (+ `up`, `down`), `quick-actions-grid`, `action-tile`,
`chart-box`, `hbar`, `hbar-label`, `hbar-track`, `hbar-fill`, `hbar-value`, `avisos-detalle`,
`stat-value.peligro` (cifra que pide atención; siempre con texto que lo explique).

Tablas: `table-card`, `table-card-header`, `table-card-title`, `table-card-footer`, `td-wrap`, `cifra`,
`actions`, `empty-state`, `empty-state-title`, `empty-state-hint`, `empty-state-compact`,
`pagination-info`, `td-check` (columna de casillas), `tabla-scroll` (tabla larga con scroll propio, cabecera fija),
`fecha-vencida` (fecha caducada: color + icono), `tr[role="button"]` (fila seleccionable con teclado en modales).

Filtros: `filter-bar`, `filter-bar-search`, `filter-bar-filters`, `filter-bar-actions`, `filter-bar-count`.

Estados: `badge-status` (+ `data-status`), `badge-estado`, `badge-estado-neutro`, `badge-estado-info`,
`badge-estado-ok`, `badge-estado-aviso`, `badge-estado-peligro`, `badge-estado-anulado`;
`badge badge-count` para recuentos neutros (no estados).

Botones: `btn-ghost`, `btn-icon`, `action-stack`, `action-stack-sep`, `btn-remove-line`
(+ variantes Bootstrap re-tematizadas: `btn-primary`, `btn-outline-primary`, `btn-outline-secondary`,
`btn-success`, `btn-danger`, `btn-warning`, `btn-info`, `btn-outline-success`, `btn-outline-warning`,
`btn-outline-danger`, `btn-outline-info`, `btn-link`).

Ficha: `doc-card-header`, `doc-card-title`, `doc-card-meta`, `doc-header-icon` (+ `success`, `warning`,
`danger`), `doc-field-label`, `doc-field-value`, `doc-summary`, `doc-total`, `doc-total-label`,
`doc-total-amount`, `doc-total-ruled` (total con filete propio cuando no hay `doc-summary`), `doc-notes`
(bloque de observaciones: `doc-field-label` + `<p>`), `texto-multilinea`, `tag-list` (alérgenos y etiquetas),
`doc-fields`, `doc-table`, `total-card-big` (`label`, `amount`), `border-separator`.

Formularios: `form-section`, `form-section-title`, `form-section-hint`, `form-section-divider`,
`form-actions`, `form-section-tools` (botones bajo la tabla de líneas), `lines-table`, `col-qty`, `col-price`, `col-iva`, `col-dto`, `col-sub`, `col-del`,
`line-subtotal`, `total-box`, `total-box-label`, `total-box-amount`, `total-breakdown`,
`total-breakdown-item` (+ `highlight`), `tbl`, `tbv`.

Login y estado: `login-page`, `login-card`, `login-brand`, `login-brand-icon`, `login-sub`, `login-foot`,
`status-page`, `status-card`, `status-icon` (+ `danger`), `status-code`, `status-title`, `status-text`,
`status-path`, `status-detail`, `status-actions`.

TPV: `tpv-tile`, `tpv-tile-nombre`, `tpv-tile-categoria`, `tpv-tile-precio`, `tpv-qty`, `tpv-cantidad`,
`tpv-importe`, `tpv-ticket` (ticket fijo al hacer scroll en escritorio), `tpv-carrito`.

Calendario: `cal-dia-body`, `cal-evento` (botón de evento; el color de tipo con `bg-*-subtle`).

Atributos de comportamiento (`app.js`): `data-confirm`, `data-navigate`, `data-autosubmit`,
`data-no-double-click`, `data-loading-text`, `data-history-back`, `data-searchable` (TomSelect).

---

## 6. Lista de comprobación por pantalla

- [ ] Sin `style=` salvo las excepciones de la regla 1.
- [ ] Sin colores literales (`#…`, `text-primary` decorativo) ni `var(--brand-color)` en línea.
- [ ] `.page-header` con un `<h1>`; un único `btn-primary`.
- [ ] Columnas de cifras con `.cifra` en `th` y `td`.
- [ ] Badges de estado con `badge-status`+`data-status` o `badge-estado-*`.
- [ ] Botones solo-icono con `btn-icon`, `aria-label` y `title`; iconos con `aria-hidden="true"`.
- [ ] Estados vacíos con icono, título y pista.
- [ ] Se ve bien en modo oscuro (conmutador de la barra superior) y a 820 px de ancho (tablet vertical).
- [ ] Ningún `x-show` comparte elemento con `d-block`/`d-flex`/`d-grid`.
- [ ] Textos comprobados por tests intactos (`grep -rn` en `src/test`).

## Checklist UI/UX Pro Max (auditada en navegador)

Reglas de la skill `ui-ux-pro-max` (`.claude/skills/ui-ux-pro-max`) que se comprobaron en 30 pantallas, a 375, 768, 1024 y 1440 px y en tablet táctil:

- **Objetivo táctil ≥ 44×44 px** con puntero grueso (`@media (pointer: coarse)` en `app.css`). Los botones llegan por relleno, no por `min-height`, porque son `inline-block`.
- **Sin scroll horizontal de página.** Toda tabla va dentro de `.table-responsive` o `.table-card`; ambos llevan `position: relative` para que los `.visually-hidden` de las celdas no estiren la página.
- **`cursor: pointer`** en todo lo pulsable y **transiciones de 150–300 ms**; `prefers-reduced-motion` las anula.
- **Todo campo tiene etiqueta**: `<label for>` o, en controles generados por JS, `aria-label`.
- **Nada de emojis como iconos**: solo bootstrap-icons con `aria-hidden="true"`.
- **El color no es el único indicador**: los badges de estado llevan punto y texto.
