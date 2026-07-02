# Manual de la aplicación ERP Tahona

## Objetivo

ERP Tahona centraliza la operativa de una panadería o tahona: clientes, proveedores, catálogo, ventas, compras, producción, inventario, trazabilidad, reparto, tesorería, contabilidad, fiscalidad y administración.

La aplicación es web. La interfaz principal se encuentra bajo `/web` y requiere autenticación.

## Módulos

| Área | Ruta principal | Funciones principales |
|------|---------------|----------------------|
| Panel | `/web/dashboard` | Resumen de KPIs, alertas y gráficos de ventas/producción |

| Clientes | `/web/clientes` | Alta, edición, consulta, exportación CSV/JSON, tarifas especiales |
| Proveedores | `/web/proveedores` | Maestro de proveedores |
| Artículos | `/web/articulos` | Catálogo, precios, IVA, stock, alérgenos y tarifas por cliente |
| Presupuestos | `/web/presupuestos` | Creación, líneas, totales y aprobación |
| Pedidos de venta | `/web/pedidos-venta` | Creación, confirmación y seguimiento |
| Albaranes | `/web/albaranes` | Preparación, entrega, descuento de stock, PDF y conversión a factura |
| Facturas | `/web/facturas` | Borrador, revisión, emisión, anulación, rectificación y PDF |
| Compras | `/web/pedidos-compra` | Pedidos, facturas y recepciones de mercancía |
| Producción | `/web/ordenes-produccion` | Recetas, ingredientes, órdenes, horneadas (con costes reales), planificación y mermas |
| Inventario | `/web/reportes` | Almacenes, valoración de inventario, movimientos, lotes, ajustes manuales |
| Trazabilidad | `/web/lotes` | Seguimiento de lotes hacia materias primas y productos |
| Reparto | `/web/hojas-ruta` | Vehículos, rutas, hojas de ruta, entregas e incidencias. API móvil `/api/movil` |
| Tesorería | `/web/tesoreria` | Caja, bancos, movimientos e importación de extractos CSV |
| Extractos | `/web/tesoreria/extractos` | Extractos bancarios, importación CSV y conciliación con facturas |
| Contabilidad | `/web/contabilidad` | Plan contable, asientos manuales/automáticos, balance y cierre de ejercicio |
| Fiscal | `/web/modelo347`, `/web/verifactu` | Modelo 347 y evidencias VeriFactu |
| APPCC | `/web/appcc` | Controles alimentarios y acciones correctivas |
| Administración | `/web/usuarios`, `/web/backups` | Empresa, usuarios, roles, auditoría y backups |

## Acceso y usuarios

1. Abrir `/web/login`.
2. Introducir usuario y contraseña.
3. La sesión redirige al panel principal.
4. Cerrar sesión desde el botón de la barra superior; el cierre se realiza por `POST` y está protegido por CSRF.

El sistema reconoce roles `ADMIN`/`ADMINISTRADOR`, `CONTABLE`, `GESTOR`, `VENDEDOR` y `USUARIO`. Los módulos de administración y fiscal exigen rol administrador o contable respectivamente.


## Flujo comercial recomendado

### Presupuesto a pedido

1. Crear o seleccionar el cliente.
2. Crear un presupuesto con artículos, cantidades, precios, descuentos e IVA.
3. Revisar totales.
4. Aprobar el presupuesto.
5. Crear el pedido de venta correspondiente.

### Pedido a albarán

1. Crear el pedido con sus líneas.
2. Confirmar el pedido.
3. Generar o preparar el albarán.
4. Comprobar cliente, almacén, cantidades y precios.
5. Marcar el albarán como entregado.

Al entregar, el servicio registra salidas de stock y actualiza el stock por almacén (`ArticuloAlmacen`).

### Albarán a factura

1. Abrir el albarán entregado o pendiente de facturar.
2. Seleccionar la acción de facturar.
3. Revisar fecha, vencimiento, medio de cobro y observaciones.
4. Revisar la factura en borrador.
5. Emitirla cuando los datos sean definitivos.

No se debe editar directamente una factura emitida. Para corregirla usar anulación o factura rectificativa.

## Compras y recepciones

1. Crear el proveedor.
2. Crear el pedido de compra.
3. Registrar la recepción y sus líneas.
4. Confirmar la recepción para registrar entradas de stock.
5. Registrar la factura de compra.

## Producción

### Recetas

Las recetas representan productos elaborados y sus ingredientes. Mantener cantidades, tiempos, temperatura, rendimiento y alérgenos actualizados.

### Órdenes

1. Crear una orden de producción con artículo o receta y cantidad prevista.
2. Iniciar la orden.
3. Registrar producción real y merma.
4. Finalizar la orden.

### Horneadas — costes reales

Al registrar una horneada se pueden indicar los costes reales de producción:

- **Coste mano de obra** (€)
- **Coste energía** (€)
- **Coste materiales** (€)

El campo **Coste unitario** se calcula automáticamente dividiendo la suma de costes entre la cantidad producida. Estos datos alimentan los informes de rentabilidad.

### Planificador

La pantalla `/web/planificador` resume pedidos, albaranes, órdenes de producción y necesidades de materia prima para una fecha concreta.

## Inventario y trazabilidad

