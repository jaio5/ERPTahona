# Pipeline de revision y refactorizacion asistida por IA

Este documento divide la revision de ERP Tahona en fases pequenas y verificables. La idea es lanzar una IA por secciones, con prompts acotados, para reducir errores por exceso de contexto y evitar refactorizaciones que mezclen capas, dominios o riesgos legales.

## Contexto base de la aplicacion

- Stack: Java 17, Spring Boot 3.5.7, Thymeleaf, Spring MVC, Spring Data JPA, Hibernate, Flyway, MySQL 8, Maven, JUnit 5, Mockito, Bootstrap 5 y Alpine.js CSP.
- Superficie principal: `/web/**` con Thymeleaf y `/api/**` con controladores REST.
- Tamano aproximado actual: 290 clases Java, 99 plantillas HTML, 32 migraciones SQL y 52 tests Java.
- Dominios principales: clientes, proveedores, articulos, ventas, compras, facturacion, VeriFactu, almacen, produccion, reparto, tesoreria, contabilidad, auditoria, RGPD, backups y configuracion.
- Riesgos ya identificados en `docs/estado-actual.md`: permisos JSON no aplicados de forma completa, VeriFactu pendiente de certificacion real, pruebas MySQL/Testcontainers dependientes de Docker, restauracion de backup no probada, CRUD generico con riesgo de saltarse invariantes, rutas de backup, limites multipart y scripts de produccion.

## Reglas del pipeline

1. Cada fase debe producir un informe breve antes de modificar codigo.
2. No se mezclan fases: si se revisa ventas, no se refactoriza seguridad global salvo hallazgo bloqueante.
3. Cada cambio debe indicar archivos tocados, riesgo funcional, tests afectados y rollback posible.
4. Si la IA no puede ejecutar comandos, debe entregar los comandos exactos para que se ejecuten manualmente.
5. Ninguna fase se da por cerrada sin evidencias: tests, compilacion, capturas Playwright, diff revisado o checklist de no-cambio.
6. Las reglas legales/fiscales se tratan como invariantes: facturas emitidas, numeracion, anulaciones, rectificativas, huellas VeriFactu, auditoria y trazabilidad no deben simplificarse sin prueba especifica.
7. El criterio de salida de cada fase es "sin hallazgos bloqueantes abiertos" o "hallazgos documentados y aplazados explicitamente".

## Protocolo anti-perdida de contexto

Estas reglas son obligatorias para evitar que una IA se pierda o asuma demasiado:

1. Una sesion de IA no debe revisar mas de 8 archivos de produccion y 4 archivos de test salvo que la fase diga lo contrario.
2. Una sesion de IA no debe modificar mas de 3 archivos de produccion y 2 archivos de test.
3. Una sesion de IA no debe cubrir mas de un flujo de negocio completo.
4. Si el alcance necesita mas archivos, se parte en microfases antes de tocar codigo.
5. La IA debe listar los archivos que ha leido antes de emitir hallazgos.
6. La IA debe marcar como "no verificado" cualquier conclusion que dependa de archivos no leidos.
7. La IA no puede deducir comportamiento de una clase por el nombre; debe citar metodo, ruta, test o plantilla.
8. La IA no puede proponer eliminar codigo sin indicar el uso buscado con `rg` o prueba equivalente.
9. La IA no puede cambiar simultaneamente entidad, migracion, servicio, controlador y plantilla salvo en una correccion de bug totalmente trazada.
10. La IA debe detenerse y pedir una nueva microfase si encuentra dependencias cruzadas con otro dominio.
11. Las fases de auditoria son de solo lectura por defecto. Los cambios se hacen en fase 14 o en una microfase de correccion aprobada.
12. Cuando haya duda entre dos interpretaciones, la salida correcta es documentar la duda y proponer la verificacion, no asumir.

## Contrato de alcance por sesion

Antes de pegar cualquier prompt de fase, rellena este bloque. Si no se puede rellenar, la fase es demasiado grande.

```text
Contrato de alcance:
- Fase/microfase:
- Objetivo exacto:
- Archivos permitidos para leer:
- Archivos permitidos para modificar:
- Flujo que se revisa:
- Flujo fuera de alcance:
- Comandos permitidos:
- Criterio de salida:
- Puede modificar codigo: si/no
```

Ejemplo:

```text
Contrato de alcance:
- Fase/microfase: 5.4 Emitir factura
- Objetivo exacto: verificar que una factura borrador pasa a EMITIDA sin quedar editable y generando evento fiscal.
- Archivos permitidos para leer: FacturaService.java, FacturaWebController.java, FacturacionEventoService.java, Factura.java, EstadoFacturaEnum.java, facturas/ver.html, FacturaServiceLifecycleTest.java.
- Archivos permitidos para modificar: FacturaService.java, FacturaServiceLifecycleTest.java.
- Flujo que se revisa: POST /web/facturas/{id}/emitir.
- Flujo fuera de alcance: rectificativas, anulaciones, PDF, VeriFactu SOAP.
- Comandos permitidos: .\mvnw.cmd -Dtest=FacturaServiceLifecycleTest test
- Criterio de salida: hallazgos con archivo:linea y test de regresion si hay bug.
- Puede modificar codigo: no en primera pasada.
```

## Plantilla de microfase segura

Usa esta plantilla cuando una fase parezca amplia:

```text
Actua como revisor senior, pero trabaja con alcance estricto.

Contrato de alcance:
[pegar contrato aqui]

Reglas:
- No salgas del alcance.
- No asumas comportamiento de archivos no leidos.
- Primero lista archivos inspeccionados.
- Si necesitas otro archivo, deten la revision y pidelo como ampliacion de alcance.
- No hagas refactor estetico.
- No cambies comportamiento sin test.
- Si encuentras un bug fuera de alcance, registralo como "hallazgo externo" y no lo corrijas.

Tarea:
1. inspecciona solo los archivos permitidos,
2. describe el flujo real con metodos/rutas concretas,
3. lista invariantes que el flujo debe cumplir,
4. identifica bugs o riesgos con archivo:linea,
5. propone la minima verificacion,
6. no modifiques codigo salvo que "Puede modificar codigo" sea si.
```

