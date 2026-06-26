# ERP Tahona - Anchored Summary

## Goal
Transition ERP from legacy JavaFX + Swing UI to modern Spring Boot + Thymeleaf web UI. Fix critical backend issues (in-memory filtering, silent data loss, XSS). Prepare for Docker deployment.

## Constraints & Preferences
- All decimal values in forms must display and accept comma as decimal separator (Spanish format: `2,50` not `2.50`)
- Product selection in invoice/delivery note creation must work with a searchable dropdown (TomSelect)
- List tables must not compress into unreadable narrow columns
- Form screens must not appear unnecessarily narrow on wide screens
- User wants priority on the most impactful fixes first

## Progress

### Done
#### Phase 1: Legacy Cleanup
- Deleted **45+ JavaFX controllers** (AlbaranController, FacturaController, ClienteFormController, etc.)
- Deleted **50+ FXML files** (ui/ directory)
- Deleted **4 legacy test files** (FxmlContractTest, FxmlLoaderSmokeTest, etc.)
- Deleted **3 tool files** (DbBootstrapper, KeystoreInspector, ProductionReadinessCheck)
- Deleted **3 legacy UI classes** (DialogUtils, Dialogs, FormUtils)
- Deleted **old CSS themes** (modern-theme.css, simple-theme.css) → consolidated to `css/app.css`
- Deleted `ErpLauncher.java` (JavaFX entry point), `CrudHelper.java`, `PasswordHashGenerator.java`
- Deleted `RepartoProduccionWebController.java`, `VentasComprasWebController.java`, `AdminWebController.java`
- Cleaned up stale entities: removed `PresupuestoLinea.tipoIva`, `FacturaCompra` unused annotations, `AsientoContableLinea` stale fields, `Usuario.admin` field

#### Phase 2: Web UI Migration
- Created **20+ new Thymeleaf templates** with full CRUD: almacenes, appcc, articulos, clientes, devoluciones, facturas, facturas-compra, horneadas, lotes, mermas, ordenes-produccion, pedidos-compra, pedidos-venta, presupuestos, proveedores, recetas, rutas, usuarios, vehiculos, albaranes
- Created **view templates** (ver.html) for all modules
- Created **specialized templates**: dashboard, calendar (planificador), balance, pagination fragment, fiscal/, recepciones/, pdf/, mermas/
- Created **web controllers** (AppccWebController, DevolucionWebController, PedidoVentaWebController, etc.) with `/web/{modulo}` mapping
- Created **REST controllers** (WebApiController, WebEntityController, WebChildEntityController, DashboardRestController, DocumentoRestController, etc.) with `/api/web/**` mapping
- Created **DTO classes**: PageResponse, ArticuloDto, ClienteDto, DevolucionDto, ProduccionDto, PendienteFacturarDto, AlbaranPendienteDto
- Created **menu fragment** (fragments/menu.html) with responsive navigation
- Created **pagination fragment** (fragments/pagination.html)
- Created **erp-crud.js**, **app-config.js**, **erp-core.js**, **erp-tpv-actions.js** for Alpine.js SPA functionality
- Created **BaseWebController** as shared superclass, **BreadcrumbBuilder**, **DocumentoParserUtil**
- Created **enums package** with shared enumerations
- Created **new entities**: Fianza, Merma, Recepcion, RecepcionLinea, TarifaCliente, AlbaranSerieSequence
- Created **new services**: DocumentoService, MermaService, RecepcionService, StockService, TarifaClienteService, AlbaranNumeroService
- Created **new repositories**: MermaRepository, RecepcionRepository, RecepcionLineaRepository, TarifaClienteRepository, AlbaranSerieSequenceRepository
- Created **Flyway migrations**: V15 (tarifas_cliente), V16 (mermas), V17 (recepciones), V18 (fianzas), V19 (albaran_estado), V20 (indexes_and_constraints), V21 (articulos_optimistic_lock), V22 (albaran_sequences), V23 (factura_albaran_optimistic_lock), V24 (presupuestos_tables), V25 (pedidos_venta_tables)
- Fixed V0 migration (proveedores table)
- Updated existing migrations (V12 alergenos, V14 tipo_impositivo)

