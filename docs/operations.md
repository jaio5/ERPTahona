# Operacion diaria

## Arranque normal

En produccion se debe arrancar siempre con:

```powershell
.\scripts\run-production.ps1
```

El script carga `.env.production.local`, fuerza el perfil `prod`, ejecuta prechequeos y arranca el ultimo JAR disponible en `target`.

## Usuarios

- Cada persona debe trabajar con su propio usuario.
- El usuario `admin` se reserva para administracion.
- Si se crea una contrasena temporal, debe cambiarse en el primer acceso.
- Los usuarios que dejen de trabajar deben desactivarse, no borrarse si tienen actividad historica.

## Flujo de facturacion

1. Revisar datos de cliente.
2. Revisar articulos, IVA y precios.
3. Emitir presupuesto, pedido, albaran o factura segun el flujo real.
4. Confirmar que la factura queda con numeracion correcta.
5. Si VERI*FACTU esta iniciado, revisar el estado de envio/evidencia.
6. Ante una factura incorrecta, usar anulacion o rectificacion segun corresponda; no manipular datos directamente en base.

## Backups

Recomendacion minima:

- Backup diario automatico o manual al cierre.
- Copia externa periodica.
- Prueba de restauracion al menos mensual.
- Conservacion alineada con necesidades fiscales y de negocio.

El backup usa el nombre de base de datos indicado en `SPRING_DATASOURCE_URL`. No depende de un nombre fijo.

## Restauracion

La restauracion debe probarse primero en una base de copia. Antes de restaurar sobre produccion:

1. Parar la aplicacion.
2. Hacer una copia del estado actual.
3. Confirmar que el backup elegido corresponde a la fecha correcta.
4. Restaurar.
5. Arrancar y revisar datos maestros, facturas, usuarios y auditoria.

## Auditoria

La auditoria sirve para revisar acciones relevantes del sistema. La exportacion esta protegida por permiso `auditoria.exportar`.

Debe revisarse cuando haya:

- Cambios de usuarios o roles.
- Operaciones de backup/restauracion.
- Cambios de facturacion.
- Incidencias de acceso.

## Mantenimiento

Antes de actualizar:

```powershell
.\mvnw.cmd test
.\scripts\build-production.ps1
```

Despues de actualizar:

- Arrancar con `.\scripts\run-production.ps1`.
- Revisar logs.
- Comprobar login.
- Comprobar una consulta de clientes/articulos.
- Comprobar pantalla de facturas.
- Comprobar estado VERI*FACTU si esta iniciado.