## Gates para evitar cambios prematuros

Cada microfase debe pasar por estos estados:

| Estado | Permite cambios | Salida requerida |
| --- | --- | --- |
| A. Inventario | No | Archivos leidos, rutas/metodos, tests existentes |
| B. Hallazgos | No | Bugs con evidencia y severidad |
| C. Plan de correccion | No | Cambio minimo, archivos a tocar, tests |
| D. Implementacion | Si | Diff pequeno y test focalizado |
| E. Regresion | No | Test focalizado, compile/test general si aplica |

Si una IA intenta pasar de A a D sin B y C, se debe cortar la sesion y relanzar con una microfase mas pequena.

## Formato de respuesta exigido a la IA

Usar este formato en todas las fases:

```markdown
## Alcance revisado
- Archivos/carpetas inspeccionados:
- Flujos revisados:

## Hallazgos
- [ALTA/MEDIA/BAJA] archivo:linea - problema, impacto y propuesta.

## Cambios propuestos o realizados
- Archivo:
- Motivo:
- Riesgo:

## Verificacion
- Comandos ejecutados:
- Resultado:
- Tests nuevos/modificados:

## Pendientes
- Elementos que quedan fuera del alcance de esta fase:
```

## Fase 0 - Preparacion y linea base

**Objetivo:** congelar una foto inicial del proyecto antes de pedir cambios a una IA.

**Alcance:**
- `README.md`
- `docs/estado-actual.md`
- `docs/architecture.md`
- `pom.xml`
- `src/main/resources/application*.properties`
- `src/test/**`

**Prompt para la IA:**

```text
Actua como auditor tecnico senior de una aplicacion Spring Boot/Thymeleaf.

Proyecto: ERP Tahona.
Objetivo de esta fase: crear una linea base antes de refactorizar. No modifiques codigo.

Revisa:
- README.md
- docs/estado-actual.md
- docs/architecture.md
- pom.xml
- src/main/resources/application*.properties
- estructura de src/test

Entrega:
1. stack real detectado,
2. comandos de build/test recomendados,
3. riesgos tecnicos iniciales,
4. modulos de negocio detectados,
5. checklist de precondiciones antes de refactorizar.

No propongas refactors todavia. Solo inventario, riesgos y gates.
```

**Verificacion minima:**

```powershell
git status --short
.\mvnw.cmd test
```

**Gate de salida:** build de tests conocido, worktree controlado y lista de riesgos iniciales aceptada.

## Fase 1 - Arquitectura, dependencias y capas

**Objetivo:** comprobar que las capas estan separadas y que no hay dependencias innecesarias o peligrosas.

**Alcance:**
- `pom.xml`
- `src/main/java/alicanteweb/erp/config`
- `src/main/java/alicanteweb/erp/controller`
- `src/main/java/alicanteweb/erp/service`
- `src/main/java/alicanteweb/erp/repository`
- `src/main/java/alicanteweb/erp/entities`

**Prompt para la IA:**

```text
Actua como arquitecto Java/Spring. Revisa solo arquitectura y dependencias.

No cambies codigo en esta fase salvo que encuentres un fallo trivial y aislado.

Analiza:
- pom.xml
- config/
- controller/
- service/
- repository/
- entities/

Busca:
- logica de negocio en controladores,
- servicios que mezclen demasiados dominios,
- repositorios usados directamente desde vistas/controladores sin justificacion,
- dependencias Maven sin uso o con riesgo,
- duplicidad de patrones CRUD,
- puntos donde el CRUD generico pueda saltarse invariantes.

Entrega hallazgos con archivo:linea, impacto y propuesta. Clasifica cada propuesta como:
- corregir ahora,
- documentar para fase de dominio,
- no tocar.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -DskipTests compile
```

**Gate de salida:** mapa de capas validado y lista de refactors arquitectonicos separados por prioridad.

## Fase 2 - Modelo de datos, entidades y migraciones Flyway

**Objetivo:** revisar coherencia entre entidades JPA, migraciones SQL y restricciones reales de negocio.

**Alcance:**
- `src/main/java/alicanteweb/erp/entities`
- `src/main/java/alicanteweb/erp/repository`
- `src/main/resources/db/migration`
- tests de migracion en `src/test/java/**/migration`

**Prompt para la IA:**

```text
Actua como especialista JPA/Flyway/MySQL.

Revisa entidades, repositorios y migraciones. No cambies reglas de negocio sin justificarlo.

Comprueba:
- correspondencia entre entidades y columnas de migraciones,
- constraints, indices, unique keys y foreign keys,
- campos monetarios y precision BigDecimal,
- enums persistidos y compatibilidad con datos existentes,
- optimistic locking donde aplica,
- secuencias de documentos comerciales,
- migraciones idempotentes o peligrosas,
- orden y nombres de migraciones.

Entrega:
1. inconsistencias entidad-migracion,
2. riesgos de datos en produccion,
3. migraciones que requieren test MySQL real,
4. propuesta de tests o migraciones correctoras.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=FlywayMySqlMigrationTest test
.\mvnw.cmd test
```

**Gate de salida:** no hay diferencias criticas entre modelo JPA y esquema esperado, o quedan documentadas con plan de migracion.

## Fase 3 - Seguridad, autenticacion, autorizacion y permisos

**Objetivo:** cerrar riesgos de acceso y permisos antes de tocar flujos de negocio.

