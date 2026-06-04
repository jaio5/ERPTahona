# Informe de validacion preproduccion - 2026-06-03

## Resultado ejecutivo

Estado actual: preproduccion. La aplicacion compila, levanta el contexto Spring, carga recursos JavaFX principales, responde por web y supera la suite automatizada. No esta apta para produccion real hasta completar configuracion de entorno, secretos, certificado VeriFactu, MySQL y pruebas manuales con datos de negocio.

## Pruebas ejecutadas

| Prueba | Resultado | Evidencia |
| --- | --- | --- |
| Suite completa Maven | OK | 77 tests, 0 fallos, 1 omitido |
| Arranque de contexto Spring | OK | `ErpApplicationTests` |
| Recursos web | OK | `WebSurfaceTest` |
| Recursos JavaFX | OK parcial | `FxmlLoaderSmokeTest`, `FxmlContractTest` omitido 1 |
| Arranque web dev H2 | OK | `GET /web/login` devuelve 200 |
| Check readiness produccion | FAIL | 13 OK / 10 fallos |
| Check variables produccion | FAIL | falta `SPRING_DATASOURCE_URL` |
| Check VeriFactu produccion | FAIL | falta `VERIFACTU_CERT_PATH` |

## Matriz de flujos funcionales

| Flujo | Cobertura automatica | Estado | Pendiente manual |
| --- | --- | --- | --- |
| Login y permisos | `AutenticacionServiceTest` | OK tecnico | Probar usuarios reales, roles y menus visibles |
| Empresa | Tests VeriFactu legal + validacion VAT implementada | OK tecnico | Validar CIF/VAT real, datos fiscales, RGSEAA |
| Clientes | Test datos externos + exportacion RGPD implementada | Parcial | Alta/edicion/baja, export CSV, export JSON RGPD |
| Articulos | Validaciones y UI smoke | Parcial | Alta con IVA, alergenos, stock inicial |
| Proveedores | UI smoke parcial | Parcial | Alta/edicion, datos fiscales, compras |
| Pedidos venta | Servicio cubierto indirectamente | Parcial | Pedido -> albaran -> factura |
| Albaranes | `AlbaranServiceTest`, lineas, PDF web | OK tecnico | Crear albaran real con lineas y descargar PDF |
| Facturas venta | Lifecycle, numeracion, fiscalidad, PDF web | OK tecnico | Emitir, comprobar QR/VeriFactu, anular/rectificar |
| Pedidos compra | `PedidoCompraServiceTest` | OK tecnico | Pedido a proveedor con lineas reales |
| Facturas compra | Unit test entidad + contabilidad | Parcial | Alta factura compra, pago, vencimiento |
| Caja/Tesoreria | `MovimientoCajaServiceTest` | OK tecnico | Cobros/pagos reales, arqueo, banco |
| Contabilidad | `ContabilidadServiceTest`, `AsientoAutomaticoServiceTest` | OK tecnico | Revisar plan contable y libro diario real |
| Recetas | `ProduccionServiceTest` | OK tecnico | Crear receta con ingredientes, coste y alergenos |
| Ordenes produccion | `ProduccionServiceTest` | OK tecnico | Planificar, iniciar y finalizar una orden real |
| Horneadas | `ProduccionServiceTest` | OK tecnico | Registrar temperaturas y resultado real |
| Lotes/trazabilidad | `TrazabilidadRepartoServiceTest` | OK tecnico | Trazabilidad adelante/atras con insumos reales |
| APPCC | UI/control servicio disponible | Parcial | Controles diarios, limites criticos, acciones correctivas |
| Vehiculos | `TrazabilidadRepartoServiceTest` | OK tecnico | Alta vehiculo real |
| Rutas y hojas | `TrazabilidadRepartoServiceTest` | OK tecnico | Generar ruta diaria y confirmar entregas |
| Devoluciones | `TrazabilidadRepartoServiceTest` | OK tecnico | Devolucion asociada a lote/factura/albaran |
| Informes | Plantillas disponibles | Parcial | Validar cifras contra datos reales |
| Importar datos | Servicio disponible | Parcial | Import CSV clientes/articulos con fichero de muestra |
| Busqueda global | Controlador corregido | Parcial | Buscar entidades reales tras carga de datos |
| Usuarios | UI disponible | Parcial | Crear usuarios nominales, cambio password, baja |
| Backups | Scripts y servicio disponibles | Bloqueado entorno | Instalar MySQL client, backup y restauracion real |
| Auditoria | Servicio y exportacion disponibles | Parcial | Verificar trazas de acciones reales y export |

## Validacion normativa

### Facturacion y sistemas informaticos de facturacion

Fuentes oficiales consultadas:

- BOE, RD 1007/2023 consolidado.
- BOE, Orden HAC/1177/2024.
- AEAT, FAQ Sistemas Informaticos de Facturacion y VERI*FACTU, actualizadas a 2025-12-05.
- AEAT, documentacion tecnica de servicios web VERI*FACTU.

Estado:

- La aplicacion tiene evidencias, eventos, hash/huella, QR, XML y firma si hay certificado.
- Falta validar con certificado real, endpoint AEAT real/preproduccion y esquemas vigentes.
- Falta declaracion responsable/certificacion del sistema antes de presentarlo como conforme.
- En produccion debe impedirse alterar facturas emitidas mediante operaciones directas no auditadas.

### RGPD/LOPDGDD

Fuente oficial consultada:

- AEPD, guia RGPD para responsables de tratamiento.

Estado:

- Hay auditoria, exportacion de datos de cliente, solicitudes RGPD, consentimientos y accesos.
- Falta procedimiento operativo: base juridica, clausulas, registro de actividades, usuarios nominales, retencion, copias y ejercicio de derechos.

### Seguridad alimentaria y APPCC

Estado:

- Existen recetas, alergenos, lotes, trazabilidad y APPCC.
- Falta parametrizar controles reales del obrador: temperaturas, limites criticos, responsables, frecuencia, registros y acciones correctivas.
- Falta validar etiquetado e informacion alimentaria con datos reales de articulos/recetas.

## Bloqueos para produccion

1. Configurar `.env.production.local`.
2. Activar `SPRING_PROFILES_ACTIVE=prod`.
3. Configurar MySQL 8 real y usuario no root.
4. Instalar MySQL client para backups/restauracion.
5. Definir secretos reales: `CIFRADO_AES_KEY`, `SECURITY_PBKDF2_SECRET`, `ADMIN_DEFAULT_PASSWORD`.
6. Configurar certificado VeriFactu real.
7. Ejecutar pruebas AEAT/VeriFactu con XML, QR, firma y envio.
8. Probar backup y restauracion en una base copia.
9. Consolidar cambios del repositorio antes de empaquetar.
10. Ejecutar ciclo completo con datos reales de prueba y guardar evidencias.

## Siguiente bloque de pruebas manuales

1. Arrancar en dev con base limpia.
2. Crear empresa y validar VAT/NIF.
3. Crear usuario nominal.
4. Crear cliente, proveedor, articulo, almacen.
5. Importar clientes/articulos desde CSV de muestra.
6. Crear pedido venta, albaran y factura.
7. Descargar PDF de albaran y factura.
8. Emitir/anular factura y revisar auditoria/eventos.
9. Crear receta, orden, horneada y lote.
10. Crear vehiculo, ruta, hoja y confirmar entrega.
11. Crear devolucion.
12. Probar informes y busqueda global.
13. Crear backup y restaurar en base copia.
