# Referencia de API REST

Todas las rutas requieren sesión autenticada. El token CSRF debe incluirse en peticiones mutables en el header `X-CSRF-TOKEN` (disponible en `<meta name="csrf-token">`).

---

## Clientes y artículos — `/api/web`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/resumen` | Contadores globales (clientes, artículos, facturas, etc.) |
| GET | `/api/web/clientes` | Lista clientes; `?q=` para buscar |
| GET | `/api/web/clientes/{id}` | Obtiene un cliente |
| POST | `/api/web/clientes` | Crea un cliente (body JSON) |
| PUT | `/api/web/clientes/{id}` | Actualiza un cliente |
| POST | `/api/web/clientes/{id}/baja` | Desactiva un cliente |
| GET | `/api/web/articulos` | Lista artículos; `?q=` para buscar |
| GET | `/api/web/articulos/{id}` | Obtiene un artículo |
| POST | `/api/web/articulos` | Crea un artículo |
| POST | `/api/web/articulos/{id}/baja` | Desactiva un artículo |
| GET | `/api/web/clientes/{id}/albaranes-recientes` | Albaranes recientes del cliente |

---

## CRUD genérico — `/api/web/entities`

Acceso por nombre de módulo. Módulos disponibles: `clientes`, `proveedores`, `articulos`, `almacenes`, `bancos`, `cajas`, `vehiculos`, `rutas-reparto`, `roles`, `usuarios`, `recetas`, `ordenes-produccion`, `lotes`, `tarifas`, `presupuestos`, `pedidos-venta`, entre otros.

| Método | Ruta | Descripción | Rol requerido |
|--------|------|-------------|---------------|
| GET | `/api/web/entities` | Lista módulos disponibles | Auth |
| GET | `/api/web/entities/{module}` | Lista entidades; `?q=` para buscar | Auth |
| GET | `/api/web/entities/{module}/{id}` | Obtiene una entidad | Auth |
| POST | `/api/web/entities/{module}` | Crea entidad | Auth |
| PUT | `/api/web/entities/{module}/{id}` | Actualiza entidad | Auth |
| POST | `/api/web/entities/{module}/{id}/baja` | Desactiva | Auth |
| POST | `/api/web/entities/{module}/{id}/activar` | Reactiva | Auth |
| DELETE | `/api/web/entities/{module}/{id}` | Elimina | ADMIN |

Módulos solo para ADMIN: `usuarios`, `roles`, `empresa`, `auditoria`, `verifactu-evidencias`, `backups`.
Módulos solo para CONTABLE/ADMIN: `asientos`, `plan-contable`, `plan-cuentas`, `movimientos-banco`, `bancos`, `movimientos-caja`, `cajas`, `modelo347`.

---

## Documentos y facturas — `/api/web`

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/web/documentos/pedido` | Crea pedido de venta |
| POST | `/api/web/documentos/albaran` | Crea albarán |
| POST | `/api/web/documentos/factura` | Crea factura en borrador |
| POST | `/api/web/facturas/{id}/emitir` | Emite una factura |
| POST | `/api/web/facturas/{id}/anular` | Anula una factura |
| POST | `/api/web/albaranes/{id}/convertir` | Convierte albarán a factura |
| GET | `/api/web/pendientes-facturar` | Albaranes pendientes de facturar |

---

## Extractos bancarios — `/api/web/extractos`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/extractos` | Lista movimientos; `?desde=&hasta=` (yyyy-MM-dd) |
| GET | `/api/web/extractos/no-conciliados` | Movimientos sin conciliar |
| POST | `/api/web/extractos/importar` | Importa CSV; form-data: `archivo` + `bancoId` |
| POST | `/api/web/extractos/{id}/conciliar` | Marca movimiento como conciliado |

**Formato CSV de extracto:**
```
fecha;concepto;importe
01/06/2026;TRANSFERENCIA CLIENTE;1500.00
02/06/2026;PAGO PROVEEDOR;-320.50
```

---

## Inventario — `/api/web/reportes`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/reportes/inventario/valoracion` | Valoración completa del inventario |
| GET | `/api/web/reportes/inventario/movimientos` | Movimientos; `?desde=&hasta=` |
| POST | `/api/web/reportes/inventario/ajustar` | Ajuste manual; `?articuloId=&cantidadNueva=&motivo=` |

**Respuesta de valoración:**
```json
{
  "items": [{"id": 1, "codigo": "PAN01", "nombre": "Pan blanco", "stock": 50, "coste": 0.30, "valor": 15.00}],
  "totalValor": 15.00
}
```

---

## Contabilidad — `/api/web/contabilidad`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/contabilidad/balance` | Balance; `?fecha=yyyy-MM-dd` |
| POST | `/api/web/contabilidad/cierre` | Cierre de ejercicio; `?anio=2025` |

---

## Producción — `/api/produccion`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/produccion/recetas` | Lista recetas |
| POST | `/api/produccion/ordenes` | Crea orden de producción |
| POST | `/api/produccion/ordenes/{id}/iniciar` | Inicia orden |
| POST | `/api/produccion/ordenes/{id}/finalizar` | Finaliza orden |
| GET | `/api/produccion/horneadas` | Lista horneadas |
| POST | `/api/produccion/horneadas` | Crea horneada |

---

## Hojas de ruta — `/api/web/hojas-ruta`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/hojas-ruta/{id}/entregas` | Entregas de una hoja de ruta |
| POST | `/api/web/hojas-ruta/{id}/vincular-albaranes` | Vincula albaranes a la hoja |
| GET | `/api/web/albaranes/pendientes-reparto` | Albaranes pendientes de asignar a ruta |

---

## API móvil de repartidor — `/api/movil`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/movil/mi-ruta` | Ruta activa del repartidor autenticado |
| POST | `/api/movil/iniciar-ruta/{hojaId}` | Inicia la ejecución de una ruta |
| POST | `/api/movil/confirmar/{entregaId}` | Confirma entrega |
| POST | `/api/movil/incidencia/{entregaId}` | Registra incidencia en entrega |
| POST | `/api/movil/finalizar-ruta/{hojaId}` | Finaliza la ruta |

---

## Dashboard y alertas

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/alertas` | Alertas activas del sistema (stock bajo, facturas vencidas, etc.) |
| GET | `/api/dashboard/ventas-mensuales` | Ventas agrupadas por mes |
| GET | `/api/dashboard/produccion-estado` | Estado de las órdenes de producción |
| GET | `/api/web/calendario` | Eventos calendario; `?inicio=&fin=` (yyyy-MM-dd) |
| GET | `/api/web/planificador` | Datos del planificador; `?fecha=yyyy-MM-dd` |

---

## Recepciones — `/api/web/recepciones`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/recepciones/{id}/lineas` | Líneas de una recepción |
| POST | `/api/web/recepciones/{id}/confirmar` | Confirma recepción y registra stock |

---

## Tarifas — `/api/web/tarifas`

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/web/tarifas` | Lista tarifas por cliente |
| POST | `/api/web/tarifas` | Crea tarifa especial cliente/artículo |
| PUT | `/api/web/tarifas/{id}` | Actualiza tarifa |
| DELETE | `/api/web/tarifas/{id}` | Elimina tarifa |

---

## Errores comunes

| Código | Causa |
|--------|-------|
| 400 | Argumento inválido o campo requerido ausente |
| 401 | Sin sesión autenticada o CSRF inválido |
| 403 | Rol insuficiente para la operación |
| 404 | Entidad no encontrada |
| 409 | Violación de integridad (duplicado, clave foránea en uso) |
| 500 | Error interno del servidor (consultar logs) |