**Alcance:**
- `src/main/java/alicanteweb/erp/config/SecurityConfig.java`
- `src/main/java/alicanteweb/erp/config/PermisoEvaluador.java`
- `src/main/java/alicanteweb/erp/config/CspNonceFilter.java`
- `src/main/java/alicanteweb/erp/config/LoginRateLimitFilter.java`
- `src/main/java/alicanteweb/erp/service/UsuarioService.java`
- `src/main/java/alicanteweb/erp/service/RolService.java`
- controladores con `@PreAuthorize`
- `docs/security-and-authorization.md`

**Prompt para la IA:**

```text
Actua como revisor de seguridad Spring Security.

Objetivo: revisar autenticacion, roles, permisos por modulo, CSRF, CSP y superficies administrativas.

Analiza:
- SecurityConfig
- PermisoEvaluador
- UsuarioService y RolService
- controladores web/rest con @PreAuthorize
- formularios Thymeleaf con POST
- docs/security-and-authorization.md

Busca:
- endpoints sin autorizacion,
- permisos JSON definidos pero no aplicados,
- acciones crear/editar/eliminar/exportar protegidas solo a nivel de vista,
- CSRF ausente en formularios,
- rutas administrativas o fiscales accesibles por roles incorrectos,
- APIs REST sensibles sin control equivalente,
- problemas de sesion, logout, rate limit o CSP nonce.

Entrega hallazgos con severidad y propone tests Spring Security para cada riesgo alto.
No hagas cambios visuales ni de dominio.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=WebSecurityTest test
.\mvnw.cmd -Dtest=CspNonceFilterTest test
```

**Gate de salida:** endpoints criticos protegidos, permisos aplicados de forma verificable y tests de seguridad actualizados.

## Fase 4 - Frontend Thymeleaf, layout, Alpine.js CSP y navegacion

**Objetivo:** revisar que la UI renderiza sin errores, sin romper CSP, formularios ni navegacion.

**Alcance:**
- `src/main/resources/templates/layout.html`
- `src/main/resources/templates/fragments`
- `src/main/resources/templates/**/*.html`
- `src/main/resources/static/css/app.css`
- `src/main/resources/static/js/app.js`
- scripts Playwright: `test-erp-flows.mjs`, `scripts/test-ui.mjs`, `scripts/audit-app.mjs`

**Prompt para la IA:**

```text
Actua como revisor frontend de una aplicacion Thymeleaf + Bootstrap + Alpine.js CSP.

No conviertas la aplicacion a SPA. No introduzcas frameworks nuevos.

Revisa:
- layout.html y fragments
- plantillas por modulo
- app.css y app.js
- componentes Alpine.data
- formularios POST y tokens CSRF

Busca:
- directivas Alpine incompatibles con @alpinejs/csp,
- x-init inline, asignaciones directas o expresiones no CSP-safe,
- formularios sin csrf,
- links rotos entre /web/**,
- botones de accion que no correspondan a permisos,
- problemas de responsive/layout evidentes,
- duplicacion de markup que convenga extraer a fragmentos.

Entrega una lista de hallazgos por pantalla y prioriza solo cambios de bajo riesgo.
Si propones cambios, indica como verificarlos con Playwright.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd spring-boot:run
node scripts/audit-app.mjs
node test-erp-flows.mjs
```

**Gate de salida:** login, dashboard, navegacion principal y formularios criticos sin errores JS ni roturas de render.

## Fase 5 - Ventas y facturacion

**Objetivo:** revisar el flujo comercial mas critico: cliente -> presupuesto/pedido -> albaran -> factura -> emision/anulacion/rectificativa/PDF.

**Ejecucion:** no ejecutar esta fase completa en una sola sesion. Usar las microfases 5.x de la seccion "Microfases obligatorias".

**Alcance:**
- `Cliente*`
- `Presupuesto*`
- `PedidoVenta*`
- `Albaran*`
- `Factura*`
- `Documento*`
- plantillas `clientes`, `presupuestos`, `pedidos-venta`, `albaranes`, `facturas`, `pdf`
- tests relacionados en `src/test/java/**/service` y `controller`

**Prompt para la IA:**

```text
Actua como auditor funcional de ventas/facturacion en ERP.

Revisa solo el dominio de ventas:
- clientes,
- presupuestos,
- pedidos de venta,
- albaranes,
- facturas,
- PDFs,
- anulaciones y rectificativas.

Comprueba invariantes:
- numeracion correlativa,
- estados permitidos,
- factura emitida no editable indebidamente,
- anulacion/rectificativa trazable,
- calculo de bases, IVA y total,
- conversion desde albaranes,
- stock afectado donde corresponda,
- validaciones de NIF/CIF y datos obligatorios,
- auditoria de acciones fiscales.

Entrega:
1. bugs funcionales,
2. riesgos legales/fiscales,
3. tests unitarios o web que faltan,
4. propuesta de refactor solo si reduce riesgo.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=FacturaServiceTest,FacturaServiceLifecycleTest,FacturaValidacionServiceTest,AlbaranServiceTest,PresupuestoServiceTest,PedidoServiceTest test
node test-erp-flows.mjs
```

**Gate de salida:** el ciclo de venta principal queda probado y no se rompen calculos ni estados fiscales.

## Fase 6 - Compras, proveedores y recepciones

**Objetivo:** revisar entrada de mercancia, compras y su impacto en stock/contabilidad.

**Ejecucion:** no ejecutar esta fase completa en una sola sesion. Usar las microfases 6.x.

**Alcance:**
- `Proveedor*`
- `PedidoCompra*`
- `FacturaCompra*`
- `Recepcion*`
- plantillas `proveedores`, `pedidos-compra`, `facturas-compra`, `recepciones`

**Prompt para la IA:**