- Cada entrada o salida de mercancía genera un movimiento de stock.
- El stock global del artículo (`Articulo.stock`) y el stock por almacén (`ArticuloAlmacen`) se mantienen sincronizados cuando el movimiento indica almacén.
- Los lotes permiten control de caducidad y trazabilidad completa.
- Las mermas registran pérdidas de producto con fecha y concepto.
- La pantalla `/web/reportes` muestra la valoración completa del inventario, movimientos filtrados por periodo y herramienta de ajuste manual.

## Tesorería y conciliación bancaria

### Extractos bancarios

Acceder a `/web/tesoreria/extractos` para:

- **Movimientos**: listar movimientos de un periodo con filtros de fecha
- **Importar CSV**: cargar un extracto bancario (formato: `fecha;concepto;importe`, primera línea cabecera)
- **Conciliar**: marcar movimientos como conciliados manualmente

### Conciliación automática asistida

Acceder a `/web/tesoreria/conciliacion` para:

1. Ver movimientos bancarios pendientes de conciliar.
2. Pulsar **Buscar** en un movimiento para ver facturas candidatas (misma cantidad ±0,01 € en ±30 días).
3. Aprobar la conciliación vinculando el movimiento a la factura o factura de compra.

## Reparto

1. Mantener vehículos (`/web/vehiculos`) y rutas maestras (`/web/rutas-reparto`).
2. Crear una hoja de ruta para la fecha.
3. Vincular albaranes pendientes.
4. Iniciar la ruta.
5. Confirmar entregas o registrar incidencias desde la app móvil (`/api/movil`).
6. Finalizar la ruta.

## Facturación y VeriFactu

El sistema incluye numeración de facturas, registros de alta y anulación, huella y encadenamiento SHA-256, firma con certificado X.509, QR, evidencias y cliente SOAP para AEAT.

La existencia de estas funciones no equivale a certificación legal. Antes del uso real validar certificado, XML, QR, firma, endpoint y respuestas en el entorno oficial de la AEAT (cambiar de `prewww2.aeat.es` a `www2.agenciatributaria.gob.es` y poner `VERIFACTU_AEAT_ENABLED=true`).

## Modelo 347

El módulo fiscal (`/web/modelo347`) permite generar y descargar información del Modelo 347 por ejercicio. Los resultados deben revisarlos la persona responsable de contabilidad o asesoría.

## Importaciones

La aplicación permite importar clientes, artículos y extractos bancarios CSV.

Antes de importar:

1. Crear un backup.
2. Probar con pocas filas.
3. Verificar codificación UTF-8 y formato de fecha/importe.
4. Revisar el resultado antes de procesar el fichero completo.

El importador CSV soporta campos entre comillas con punto y coma internos (e.g. `"García Hermanos, S.L."`).

## Backups

Desde `/web/backups` un administrador puede consultar copias disponibles y crear una copia manual. El servidor necesita `mysqldump`. La restauración debe realizarse con la aplicación detenida, sobre una copia de prueba primero y con verificación posterior.

## Informes

El área `/web/reportes` incluye: valoración de inventario, movimientos de stock, ventas mensuales, producción, rentabilidad por horneada (coste unitario real vs. PVP) y trazabilidad.

## Operación segura

- Usar cuentas nominales, no compartir el usuario `admin`.
- Reservar roles administrativos solo para mantenimiento.
- No modificar registros fiscales directamente en base de datos.
- Ejecutar backups diarios y probar restauración periódicamente.
- Revisar logs, auditoría y evidencias VeriFactu regularmente.
- Mantener certificados y secretos fuera del repositorio (`.env.production.local`).
- Usar HTTPS mediante proxy inverso o terminación TLS.

## Rutas útiles

| Ruta | Uso |
|------|-----|
| `/web/dashboard` | Panel principal |

| `/web/clientes` | Clientes |
| `/web/articulos` | Artículos |
| `/web/pedidos-venta` | Pedidos de venta |
| `/web/albaranes` | Albaranes |
| `/web/facturas` | Facturas |
| `/web/recepciones` | Recepciones de compra |
| `/web/ordenes-produccion` | Órdenes de producción |
| `/web/horneadas` | Horneadas |
| `/web/hojas-ruta` | Hojas de ruta de reparto |
| `/web/tesoreria` | Tesorería |
| `/web/tesoreria/extractos` | Extractos y conciliación bancaria |
| `/web/tesoreria/conciliacion` | Conciliación asistida |
| `/web/reportes` | Informes e inventario |
| `/web/contabilidad` | Contabilidad |
| `/web/contabilidad/balance` | Balance y cierre contable |
| `/web/verifactu` | VeriFactu |
| `/web/modelo347` | Modelo 347 |
| `/web/usuarios` | Gestión de usuarios |
| `/web/backups` | Backups |
| `/swagger-ui/index.html` | OpenAPI (solo administración) |

## Soporte y diagnóstico

```powershell
.\mvnw.cmd clean verify                       # build completo + tests
.\scripts\check-production-env.ps1            # validar entorno de producción
.\scripts\check-verifactu-production.ps1      # validar certificado VeriFactu
.\scripts\build-production.ps1                # compilar y empaquetar para producción
.\scripts\run-production.ps1                  # arrancar en producción
```
