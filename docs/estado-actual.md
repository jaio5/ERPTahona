# Estado actual de ERP Tahona

**Última validación:** 2 de julio de 2026
**Rama:** `refactor-general` / `desarrollo` (sincronizadas)

Este documento es la referencia rápida del estado técnico actual. Las auditorías fechadas se conservan como histórico y pueden contener hallazgos ya corregidos.

## Estado del build

```powershell
.\mvnw.cmd clean verify
```

| Métrica | Resultado |
|---------|-----------|
| Build | `BUILD SUCCESS` |
| Fuentes Java | ~265 |
| Pruebas | 204 |
| Fallos | 0 |
| Errores | 0 |
| Gate JaCoCo líneas | 50 % mínimo |
| Gate JaCoCo ramas | 20 % mínimo |

## Validación funcional (2 de julio de 2026)

- 55 rutas web verificadas sin errores; 0 errores de consola JavaScript.
- CRUD y transiciones de estado probados en los 22 módulos (crear, editar,
  confirmar/entregar/facturar/aprobar/anular/rectificar según el módulo).
- Cumplimiento fiscal operable de extremo a extremo desde
  `Fiscal → Cumplimiento`: datos del SIF, declaración responsable con huella,
  inicio de VERI*FACTU y diagnóstico "listo para emisión".
- Emisión de factura verificada con registro de alta oficial de AEAT
  (XML tikeV1.0 validado por XSD, huella Orden HAC/1177/2024, firma digital,
  cadena de evidencias íntegra) y QR tributario en el PDF.
- Inalterabilidad activa: las facturas emitidas no se editan ni se borran
  (exigen rectificativa) y la emisión requiere cumplimiento fiscal completo.
- Backups desde la interfaz y Modelo 347 con desglose trimestral y compras.

Pendiente externo antes de remisión real a AEAT: validar contra el entorno de
pruebas oficial con el certificado real de la empresa y presentar la
declaración responsable (documento que ya genera la aplicación).

## Capacidades disponibles

- Clientes, proveedores y catálogo de artículos con tarifas especiales por cliente.
- Presupuestos, pedidos de venta, albaranes y facturas (con PDF, anulación, rectificación).

- Compras: pedidos, facturas de compra y recepciones con entrada de stock.
- Recetas, órdenes de producción, horneadas **con costes reales** (mano de obra, energía, materiales) y coste unitario calculado.
- Mermas y APPCC.
- Almacenes, **stock por almacén** (`ArticuloAlmacen`), movimientos, lotes y trazabilidad.
- Vehículos, rutas, hojas de ruta, entregas e incidencias. API móvil `/api/movil`.
- Tesorería: caja, bancos, **importación CSV de extractos** y **conciliación bancaria asistida**.
- Contabilidad: plan contable, asientos automáticos/manuales, balance y cierre de ejercicio.
- Auditoría, RGPD, backups y configuración de empresa.
- Modelo 347 y soporte técnico VeriFactu (AEAT SOAP + firma digital + encadenamiento).
- Calendario semanal y planificador diario con necesidades de materia prima.

## Cambios recientes (junio 2026)

### Seguridad y calidad de código
- CSRF corregido en logout (redirige a `/web/login?forbidden` correctamente).
- HSTS y CSP activados con nonce por petición (`CspNonceFilter`).
- `@PreAuthorize` añadido en `UsuarioService`, `BackupService`, `AsientoContableService`.
- Restricciones de rol aplicadas en `WebEntityController` para módulos admin y finanzas.
- 15 controladores web convertidos de package-private a `public class` (necesario para proxies CGLIB).
- 4 métodos `@Modifying` sin `@Transactional` corregidos en repositorios.
- 6 métodos `@Scheduled` de `NotificacionService` envueltos en try-catch; doble consulta eliminada.

### Nuevas funcionalidades

- **Stock por almacén**: entidad `ArticuloAlmacen`, sincronizada en entradas/salidas.
- **Costes de horneada**: campos `costeManoObra`, `costeEnergia`, `costeMateriales` + `costeUnitario()`.
- **Conciliación bancaria**: `/web/tesoreria/conciliacion` con candidatos automáticos.
- **Importación CSV mejorada**: soporte de campos entre comillas con comas internas.

### Frontend Alpine.js
- Bug crítico corregido: los 6 componentes Alpine usaban asignaciones directas (`@click="tab='x'"`) incompatibles con `@alpinejs/csp`.
- Todos los componentes convertidos al patrón `setTab()`/`isTab()` con métodos en `Alpine.data()`.
- `init()` lifecycle hook en lugar de `x-init="method()"` en HTML.
- `[x-cloak]` añadido en `app.css` para eliminar el parpadeo visual.

### Migraciones Flyway
- Corregidos dos conflictos de versión duplicada: V10 y V11 tenían dos archivos cada uno.
- Las migraciones de stock por almacén y costes de horneada renombradas a V26 y V27.

## Riesgos abiertos

### Prioridad alta

1. **Permisos JSON**: la matriz JSON de permisos por módulo/acción está modelada en `Rol` pero no se aplica como autorización efectiva en controladores. La seguridad real depende únicamente de los roles Spring Security.
2. **VeriFactu sin certificar**: el endpoint apunta a `prewww2.aeat.es` (sandbox). Para producción real cambiar a `www2.agenciatributaria.gob.es` + certificado digital real + `VERIFACTU_AEAT_ENABLED=true`.
3. **Pruebas MySQL/Flyway**: los Testcontainers necesitan Docker; sin Docker, las pruebas de integración con MySQL real no se ejecutan.
4. **Backup sin restauración probada**: el proceso de backup funciona; la restauración no se ha probado sobre una instancia MySQL independiente.

### Prioridad media

1. El CRUD genérico (`WebEntityController`) puede evitar invariantes de dominio en entidades complejas.
2. Límites multipart no definidos explícitamente en configuración.
3. Operaciones de backup aceptan rutas sin confinamiento de directorio explícito.
4. La configuración de pruebas está parcialmente duplicada.
5. El script `check-production-readiness.ps1` puede usar nombres de variables obsoletos.

## Limitaciones de la validación actual

- Docker/Testcontainers no disponible en entorno local: las pruebas MySQL no se ejecutaron.
- No se realizó prueba de carga ni pentest.
- No se probó restauración sobre una base MySQL independiente.
- No se realizaron envíos reales a la AEAT.