```text
Actua como auditor funcional de compras y recepciones.

Revisa:
- proveedores,
- pedidos de compra,
- facturas de compra,
- recepciones,
- entrada de stock,
- validaciones de importes y estados.

Busca:
- recepciones duplicables,
- cambios de estado inconsistentes,
- stock actualizado sin movimiento trazable,
- facturas de compra con totales incoherentes,
- formularios que permitan guardar datos incompletos,
- permisos de compras insuficientes.

Entrega hallazgos con propuesta de test para cada bug relevante.
No modifiques ventas ni facturacion de clientes en esta fase.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=PedidoCompraServiceTest,FacturaCompraUnitTest,CatalogoServicesCoverageTest test
```

**Gate de salida:** compras y recepciones no generan stock o importes inconsistentes.

## Fase 7 - Almacen, stock, lotes, trazabilidad, APPCC y mermas

**Objetivo:** asegurar que los movimientos fisicos son trazables y coherentes.

**Ejecucion:** no ejecutar esta fase completa en una sola sesion. Usar las microfases 7.x.

**Alcance:**
- `Articulo*`
- `Almacen*`
- `StockService`
- `MovimientoStock`
- `Lote*`
- `Merma*`
- `Appcc*`
- controladores REST de inventario/trazabilidad
- plantillas `articulos`, `almacenes`, `lotes`, `mermas`, `appcc`

**Prompt para la IA:**

```text
Actua como especialista en inventario, lotes y trazabilidad alimentaria.

Revisa:
- articulos,
- almacen por articulo,
- movimientos de stock,
- lotes e insumos,
- mermas,
- APPCC,
- endpoints REST de inventario y trazabilidad.

Comprueba:
- no hay stock negativo salvo regla explicita,
- cada entrada/salida genera movimiento trazable,
- lote permite trazabilidad adelante/atras,
- recepciones y produccion conectan con stock,
- mermas reducen stock correctamente,
- APPCC conserva informacion suficiente.

Entrega bugs por flujo y tests recomendados.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=StockServiceTest,ArticuloServiceTest test
```

**Gate de salida:** movimientos y trazabilidad explicables desde datos persistidos.

## Fase 8 - Produccion, recetas, ordenes y horneadas

**Objetivo:** revisar el ciclo productivo de la panaderia y sus costes reales.

**Ejecucion:** no ejecutar esta fase completa en una sola sesion. Usar las microfases 8.x.

**Alcance:**
- `Receta*`
- `OrdenProduccion*`
- `Horneada*`
- `ProduccionRestController`
- `PlanificadorRestController`
- plantillas `recetas`, `ordenes-produccion`, `horneadas`, `planificador`, `calendario`

**Prompt para la IA:**

```text
Actua como auditor de dominio de produccion.

Revisa:
- recetas e ingredientes,
- ordenes de produccion,
- inicio/finalizacion/cancelacion,
- horneadas y costes reales,
- consumo de materias primas,
- planificador y calendario.

Busca:
- estados imposibles,
- costes unitarios mal calculados,
- consumos de stock no trazados,
- merma no reflejada,
- planificador con datos agregados incorrectos,
- APIs REST que no validen cantidades.

Entrega hallazgos y pruebas recomendadas por estado del flujo.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=ProduccionServiceTest,TrazabilidadRepartoServiceTest test
```

**Gate de salida:** produccion conserva stock, costes y estados coherentes.

## Fase 9 - Reparto, rutas, hojas de ruta y API movil

**Objetivo:** validar la operativa de reparto y la API para repartidores.

**Ejecucion:** no ejecutar esta fase completa en una sola sesion. Usar las microfases 9.x.

**Alcance:**
- `Vehiculo*`
- `RutaReparto*`
- `HojaRuta*`
- `RepartoRestController`
- `RepartidorMovilController`
- plantillas `vehiculos`, `rutas`, `hojas-ruta`

**Prompt para la IA:**

```text
Actua como auditor de reparto y movilidad.

Revisa:
- vehiculos,
- rutas y paradas,
- hojas de ruta,
- entregas,
- incidencias,
- API movil /api/movil.

Comprueba:
- solo el repartidor correcto accede a su ruta,
- no se puede finalizar ruta con entregas inconsistentes salvo regla explicita,
- albaranes pendientes se vinculan una sola vez,
- incidencias quedan trazadas,
- permisos web y REST son equivalentes.

Entrega hallazgos y pruebas web/rest necesarias.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=RepartidorMovilControllerTest,TrazabilidadRepartoServiceTest test
```

**Gate de salida:** rutas y entregas mantienen trazabilidad y autorizacion correcta.

## Fase 10 - Tesoreria, bancos, conciliacion y contabilidad

**Objetivo:** revisar importes monetarios, conciliacion, asientos y cierre.

**Ejecucion:** no ejecutar esta fase completa en una sola sesion. Usar las microfases 10.x.

**Alcance:**
- `MovimientoCaja*`
- `MovimientoBanco*`
- `IntegracionBancariaService`
- `AsientoContable*`
- `PlanContable*`
- `Contabilidad*`
- `CierreContableRestController`
- `ExtractoBancarioRestController`
- plantillas `tesoreria`, `contabilidad`

**Prompt para la IA:**

```text
Actua como auditor de tesoreria y contabilidad operativa.

Revisa:
- caja,
- bancos,
- importacion CSV de extractos,
- conciliacion,
- plan contable,
- asientos automaticos/manuales,
- balance y cierre.

Busca:
- importes con signo incorrecto,
- redondeos peligrosos,
- conciliaciones duplicadas,
- asientos descuadrados,
- cierre ejecutable sin permisos fuertes,
- CSV parsing fragil,
- datos financieros exportables sin autorizacion.

Entrega hallazgos, impacto contable y tests recomendados.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=MovimientoCajaServiceTest,ContabilidadServiceTest,AsientoAutomaticoServiceTest test
```

**Gate de salida:** no hay asientos descuadrados ni conciliaciones duplicables sin control.

## Fase 11 - Fiscal, VeriFactu, Modelo 347, RGPD, auditoria y backups

**Objetivo:** revisar las areas de mayor impacto legal y operacional.