#### Phase 3: CSS/JS Refactoring
- Rewrote `css/app.css` (~650 lines): responsive layouts, CSS variables, form-grid, table-responsive, modal, sidebar, utilities
- Created `app.css` (root) as entry point
- Migrated all Thymeleaf templates from inline styles to CSS classes in `css/app.css`
- Rewrote `js/app.js` (~150 lines): Alpine.js store with esc(), fmtCell(), modal management, API helpers, toast notifications
- Rewrote `app.js` (~1000 lines): main SPA logic with CRUD operations, PDF downloads, dashboard widgets, TPV module
- Created `app-config.js`: module definitions, column schemas, child schemas, SVG icons, empty states
- Created `erp-crud.js`: table rendering, form rendering, inline editing, child entity management
- Created `erp-tpv-actions.js`: TPV/quick-sale modal with search, cart, save flow
- Created `erp-core.js`: API fetch wrapper, download blob, formatMoneyEs, todayISO, SVG icons helper

#### Phase 4: Critical Backend Fixes
1. **Fixed `orElse(new Entity())` → `orElseThrow(IllegalArgumentException)`** in **17 controllers**: AlmacenWebController, AppccWebController, ProveedorWebController, VehiculoWebController, RutaRepartoWebController, DevolucionWebController, FacturaCompraWebController, HorneadaWebController, LoteWebController, MermaWebController, PedidoCompraWebController, PresupuestoWebController, ClienteWebController, ArticuloWebController, RecetaWebController, OrdenProduccionWebController, EmpresaWebController — prevents silent data loss on invalid IDs

2. **Fixed in-memory filtering (`findAll().stream().filter()`) → DB queries** in **14 controllers**: AlmacenWebController, AppccWebController, ProveedoresWebController, VehiculoWebController, RutaRepartoWebController, DevolucionWebController, FacturaCompraWebController, HorneadaWebController, MermaWebController, PedidoCompraWebController, PedidoVentaWebController, PresupuestoWebController, LoteWebController, ClienteWebController (CSV export)

3. **Added `@Query` search methods** to **7 repositories**: AlmacenRepository, AppccControlRepository, VehiculoRepository, HorneadaRepository, MermaRepository, PedidoCompraRepository, DevolucionRepository — uses `LOWER()` + `LIKE CONCAT('%', :q, '%')`

4. **Added service-layer search methods** to **8 services**: AlmacenService, AppccControlService, VehiculoService, HorneadaService, MermaService, DevolucionService, PedidoCompraService, PedidoService

5. **Fixed `throws Exception` on PDF endpoints** → try-catch + RuntimeException in FacturaWebController, AlbaranWebController, ClienteWebController

6. **Fixed ClienteWebController CSV export**: replaced in-memory stream filter with paginated service call

7. **Fixed XSS via `innerHTML`** in `erp-tpv-actions.js`:
   - All `id` values in onclick handlers wrapped with `Number()` to coerce to safe numeric values
   - All string values (`label`, `articulo`, `descripcion`, `childLabel`) wrapped with `this.esc()` (HTML-entity escaping: `&`, `<`, `>`, `"`, `'`)
   - `runCustomAction` onclick handler refactored from inline string concatenation to data attributes + `runCustomActionFromData()`
   - All attribute values (`title`, `value`, `data-*`) use `this.esc()`
   - `childPath` in `deleteChild()` onclick escaped with `this.esc()`

8. **Fixed ProveedorWebController**: changed `service.buscarPorCriterio(q)` → `service.buscar(q)` to match service method name (compilation fix)

#### Phase 5: Forms & Templates
- `needs-validation` / `novalidate` on all 13+ forms
- Hidden `id` fields on all 11+ edit forms
- `th:selected` on all 5+ select dropdowns
- `lang="es"` + favicon on layout.html
- Albaran `/editar` endpoint and edit buttons
- IIFE pattern for form JS with graceful TomSelect degradation (try/catch)
- TomSelect CDN loads at bottom of `<body>` in layout.html
- CSRF meta fix in layout.html
- APPCC field name fix in appcc/formulario.html
- Rectificativa field fix in facturas/rectificativa.html
- NPE null-safe formatting throughout (`#numbers.formatDecimal(...,'COMMA',...)`)

#### Phase 6: Docker Setup
- Created `Dockerfile`: multi-stage (maven:3.9-eclipse-temurin-17 builder → eclipse-temurin:17-jre runtime), port 8080, prod profile
- Created `docker-compose.yml`: MySQL 8.0 (volume, healthcheck) + app (depends_on: healthy)
- Created `.dockerignore`: excludes target/, .git/, logs, etc.

### In Progress
- (none)

### Blocked
- (none)

