# Estado actual de ERP Tahona

**Última validación:** 4 de julio de 2026 (auditoría GO/NO-GO pre-lanzamiento)
**Rama:** `chore/fase4-deuda` (PRs hacia `main`)

Este documento es la referencia rápida del estado técnico actual.

## Estado del build

```powershell
.\mvnw.cmd verify
```

| Métrica | Resultado |
|---------|-----------|
| Build | `BUILD SUCCESS` |
| Pruebas | 267 |
| Fallos | 0 |
| Gate JaCoCo | Cumplido (líneas y ramas) |

## Capacidades disponibles

- Clientes, proveedores y catálogo con tarifas especiales por cliente.
- Presupuestos, pedidos, albaranes y facturas (PDF, anulación, rectificativa).
- Compras: pedidos, facturas de compra y recepciones con entrada de stock.
- Recetas, órdenes de producción y horneadas con costes reales y coste unitario.
- Mermas, APPCC, almacenes, stock por almacén, lotes y trazabilidad.
- Reparto: vehículos, rutas, hojas de ruta, entregas, incidencias y API móvil `/api/movil`.
- Tesorería: caja, bancos, importación CSV y Norma 43, conciliación asistida, cartera de cobros/pagos, remesas SEPA (pain.008) y mandatos.
- Contabilidad: plan PGC, asientos automáticos/manuales, libro mayor, balance, PyG, apertura y cierre de ejercicio.
- Fiscal: VeriFactu (registro alta/anulación, hash encadenado, firma, QR, XML oficial), Modelo 347, libros de IVA, borradores 303/111, export Facturae 3.2.2 (sin firma).
- TPV de mostrador con factura simplificada VeriFactu; envío de facturas por email.
- Auditoría, RGPD básico, backups con restore endurecido y configuración de empresa.

## Seguridad (verificada en auditoría 2026-07-04)

- Permisos granulares (módulo/acción del rol JSON) **aplicados en servidor** en toda la API REST vía `@PreAuthorize("@permisos.puede(...)")` y guards default-deny; tests negativos en `RestApiPermisosTest`.
- Inalterabilidad RRSIF: facturas emitidas/enviadas y sus líneas rechazan escritura (API genérica read-only + guard en líneas hijas).
- Backups: rutas confinadas a `backup.directory` (anti path-traversal), solo `backup_*.sql`, operaciones solo ADMIN.
- Guard de arranque en `prod`: secretos obligatorios sin placeholders, `ddl-auto=validate`, sin fallback H2, y VeriFactu no puede activarse apuntando al sandbox.
- Cambio de contraseña obligatorio en primer login del admin; rate limiting de login por IP real (RemoteIpValve tras Caddy).
- CSP con nonce por petición. Excepción conocida: `'unsafe-eval'` en `script-src` (requerido por el build estándar de Alpine.js; la migración al build CSP se abortó — 6 plantillas con expresiones inline).

## Despliegue

Docker Compose: MySQL 8 + aplicación + Caddy (TLS interno o Let's Encrypt). La app no publica puertos; todo entra por HTTPS. Cookie de sesión `Secure` activa por defecto. Ver [RUNBOOK](RUNBOOK.md).

Migraciones Flyway V0..V42; `dev` también valida (`ddl-auto=validate`).

## Pendiente antes del GO (acciones fuera del código)

1. **Fecha límite interna y responsable de la validación VeriFactu** contra el sandbox AEAT — rellenar en [plan-lanzamiento.md](plan-lanzamiento.md) §4.1 (plazo legal: 01/07/2027; se lanza con remisión deshabilitada).
2. **Ensayo real de restore** en el servidor y anotar la fila en [RUNBOOK](RUNBOOK.md) §4.3.
3. Verificar en el host de producción: secretos del `.env` generados, `flyway_schema_history` en V42, cookie `Secure` sobre HTTPS.

Detalle completo en la auditoría del plan de lanzamiento.