**Ejecucion:** no ejecutar esta fase completa en una sola sesion. Usar las microfases 11.x.

**Alcance:**
- `Verifactu*`
- `FiscalService`
- `Modelo347Service`
- `Rgpd*`
- `Auditoria*`
- `Backup*`
- `EmpresaConfig*`
- plantillas `fiscal`, `auditoria`, `empresa`
- `docs/verifactu.md`
- `docs/production-checklist.md`

**Prompt para la IA:**

```text
Actua como auditor tecnico de cumplimiento fiscal, RGPD y operacion.

Revisa:
- VeriFactu,
- Modelo 347,
- RGPD,
- auditoria,
- backups,
- configuracion de empresa.

Comprueba:
- huella/hash y encadenamiento,
- evidencias y eventos de facturacion,
- firma/certificado configurados de forma segura,
- endpoints de reenvio/verificacion protegidos,
- datos personales exportables auditados,
- backup con rutas confinadas,
- restauracion documentada o marcada como pendiente,
- secretos fuera de Git.

No certifiques cumplimiento legal. Distingue entre soporte tecnico y validacion oficial pendiente.
Entrega hallazgos con riesgo legal/operativo y tests propuestos.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd -Dtest=VerifactuEvidenceServiceTest,VerifactuAeatSoapClientTest,EmpresaConfigServiceVerifactuLegalTest,RgpdSolicitudServiceTest,RgpdConsentimientoServiceTest,RgpdAccesoDatosServiceTest,AuditoriaServiceTest,BackupServiceTest test
.\scripts\check-production-readiness.ps1
```

**Gate de salida:** riesgos legales diferenciados entre bug tecnico, configuracion pendiente y certificacion externa pendiente.

## Fase 12 - Pruebas automatizadas, cobertura y regresion

**Objetivo:** asegurar que las fases anteriores dejaron una red de pruebas suficiente.

**Alcance:**
- `src/test/java`
- `src/test/resources`
- `pom.xml`
- scripts Playwright
- reportes JaCoCo en `target/site/jacoco`

**Prompt para la IA:**

```text
Actua como responsable de calidad y testing.

Revisa la suite de tests completa.

Objetivo:
- detectar modulos criticos sin tests,
- proponer tests de regresion por bug encontrado en fases anteriores,
- revisar duplicidad o fragilidad,
- mantener o subir el gate JaCoCo sin tests superficiales.

Analiza:
- tests unitarios de servicios,
- tests web/controladores,
- tests de seguridad,
- tests REST,
- Playwright/scripts UI,
- configuracion H2/test.

Entrega:
1. matriz modulo -> tests existentes,
2. huecos criticos,
3. tests nuevos propuestos,
4. comandos por grupo de regresion,
5. riesgos que requieren MySQL real/Testcontainers.
```

**Verificacion minima:**

```powershell
.\mvnw.cmd clean verify
```

**Gate de salida:** cobertura minima mantenida y los flujos criticos tienen test de regresion.

## Fase 13 - Produccion, despliegue, Docker y scripts

**Objetivo:** validar que el empaquetado y el entorno de produccion son reproducibles.

**Alcance:**
- `Dockerfile`
- `docker-compose.yml`
- `.env.production.example`
- `scripts/*.ps1`
- `docs/production-deployment.md`
- `docs/operations.md`
- `docs/configuration.md`

**Prompt para la IA:**

```text
Actua como revisor DevOps/produccion.

Revisa:
- Dockerfile,
- docker-compose.yml,
- scripts de build/run/check,
- application-prod.properties,
- documentacion de despliegue y operacion.

Busca:
- variables obligatorias no documentadas,
- secretos que podrian filtrarse,
- fallback H2 activo en produccion,
- ddl-auto inseguro,
- checks de produccion obsoletos,
- rutas de backup/logs no preparadas,
- incompatibilidades Java/Maven,
- pasos de restauracion ausentes.

Entrega un checklist de produccion ejecutable y cambios minimos recomendados.
```

**Verificacion minima:**

```powershell
.\scripts\check-production-env.ps1
.\scripts\build-production.ps1
.\scripts\package-production.ps1
```

**Gate de salida:** build productivo reproducible y checklist de despliegue consistente.

## Fase 14 - Refactor controlado por paquetes

**Objetivo:** aplicar refactors pequenos despues de conocer riesgos, nunca antes.

**Regla:** cada refactor debe afectar a un solo paquete o dominio, con tests antes/despues.

**Prompt para la IA:**

```text
Actua como ingeniero senior Java. Vas a refactorizar solo el alcance indicado.

Alcance exacto:
[pegar aqui paquete, clases o modulo]

Restricciones:
- no cambies comportamiento funcional,
- no cambies esquema de base de datos salvo aprobacion explicita,
- no cambies HTML si el alcance es backend,
- no cambies seguridad global salvo que el alcance sea seguridad,
- conserva APIs publicas y rutas existentes,
- anade o ajusta tests si el refactor toca logica.

Tarea:
1. explica el problema concreto,
2. propone el cambio minimo,
3. aplica el cambio,
4. ejecuta tests del modulo,
5. resume diff y riesgos residuales.
```

**Verificacion minima segun tipo de refactor:**

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd -Dtest=NombreDelTest test
.\mvnw.cmd test
```

**Gate de salida:** diff pequeno, tests verdes y comportamiento externo conservado.

## Fase 15 - Auditoria final de regresion funcional

**Objetivo:** verificar la aplicacion completa despues de los cambios.

**Prompt para la IA:**

```text
Actua como auditor final de regresion.

No propongas nuevos refactors salvo bugs bloqueantes.

Revisa el estado final del proyecto:
- git diff,
- tests ejecutados,
- documentacion actualizada,
- riesgos pendientes,
- flujos principales web.