## Key Decisions
- **DB queries replace in-memory stream().filter()**: `LOWER()` + `LIKE CONCAT('%', :q, '%')` in `@Query` on repositories
- **Fail-fast on invalid IDs**: `orElseThrow(IllegalArgumentException)` instead of `orElse(new Entity())`
- **HTML-escaping for XSS prevention**: `esc(s)` function replaces `&<>"'` with entities; `Number()` for numeric IDs
- **Data attributes for onclick**: Moved `runCustomAction` from inline string interpolation to `data-action-*` attributes + delegated handler
- **Alpine.js store pattern**: All JS state/methods on a single `Alpine.store('erp')` object shared across all modules
- **No jQuery dependency**: All JS uses native DOM API + Alpine.js
- **CSS variables for theming**: `css/app.css` uses `--primary`, `--bg`, `--border`, etc.
- **Flyway for schema migrations**: All new tables via versioned migrations
- **prod profile with env vars**: Spring prod profile reads DB/keystore config from environment (for Docker)

## Next Steps
1. **God class decomposition**: `VerifactuService.java` (959 lines), `VerifactuAeatSoapClient.java` (554 lines), `RgpdSolicitudService.java` (477 lines)
2. **N+1 queries in loops**: `save()` in loop in `EmpresaConfigService`, `findAll()` in `NotificacionService`, `anonimizarDatosCliente()` in `RgpdSolicitudService`
3. **Schema mismatches**: `factura_lineas` missing `precio` and `descuento` columns in V1 migration
4. **Accessibility**: `id`/`for` attributes on all form labels
5. **JS consolidation**: Extract shared JS utilities from inline IIFE patterns into reusable modules
6. **Batch operations**: `saveAll()` instead of `save()` in loops in services
7. **Move inline styles to CSS classes** (remaining templates with direct style attributes)
8. **Add `Access-Control-Expose-Headers`** for Content-Disposition in PDF downloads
9. **Add missing line-item product tables** to forms for pedidos-venta, presupuestos, pedidos-compra, devoluciones
10. **Missing list-only templates** for: empresas, vehiculos

## Relevant Files
### Controllers
- `src/main/java/alicanteweb/erp/controller/web/` — all web controllers
- `src/main/java/alicanteweb/erp/controller/rest/` — all REST controllers
- `src/main/java/alicanteweb/erp/controller/web/WebController.java` — base layout helper
- `src/main/java/alicanteweb/erp/controller/web/BaseWebController.java` — shared superclass
- `src/main/java/alicanteweb/erp/controller/web/BreadcrumbBuilder.java` — nav breadcrumbs

### Services
- `src/main/java/alicanteweb/erp/service/VerifactuService.java` — God class (959 lines), orchestrates Verifactu
- `src/main/java/alicanteweb/erp/service/VerifactuAeatSoapClient.java` — God class (554 lines), AEAT SOAP
- `src/main/java/alicanteweb/erp/service/RgpdSolicitudService.java` — God class (477 lines), RGPD
- `src/main/java/alicanteweb/erp/service/FacturaService.java` — invoice lifecycle
- `src/main/java/alicanteweb/erp/service/ImpresionService.java` — PDF generation
- `src/main/java/alicanteweb/erp/service/DocumentoService.java` — invoice/delivery note creation
- `src/main/java/alicanteweb/erp/service/AlbaranService.java` — delivery note lifecycle

### Frontend
- `src/main/resources/static/erp-tpv-actions.js` — XSS fixes applied, TPV module
- `src/main/resources/static/app.js` — Alpine store, main SPA logic
- `src/main/resources/static/app-config.js` — module definitions
- `src/main/resources/static/erp-crud.js` — CRUD rendering
- `src/main/resources/static/erp-core.js` — API helpers
- `src/main/resources/static/js/app.js` — legacy non-SPA helpers
- `src/main/resources/static/css/app.css` — main stylesheet
- `src/main/resources/templates/layout.html` — base layout (TomSelect, CSRF, lang=es, favicon)
- `src/main/resources/templates/web/app.html` — SPA shell

### Infrastructure
- `Dockerfile` — multi-stage build
- `docker-compose.yml` — MySQL + app on port 8080
- `.dockerignore`

### Tests
- `src/test/java/alicanteweb/erp/WebSecurityTest.java` — 9 tests, security
- `src/test/java/alicanteweb/erp/WebSurfaceTest.java` — 2 tests, surface
- 90 tests total, 0 failures (as of last run)