Entrega:
1. resumen de cambios por dominio,
2. riesgos cerrados,
3. riesgos pendientes,
4. comandos ejecutados y resultados,
5. recomendacion go/no-go para pasar a pruebas manuales o produccion.
```

**Verificacion minima:**

```powershell
git status --short
git diff --stat
.\mvnw.cmd clean verify
node test-erp-flows.mjs
```

**Gate de salida:** no hay regresiones conocidas en build, tests ni flujos principales.

## Microfases obligatorias

Estas microfases son la unidad recomendada de trabajo. La fase grande sirve para orientacion, pero la IA debe ejecutar una microfase concreta.

### Microfases 1.x - Arquitectura y capas

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 1.1 Dependencias Maven | `pom.xml` | detectar dependencias inutiles, duplicadas o peligrosas | `.\mvnw.cmd -DskipTests compile` |
| 1.2 Configuracion Spring | `config/*Config.java`, `application*.properties` | revisar perfiles, beans, filtros y propiedades | compile |
| 1.3 Controladores web base | `BaseWebController`, `WebController`, `WebUiController`, `LegacyRedirectController`, `BreadcrumbBuilder` | revisar convenciones web comunes | tests web existentes |
| 1.4 CRUD generico REST | `WebEntityController`, `WebChildEntityController`, DTOs relacionados | listar entidades que no deberian pasar por CRUD generico | tests de controller |
| 1.5 Repositorios compartidos | maximo 8 repositorios de un dominio | buscar queries peligrosas o sin transaccion | test focalizado |

### Microfases 2.x - Datos y migraciones

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 2.1 Orden Flyway | solo nombres de `db/migration/*.sql` | revisar numeracion, duplicados y saltos | inspeccion |
| 2.2 Migraciones base | V0-V3 | clientes, proveedores, articulos, facturacion base | Flyway test |
| 2.3 Migraciones fiscales | V4-V9 | VeriFactu, eventos, roles, hardening | Flyway test |
| 2.4 Migraciones produccion/reparto | V10-V18 | produccion, reparto, APPCC, mermas, recepciones | Flyway test |
| 2.5 Migraciones ventas/stock | V19-V31 | albaranes, presupuestos, pedidos, stock, unlock | Flyway test |
| 2.6 Entidad contra tabla | 1 entidad + migracion relacionada | verificar columnas, indices, enums y precision | test entidad/repositorio |

### Microfases 3.x - Seguridad

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 3.1 Login/logout/sesion | `SecurityConfig`, `LoginRateLimitFilter`, `WebController` login | validar autenticacion, logout y rate limit | `WebSecurityTest` |
| 3.2 CSRF formularios | 1 carpeta de templates + controlador asociado | confirmar tokens CSRF en POST | test web |
| 3.3 Permisos por modulo | 1 controlador web + `PermisoEvaluador` | comprobar `ver/crear/editar/eliminar/exportar` | test security |
| 3.4 APIs REST sensibles | 1 controlador REST | revisar autorizacion equivalente a web | test MVC |
| 3.5 Usuarios y roles | `UsuarioService`, `RolService`, templates usuarios | altas, bloqueos, passwords, permisos | `UsuarioServiceTest` |
| 3.6 CSP y scripts | `CspNonceFilter`, `layout.html`, 1 plantilla dinamica | nonces y ausencia de scripts inline peligrosos | `CspNonceFilterTest` |

### Microfases 4.x - Frontend

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 4.1 Layout y menu | `layout.html`, `fragments/menu.html`, `app.css` | navegacion, permisos visibles, responsive base | Playwright navegacion |
| 4.2 Login y dashboard | `login.html`, `dashboard.html`, `DashboardRestController` | render inicial, Alpine, errores JS | captura/Playwright |
| 4.3 Una pantalla CRUD | 1 carpeta de templates + controlador | lista/nuevo/ver/editar sin enlaces rotos | test web |
| 4.4 Un componente Alpine | 1 plantilla con `Alpine.data` | compatibilidad CSP estricta | Playwright sin JS errors |
| 4.5 Formularios con lineas | factura/albaran/pedido, solo uno | alta dinamica de lineas, totales, submits | Playwright focalizado |
| 4.6 PDFs/templates imprimibles | 1 template `pdf/*` + servicio PDF | render y datos obligatorios | test servicio/manual |

### Microfases 5.x - Ventas y facturacion

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 5.1 Cliente alta/edicion | `Cliente`, repo, service, web controller, templates clientes | NIF/CIF, validaciones, exportacion | `ClienteService`/controller test |
| 5.2 Articulo para venta | `Articulo`, stock basico, templates articulos | precio, IVA, activo/inactivo | `ArticuloServiceTest` |
| 5.3 Presupuesto | `Presupuesto*`, templates presupuestos | crear, editar, aprobar, totales | `PresupuestoServiceTest` |
| 5.4 Pedido venta | `PedidoVenta*`, templates pedidos-venta | crear, confirmar, lineas, estados | test focalizado |
| 5.5 Albaran borrador | `Albaran*`, templates albaranes | numeracion, lineas, totales | `AlbaranServiceTest` |
| 5.6 Entregar/facturar albaran | `AlbaranService`, `FacturaAlbaranService`, controlador | estado entregado y conversion a factura | test servicio |
| 5.7 Factura borrador | `Factura*`, templates facturas/formulario | crear/editar borrador, IVA y totales | `FacturaServiceTest` |
| 5.8 Emitir factura | `FacturaService`, `FacturacionEventoService`, controlador emitir | BORRADOR -> EMITIDA, bloqueo edicion, evento | `FacturaServiceLifecycleTest` |
| 5.9 Anular factura | `FacturaService`, controlador anular | estado, auditoria, no borrado fisico | test lifecycle |
| 5.10 Rectificativa | factura rectificativa template + service | relacion con factura original y signos | test fiscal |
| 5.11 PDF factura/albaran | templates pdf + `DocumentoService`/impresion | datos legales y formato | test/manual |
| 5.12 Flujo Playwright venta | `test-erp-flows.mjs` solo venta | cliente -> articulo -> albaran -> factura | Playwright |

### Microfases 6.x - Compras

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 6.1 Proveedores | `Proveedor*`, templates proveedores | alta, edicion, datos fiscales | test catalogo |
| 6.2 Pedido compra | `PedidoCompra*`, templates pedidos-compra | estados, lineas, numeracion | `PedidoCompraServiceTest` |
| 6.3 Factura compra | `FacturaCompra*`, templates facturas-compra | importes, lineas, proveedor | `FacturaCompraUnitTest` |
| 6.4 Recepcion | `Recepcion*`, templates recepciones | confirmar recepcion y evitar duplicados | test recepcion |
| 6.5 Compra afecta stock | `RecepcionService`, `StockService`, repos movimiento | entrada stock trazable | `StockServiceTest` |

### Microfases 7.x - Almacen y trazabilidad

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 7.1 Almacenes | `Almacen*`, templates almacenes | CRUD y permisos | test web |
| 7.2 Stock por almacen | `ArticuloAlmacen`, `StockService`, repos | entradas/salidas y no negativos | `StockServiceTest` |
| 7.3 Movimientos stock | `MovimientoStock`, service que lo crea | trazabilidad de cada cambio | test servicio |
| 7.4 Lotes | `Lote*`, `TrazabilidadRestController` | trazabilidad adelante/atras | test REST |
| 7.5 Mermas | `Merma*`, templates mermas | descuento stock y motivo | test servicio |
| 7.6 APPCC | `Appcc*`, templates appcc | controles y conservacion de datos | test web |

### Microfases 8.x - Produccion

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 8.1 Recetas | `Receta*`, templates recetas | ingredientes, cantidades, coste teorico | test receta |
| 8.2 Orden planificada | `OrdenProduccion*`, template orden formulario | crear orden valida | `ProduccionServiceTest` |
| 8.3 Iniciar/finalizar orden | service + controlador acciones | estados y cantidades finales | test estados |
| 8.4 Consumo materias | orden + stock + lote si aplica | salida stock trazable | `StockServiceTest` |
| 8.5 Horneadas | `Horneada*`, templates horneadas | costes mano obra/energia/materiales | test horneada |
| 8.6 Planificador/calendario | `PlanificadorRestController`, `CalendarioRestController`, templates | agregados y necesidades | test REST/Playwright |

### Microfases 9.x - Reparto

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 9.1 Vehiculos | `Vehiculo*`, templates vehiculos | CRUD y estado | test web |
| 9.2 Rutas | `RutaReparto*`, templates rutas | paradas y orden | test servicio |
| 9.3 Hoja de ruta | `HojaRuta*`, templates hojas-ruta | generar y vincular albaranes | test servicio |
| 9.4 Entrega/incidencia | `HojaRutaEntrega`, controllers reparto | confirmar, incidencia, trazabilidad | test REST |
| 9.5 API movil | `RepartidorMovilController` | autorizacion y datos del repartidor | `RepartidorMovilControllerTest` |

### Microfases 10.x - Tesoreria y contabilidad

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 10.1 Caja | `MovimientoCaja*`, `TesoreriaWebController` | ingresos/gastos y signos | `MovimientoCajaServiceTest` |
| 10.2 Importacion banco | `IntegracionBancariaService`, `ExtractoBancarioRestController` | CSV robusto y duplicados | test importacion |
| 10.3 Conciliacion | movimiento banco + candidatos | no duplicar conciliaciones | test servicio |
| 10.4 Plan contable | `PlanContable*`, templates contabilidad | cuentas y validaciones | test servicio |
| 10.5 Asientos | `AsientoContable*`, `AsientoAutomaticoService` | debe/haber y permisos | `AsientoAutomaticoServiceTest` |
| 10.6 Balance/cierre | `CierreContableRestController`, reportes | cierre autorizado y consistente | test REST |

### Microfases 11.x - Fiscal, legal y operacion

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 11.1 Empresa config | `EmpresaConfig*`, template empresa | datos fiscales y VeriFactu settings | `EmpresaConfigServiceVerifactuLegalTest` |
| 11.2 Evidencia VeriFactu | `VerifactuEvidence*`, `HashUtils` | hash, encadenamiento, persistencia | `VerifactuEvidenceServiceTest` |
| 11.3 Cliente AEAT SOAP | `VerifactuAeatSoapClient`, properties | endpoints, certificado, errores | `VerifactuAeatSoapClientTest` |
| 11.4 Pantallas VeriFactu | `FiscalWebController`, templates fiscal/verifactu* | permisos, reenvio, verificar | test web/security |
| 11.5 Modelo 347 | `Modelo347Service`, fiscal controller/template | agregacion y descarga | test servicio |
| 11.6 RGPD consentimientos | `RgpdConsentimiento*` | consentimiento y auditoria | `RgpdConsentimientoServiceTest` |
| 11.7 RGPD solicitudes/accesos | `RgpdSolicitud*`, `RgpdAccesoDatos*` | exportacion y trazabilidad | tests RGPD |
| 11.8 Auditoria | `AuditoriaService`, `AuditoriaWebController` | registrar acciones relevantes | `AuditoriaServiceTest` |
| 11.9 Backup crear | `BackupService`, `BackupWebController` | rutas, permisos, secretos | `BackupServiceTest` |
| 11.10 Backup restaurar | docs/scripts, no codigo salvo aprobacion | procedimiento de restore probado o pendiente | checklist manual |

### Microfases 12.x - Testing

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 12.1 Mapa tests por modulo | `src/test/java` listado | matriz modulo -> tests | informe |
| 12.2 Tests seguridad | tests security/controller | cubrir rutas criticas | `WebSecurityTest` |
| 12.3 Tests dominio critico | 1 dominio | aumentar regresion real | test focalizado |
| 12.4 Tests UI Playwright | 1 flujo | login y flujo funcional | script Playwright |
| 12.5 JaCoCo | `pom.xml`, reporte | evitar bajar gate | `.\mvnw.cmd clean verify` |

### Microfases 13.x - Produccion

| Microfase | Alcance maximo | Objetivo | Test/gate |
| --- | --- | --- | --- |
| 13.1 Variables prod | `.env.production.example`, `application-prod.properties` | obligatorias y seguras | check env |
| 13.2 Scripts check | `scripts/check-*.ps1` | nombres de variables y validaciones | script |
| 13.3 Build/package | build/package scripts, Dockerfile | reproducibilidad | build |
| 13.4 Docker compose | `docker-compose.yml`, docs deploy | MySQL, volumenes, secretos | revision |
| 13.5 Operacion | `docs/operations.md`, checklist | backups, restore, logs | checklist |

## Prompts cortos reutilizables

### Prompt de inspeccion sin cambios

```text
Ejecuta solo inspeccion. No modifiques archivos.

Contrato de alcance:
[pegar contrato]

Entrega:
- archivos leidos,
- flujo real observado,
- invariantes,
- hallazgos con archivo:linea,
- archivos que faltaria leer en otra microfase,
- tests existentes relacionados.

Marca como "no verificado" cualquier cosa que no puedas demostrar con los archivos leidos.
```

### Prompt de correccion minima

```text
Corrige solo el bug aprobado.

Bug aprobado:
[pegar hallazgo exacto con archivo:linea]

Contrato de alcance:
[pegar contrato con Puede modificar codigo: si]

Reglas:
- modifica el minimo numero de lineas,
- no hagas refactor colateral,
- anade o ajusta un test que falle sin el cambio,
- ejecuta solo el test focalizado primero,
- resume el diff y el riesgo residual.
```

### Prompt de cierre de microfase

```text
Cierra la microfase.

Resume:
- que se reviso,
- que se cambio,
- que pruebas pasaron,
- que queda fuera del alcance,
- que microfase deberia seguir.

No abras nuevos temas salvo hallazgos bloqueantes.
```

## Matriz rapida de fases por riesgo

| Riesgo | Fase principal | Evidencia minima |
| --- | --- | --- |
| Permisos JSON no aplicados | Fase 3 | Tests Spring Security y revision `@PreAuthorize` |
| VeriFactu no certificado | Fase 11 | Informe separado: soporte tecnico vs certificacion oficial |
| Migraciones MySQL no probadas | Fase 2 | Test Flyway/MySQL o pendiente explicito por Docker |
| Backup sin restauracion probada | Fase 11/13 | Procedimiento de restore documentado y prueba manual |
| CRUD generico saltando invariantes | Fase 1/2/5 | Inventario de entidades permitidas en CRUD generico |
| Alpine.js CSP | Fase 4 | Playwright sin errores JS y revision de directivas |
| Facturacion fiscal | Fase 5/11 | Tests de estados, numeracion, anulacion, rectificativa y huella |
| Stock y trazabilidad | Fase 7/8 | Tests de movimientos, lotes, consumos y mermas |
| Produccion y costes | Fase 8 | Tests de estados, consumo y coste unitario |
| Despliegue prod | Fase 13 | Scripts de check/build/package ejecutados |

## Orden recomendado de ejecucion

1. Fase 0: linea base.
2. Microfases 1.x a 3.x: arquitectura, datos y seguridad, una por sesion.
3. Microfases 4.x: frontend y navegacion, una pantalla o componente por sesion.
4. Microfases 5.x a 11.x: dominios de negocio, un flujo por sesion.
5. Microfases 12.x: cobertura y regresion.
6. Microfases 13.x: produccion.
7. Fase 14: refactors controlados, uno por uno, solo despues de una microfase de inspeccion.
8. Fase 15: auditoria final.

Orden sugerido para reducir riesgo:

```text
0
1.1 -> 1.2 -> 1.4
2.1 -> 2.6 por entidad critica
3.1 -> 3.3 por modulo -> 3.4 por API sensible
4.1 -> 4.2 -> 4.3 por pantalla
5.1 -> 5.2 -> 5.5 -> 5.7 -> 5.8 -> 5.9 -> 5.10
6.1 -> 6.2 -> 6.3 -> 6.4 -> 6.5
7.2 -> 7.3 -> 7.4 -> 7.5
8.1 -> 8.2 -> 8.3 -> 8.4 -> 8.5
9.3 -> 9.4 -> 9.5
10.1 -> 10.2 -> 10.3 -> 10.5 -> 10.6
11.1 -> 11.2 -> 11.3 -> 11.4 -> 11.8 -> 11.9 -> 11.10
12.x
13.x
15
```

## Prompt maestro para abrir una sesion nueva

Usa este prompt al iniciar una IA nueva antes de pegar la microfase concreta:

```text
Trabajas sobre ERP Tahona, una aplicacion Spring Boot 3.5.7 + Thymeleaf + JPA + Flyway + MySQL para gestion de una panaderia.

Reglas:
- trabaja solo en la microfase indicada,
- no ejecutes una fase completa si existe microfase,
- primero inspecciona archivos y entrega hallazgos,
- lista los archivos leidos antes de razonar,
- no asumas comportamiento de archivos no leidos,
- marca como "no verificado" cualquier conclusion no demostrada,
- no mezcles dominios,
- no cambies reglas fiscales o de stock sin test,
- no introduzcas frameworks nuevos,
- conserva rutas existentes,
- todo cambio debe tener verificacion,
- no modifiques codigo si el contrato dice "Puede modificar codigo: no",
- responde con el formato: Alcance revisado, Hallazgos, Cambios, Verificacion, Pendientes.

Contrato de alcance:
[pegar contrato]

Microfase a ejecutar:
[pegar aqui una microfase de docs/pipeline-refactorizacion-ia.md]
```
