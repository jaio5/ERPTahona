# Auditoría técnica integral — ERP Tahona

**Fecha:** 18 de junio de 2026  
**Referencia:** estado local actual de la rama `main`, incluyendo cambios modificados y no versionados  
**Stack:** Java 17, Spring Boot 3.5.7, Spring MVC, Thymeleaf, Spring Data JPA, MySQL/H2 y Flyway

## 1. Resumen ejecutivo

La aplicación compila desde cero, genera el JAR y supera sus 83 pruebas automatizadas. La migración desde JavaFX hacia una aplicación web ha reducido una cantidad importante de código y ha añadido funcionalidades reales de inventario, recepciones, tarifas, mermas, calendario y planificación.

El estado actual todavía no debería considerarse preparado para producción con usuarios de distintos perfiles. Los principales riesgos están en autorización, protección CSRF, acceso genérico a entidades y consistencia de stock.

### Resultado global

| Área | Estado | Observación principal |
|---|---|---|
| Compilación y empaquetado | Correcto | `mvnw clean verify` finaliza con `BUILD SUCCESS` |
| Pruebas | Parcial | 83/83 pasan, pero cubren el 19,05 % de líneas y 8,15 % de ramas |
| Arquitectura | Mejorable | Capas reconocibles, aunque varios controladores y CRUD genéricos evitan servicios |
| Seguridad | Crítico | No existe autorización efectiva por módulo y toda `/api/**` ignora CSRF |
| Persistencia | Riesgo alto | Tests sin Flyway y varios flujos no protegen concurrencia |
| Inventario | Crítico | Un albarán puede descontar stock dos veces |
| Frontend | Mejorable | Dos superficies y recursos paralelos elevan duplicación y coste de mantenimiento |
| Operabilidad | Parcial | Hay perfiles, backups y scripts, pero faltan gates y validación real de migraciones |

### Prioridad inmediata

1. Cerrar el CRUD genérico y aplicar autorización por rol/módulo.
2. Reactivar CSRF para las APIs de sesión.
3. Corregir el ciclo de stock de albaranes y añadir pruebas de integración.
4. Definir el modelo de existencias por almacén y proteger actualizaciones concurrentes.
5. Ejecutar Flyway y pruebas de integración sobre MySQL en CI.

## 2. Alcance y validaciones realizadas

- Revisión estática de 277 clases Java, 98 plantillas HTML y 20 migraciones.
- Inspección de configuración, seguridad, controladores, servicios, repositorios, entidades, frontend y scripts.
- Compilación limpia mediante `.\mvnw.cmd clean verify`.
- Resultado: 83 pruebas, 0 fallos, 0 errores y 0 omitidas.
- Informe JaCoCo sobre 255 clases:
  - Líneas: 1.834 cubiertas de 9.627, **19,05 %**.
  - Ramas: 312 cubiertas de 3.830, **8,15 %**.
- Análisis de dependencias mediante `mvnw dependency:analyze`.
- No se realizó certificación legal, prueba contra AEAT, restauración real de backup ni prueba de migraciones sobre una instancia MySQL con datos existentes.

## 3. Hallazgos críticos

### C-01 — No existe autorización efectiva por rol o módulo

**Evidencia**

- `SecurityConfig.java:71-89` solo distingue recursos públicos de usuarios autenticados.
- No hay reglas `hasRole`, `hasAuthority`, `@PreAuthorize` ni seguridad de método.
- `SecurityConfig.java:211-223` construye autoridades, pero ninguna regla las consume.
- `docs/index.md:24` afirma que hay roles y permisos por módulo.

**Impacto**

Cualquier usuario autenticado puede acceder a usuarios, empresa, backups, auditoría, contabilidad, inventario, fiscalidad, VeriFactu y operaciones destructivas. La aplicación presenta roles en el modelo, pero no los hace cumplir.

**Recomendación**

- Definir una matriz de permisos por módulo y operación.
- Activar seguridad de método y aplicar permisos en servicios de negocio.
- Restringir explícitamente administración, fiscalidad, backups, usuarios, cierres y ajustes.
- Añadir pruebas negativas por rol, no solo pruebas de autenticación.

**Esfuerzo:** alto.

### C-02 — El CRUD genérico permite saltarse servicios, validaciones e invariantes

**Evidencia**

- `WebEntityController.java:26-63` publica entidades críticas como usuarios, asientos, auditoría, evidencias VeriFactu y configuración de empresa.
- `WebEntityController.java:104-182` crea, actualiza y elimina directamente mediante `EntityManager`.
- `WebEntityController.java:233-257` asigna por reflexión cualquier campo simple recibido.
- `WebChildEntityController.java` replica el mismo patrón para entidades hijas.
- Solo se excluyen tres nombres sensibles en `WebEntityController.java:296-298`.

**Impacto**

Un usuario autenticado puede modificar estados, importes, campos legales y relaciones sin pasar por validaciones, auditoría ni flujos transaccionales. También puede borrar registros que deberían ser inmutables o conservarse por trazabilidad.

**Recomendación**

- Retirar del CRUD genérico las entidades con reglas de negocio o valor legal.
- Sustituir mapas y reflexión por DTO explícitos y casos de uso dedicados.
- Mantener, como máximo, un CRUD común tipado para catálogos simples con allowlist de campos y permisos.
- Prohibir eliminación física de auditoría, facturas emitidas y evidencias.

**Esfuerzo:** alto.

### C-03 — CSRF está deshabilitado para todas las operaciones REST

**Evidencia**

- `SecurityConfig.java:105-110` ignora CSRF para `/api/**`.
- La API usa autenticación de sesión mediante cookie, no tokens independientes.
- `WebSecurityTest.java:59-67` prueba expresamente que un POST funciona sin CSRF.

**Impacto**

Una página externa puede provocar peticiones con la sesión activa del usuario y ejecutar ajustes de inventario, emisión/anulación, cambios de usuarios u otras mutaciones.

**Recomendación**

- Mantener CSRF habilitado para APIs consumidas desde el navegador con sesión.
- Exponer el token en metaetiquetas o cookie segura y añadirlo desde un único wrapper `fetch`.
- Si existe una futura API para clientes externos, separarla y protegerla con autenticación sin cookies.

**Esfuerzo:** medio.

### C-04 — El stock de un albarán puede descontarse dos veces

**Evidencia**

- `DocumentoService.java:174-176` descuenta stock al crear cada línea de un albarán nuevo.
- `AlbaranService.java:143-166` vuelve a descontarlo cuando el albarán se marca como entregado.
- No hay pruebas de `MovimientoStock` ni del ciclo completo crear-editar-entregar.
- Si falta stock al entregar, `AlbaranService.java:159-165` captura el error y mantiene el albarán como entregado.

**Impacto**

El inventario puede quedar por debajo del stock real. Además, un albarán puede figurar como entregado aunque uno o varios movimientos no se hayan registrado.

**Recomendación**

- Elegir un único punto de compromiso de stock: reserva al confirmar y salida al entregar, o salida únicamente al entregar.
- Modelar reservas separadas si son necesarias.
- Hacer atómica la transición: cualquier error debe revertir estado y movimientos.
- Reconciliar diferencias al editar o cancelar un documento.

**Esfuerzo:** medio-alto.

## 4. Hallazgos de severidad alta

### A-01 — El parámetro de almacén se ignora y el stock es global

**Evidencia**

- `StockService.java:26-50` y `StockService.java:54-71` reciben `almacenId`, pero no lo utilizan.
- `MovimientoStock.java:37-43` sí dispone de almacén origen y destino.
- `Articulo.java:70-71` almacena una única cantidad global.

**Impacto**

Las recepciones, producción, mermas y ventas no permiten saber dónde está el stock. Los informes por almacén devuelven datos incompletos y no pueden soportar traspasos fiables.

**Recomendación**

Crear una entidad de existencia `ArticuloAlmacen` con restricción única por artículo/almacén y usar movimientos con origen/destino. Mantener el total global como dato calculado, no como segunda fuente de verdad.

**Esfuerzo:** alto, con migración de datos.

### A-02 — Las actualizaciones de stock no están protegidas contra concurrencia

**Evidencia**

- `StockService.java:29-40` y `StockService.java:57-62` implementan lectura-modificación-escritura.
- `Articulo.java` no tiene `@Version`.
- No se usa bloqueo pesimista ni actualización condicional.

**Impacto**

Dos operaciones simultáneas pueden leer la misma cantidad y sobrescribirse, perdiendo entradas o salidas.

**Recomendación**

Usar bloqueo optimista con `@Version` y reintento controlado, o una actualización SQL atómica que valide stock suficiente.

**Esfuerzo:** medio.

### A-03 — La consulta temporal de movimientos de stock tiene tipos incompatibles

**Evidencia**

- `MovimientoStock.java:25-27` declara `fecha` como `LocalDate`.
- `MovimientoStockRepository.java:44` define `findByFechaBetween(LocalDateTime, LocalDateTime)`.
- `InventarioRestController.java:41-48` envía `LocalDateTime`.

**Impacto**

El informe de movimientos puede fallar al ejecutarse o depender de conversiones no portables del proveedor JPA.

**Recomendación**

Cambiar el repositorio para recibir `LocalDate`, o consultar `fechaCreacion` si se necesita precisión temporal. Añadir prueba de integración del endpoint.

**Esfuerzo:** bajo.

### A-04 — Operaciones de API se atribuyen al usuario `admin`

**Evidencia**

- `DocumentoRestController.java:133-150` busca al usuario `admin` para auditar conversiones.

**Impacto**

La trazabilidad no identifica al actor real. Esto invalida parte del valor de la auditoría y dificulta investigar errores o acciones indebidas.

**Recomendación**

Obtener el principal autenticado y resolver el usuario por su identificador. Centralizarlo en un `CurrentUserService`.

**Esfuerzo:** bajo.

### A-05 — La numeración de albaranes no es segura bajo concurrencia

**Evidencia**

- `AlbaranService.java:415-420` calcula `MAX + 1`.
- `AlbaranVentaService.java:52-63` duplica la misma estrategia.
- El comentario afirma que `MAX` evita duplicados, pero dos transacciones pueden obtener el mismo máximo.
- Facturas y pedidos de compra ya usan tablas de secuencia con bloqueo pesimista.

**Impacto**

Creaciones simultáneas pueden producir números duplicados y errores de persistencia o documentos inconsistentes.

**Recomendación**

Aplicar el patrón de `FacturaSerieSequence` a albaranes y eliminar implementaciones duplicadas.

**Esfuerzo:** medio.

### A-06 — Las migraciones Flyway no se prueban

**Evidencia**

- `src/test/resources/application.properties:8-12` usa `ddl-auto=create-drop` y deshabilita Flyway.
- `application-test.properties` contiene otra configuración distinta y no está activa en los logs.
- `V20__indexes_and_constraints.sql:36-41` añade restricciones únicas sin limpieza previa de duplicados.

**Impacto**

El contexto de test puede pasar aunque una instalación nueva o una actualización de producción falle. H2 tampoco valida completamente SQL específico de MySQL.

**Recomendación**

- Añadir Testcontainers con MySQL y Flyway habilitado.
- Probar base vacía y actualización desde una versión soportada.
- Incluir prechecks para datos duplicados antes de añadir constraints.
- Unificar la configuración de test.

**Esfuerzo:** medio.

### A-07 — Cobertura baja sin quality gate

**Evidencia**

- Cobertura global: 19,05 % de líneas y 8,15 % de ramas.
- Servicios: 20,8 %; controladores REST: 14,7 %; controladores web: 12,3 %.
- `pom.xml:311-328` genera informe JaCoCo pero no ejecuta `jacoco:check`.
- `qodana.yaml:35-45` mantiene comentados todos los límites.
- Componentes críticos con cobertura prácticamente nula: cliente SOAP AEAT, backups, documento, validación de factura, usuarios, importación y notificaciones.

**Impacto**

Cambios en facturación, inventario, seguridad o migraciones pueden introducir regresiones sin bloquear el build.

**Recomendación**

Empezar con gates modestos sobre código nuevo y elevarlos gradualmente. Priorizar pruebas de flujos e invariantes, no getters ni cobertura artificial.

**Esfuerzo:** continuo.

## 5. Hallazgos de severidad media

### M-01 — Validación de entrada inconsistente

Solo dos endpoints usan `@Valid`. El resto acepta entidades o `Map<String,Object>` y realiza conversiones manuales. El CRUD genérico no ejecuta Bean Validation antes de persistir.

**Recomendación:** DTO por operación, anotaciones Jakarta Validation y respuestas de error uniformes.

### M-02 — Cálculos monetarios duplicados

Descuentos, bases e IVA se recalculan en entidades, `DocumentoService`, `AlbaranService`, `FacturaService` y otros servicios. Existen distintos criterios sobre si `total` incluye IVA y cuándo redondear.

**Recomendación:** crear un componente de cálculo fiscal puro y ampliamente probado, con una política única de escala y redondeo.

### M-03 — Controladores acoplados directamente a repositorios

`RecepcionWebController.java:30-48`, `WebApiController.java:23-41` e `InventarioRestController.java:20-29` mezclan servicios y repositorios. Esto dispersa reglas, transacciones y autorización.

**Recomendación:** hacer que los controladores dependan de casos de uso o fachadas; reservar repositorios para la capa de aplicación/dominio.

### M-04 — Cargas completas y filtrado en memoria

`WebEntityController.java:79-91` carga hasta 500 entidades y luego filtra en memoria. Varios controladores web usan `findAll()` para catálogos y listados; se detectaron 50 usos directos en controladores.

**Recomendación:** paginación en base de datos, proyecciones DTO y búsquedas específicas con límites explícitos.

### M-05 — Exposición directa de entidades JPA en REST

Endpoints de inventario, extractos, producción, reparto y trazabilidad devuelven entidades. Con `open-in-view=false`, esto facilita errores por relaciones lazy, respuestas inestables y exposición accidental de campos.

**Recomendación:** DTO de salida inmutables y consultas de proyección.

### M-06 — Importación bancaria frágil

`ExtractoBancarioRestController.java:47-70`:

- Divide por coma o punto y coma, por lo que rompe importes con coma decimal y CSV con campos entrecomillados.
- Convierte importes monetarios mediante `double`.
- Consulta el banco en cada fila.
- Acepta banco inexistente y continúa con `null`.
- No aplica límites propios de filas, formato o tamaño.

**Recomendación:** parser CSV real, `BigDecimal` directo, carga previa del banco, validación de esquema y procesamiento transaccional por lotes.

### M-07 — No hay límites explícitos para archivos subidos

No se configuran `spring.servlet.multipart.max-file-size` ni `max-request-size`; importaciones y extractos dependen de valores implícitos.

**Recomendación:** fijar límites por entorno, validar tipo y tamaño antes de procesar y limitar filas.

### M-08 — Demasiadas capturas genéricas

Se detectaron 137 bloques `catch (Exception ...)`. Muchos controladores muestran `e.getMessage()` al usuario y continúan con redirecciones.

**Impacto:** errores de programación pueden presentarse como errores operativos, se pierde semántica y puede filtrarse información interna.

**Recomendación:** excepciones de dominio específicas, manejo centralizado y mensajes públicos separados del detalle de log.

### M-09 — Borradores con datos empresariales en `localStorage`

`static/app.js:275-289` guarda borradores completos por módulo en almacenamiento persistente del navegador.

**Impacto:** datos de clientes, precios o documentos permanecen en equipos compartidos y quedan accesibles a cualquier script ejecutado en el mismo origen.

**Recomendación:** guardar solo campos no sensibles, usar `sessionStorage` cuando sea suficiente, establecer caducidad y limpiar en logout.

### M-10 — Frontend duplicado y dos modelos de interacción

Existen `static/app.js` y `static/js/app.js`, junto con `static/app.css` y `static/css/app.css`, además de SPA y pantallas Thymeleaf tradicionales.

**Impacto:** dos wrappers, dos sistemas de formularios y estilos diferentes incrementan regresiones y trabajo duplicado.

**Recomendación:** elegir una superficie principal, compartir componentes y retirar recursos que ya no tengan consumidores.

### M-11 — H2 se empaqueta en producción

`pom.xml:105-110` afirma que H2 es solo de test, pero declara `runtime`. Por tanto entra en el JAR ejecutable.

**Recomendación:** usar scope `test` y separar cualquier fallback local en un perfil o módulo explícito.

### M-12 — Configuración de actualización Git habilitada por defecto

`application.properties:64-70` y `application-prod.properties:55-60` habilitan actualización desde Git salvo que se desactive externamente.

**Recomendación:** valor seguro por defecto `false`; habilitarlo solo en una instalación administrada, con autorización exclusiva y proceso de despliegue verificable.

### M-13 — El empaquetado Spring Boot se ejecuta dos veces

`pom.xml:289-309` añade una ejecución `repackage` aunque el parent de Spring Boot ya aporta una. El log de `verify` muestra `repackage (repackage)` y `repackage (default)`.

**Recomendación:** eliminar la ejecución redundante y conservar solo configuración del plugin.

### M-14 — Dependencias gestionadas manualmente sin una política clara

El POM fija versiones de MySQL, Hikari, Flyway, Guava y varias librerías de seguridad por encima del BOM de Spring Boot. Algunas propiedades no se usan (`mapstruct`, `dependency-check`) y el plugin OWASP no está configurado.

**Recomendación:** delegar en el BOM salvo excepción documentada, eliminar propiedades muertas y ejecutar análisis de vulnerabilidades en CI.

### M-15 — La restauración y eliminación de backups aceptan rutas arbitrarias

`BackupService.java:171-213` restaura cualquier ruta recibida y `BackupService.java:331-352` elimina cualquier archivo cuya ruta se entregue al método.

Actualmente no se observó un endpoint web de restauración/eliminación, pero el servicio no protege su propio límite de confianza.

**Recomendación:** resolver rutas canónicas y exigir que permanezcan dentro del directorio de backups.

## 6. Hallazgos de severidad baja

### B-01 — `node_modules` no está ignorado

El directorio aparece no versionado y `.gitignore` no contiene `node_modules/`.

### B-02 — Configuración de tests duplicada

Existen `application.properties` y `application-test.properties` con bases, usuarios y estrategias DDL diferentes. Los tests actuales no activan explícitamente `test`.

### B-03 — Pruebas de superficie demasiado superficiales

`WebSurfaceTest` verifica que existan algunos archivos y un controlador, pero no carga todas las plantillas ni comprueba contratos JavaScript/API.

### B-04 — Logging con símbolos dañados

La salida de tests contiene caracteres `?` en lugar de iconos. No afecta la lógica, pero reduce legibilidad y dificulta búsquedas.

### B-05 — Comentarios y documentación no siempre reflejan el comportamiento

Ejemplos: H2 descrito como dependencia de test, `MAX` descrito como seguro ante concurrencia y roles declarados como aplicados por módulo.

## 7. Aspectos positivos

- El proyecto compila limpiamente con Java 17 y el Maven Wrapper.
- `open-in-view=false` evita ocultar cargas perezosas durante el renderizado.
- Producción usa `ddl-auto=validate` y Flyway.
- Facturas y pedidos de compra ya tienen secuencias con bloqueo pesimista.
- Hay manejo centralizado de errores REST que evita devolver stack traces.
- Se usan `BigDecimal` en la mayor parte del dominio monetario.
- Los secretos de producción se externalizan mediante variables.
- La migración web ha eliminado una cantidad considerable de código JavaFX y ha simplificado el despliegue.
- Hay tests de ciclo de factura, contabilidad, producción, reparto, autenticación y evidencias VeriFactu.

## 8. Plan de mejora priorizado

### Fase 0 — Contención inmediata

1. Restringir temporalmente el acceso de la aplicación a usuarios administradores hasta implementar permisos.
2. Deshabilitar o retirar el CRUD genérico para entidades críticas.
3. Reactivar CSRF en `/api/**`.
4. Deshabilitar `app.update.enabled` por defecto en producción.

### Fase 1 — Integridad funcional

1. Corregir el momento único de descuento de stock.
2. Hacer atómicas las transiciones de albarán y recepción.
3. Arreglar la consulta de fechas de movimientos.
4. Proteger stock y numeración de albaranes frente a concurrencia.
5. Añadir pruebas de integración para estos flujos.

### Fase 2 — Seguridad y arquitectura

1. Implementar permisos por módulo y acción.
2. Sustituir mapas/reflexión por DTO y casos de uso.
3. Resolver el usuario autenticado de forma central.
4. Separar controladores, servicios de aplicación y repositorios.
5. Introducir eventos o auditoría transversal para operaciones sensibles.

### Fase 3 — Datos y calidad

1. Introducir existencias por almacén y migrar stock.
2. Probar Flyway sobre MySQL con Testcontainers.
3. Consolidar cálculo monetario.
4. Fijar gates de cobertura y análisis estático.
5. Añadir pruebas E2E de los flujos principales.

### Fase 4 — Simplificación

1. Consolidar el frontend y retirar recursos duplicados.
2. Limpiar dependencias y configuración Maven.
3. Unificar perfiles de test.
4. Actualizar documentación para que describa garantías reales.

## 9. Criterios de aceptación recomendados

- Un usuario sin permiso recibe 403 en UI y API para cada módulo restringido.
- Ningún POST/PUT/PATCH/DELETE con sesión funciona sin CSRF.
- Crear y entregar un albarán produce exactamente una salida de stock.
- Un fallo de stock revierte íntegramente el cambio de estado.
- Dos salidas concurrentes no pueden dejar stock negativo ni perder movimientos.
- Las existencias y movimientos identifican almacén.
- Todas las migraciones se aplican en CI sobre MySQL vacío y sobre una versión anterior.
- El build falla si no se alcanzan los gates acordados.
- Ninguna API pública devuelve entidades JPA directamente.
- Auditoría registra al principal real y no un usuario fijo.

## 10. Conclusión

El proyecto tiene una base funcional amplia y una migración web significativa, pero su principal deuda no es estética: está en los límites de seguridad y en la integridad de operaciones críticas. Aplicar Clean Code aquí significa primero hacer explícitos permisos, casos de uso, invariantes y transacciones; después reducir duplicación y mejorar estilo.

La aplicación puede seguir evolucionando sobre la arquitectura actual si se elimina el acceso genérico a entidades, se refuerza la capa de aplicación y se añade una red de pruebas de integración centrada en facturación, stock, migraciones y autorización.

## 11. Avance de remediación — 18 de junio de 2026

Aplicado en la primera iteración:

- CSRF reactivado para todas las APIs de sesión y pruebas negativas/positivas añadidas.
- Restricción de servidor para administración, fiscalidad, contabilidad y ajustes de inventario.
- Auditoría, evidencias VeriFactu y Modelo 347 convertidos en módulos de solo lectura dentro del CRUD genérico.
- Doble descuento de stock de albaranes eliminado; la salida ocurre al entregar.
- La entrega es atómica: un fallo de stock impide cambiar el estado.
- Movimientos de stock asociados a almacén origen o destino.
- Bloqueo optimista añadido a artículos mediante Flyway V21.
- Consulta temporal de movimientos corregida para usar `LocalDate`.
- Conversiones de albaranes atribuidas al usuario autenticado real.
- Actualización automática desde Git desactivada por defecto.
- Quality gate JaCoCo activado en `verify`: mínimo 35 % de líneas y 20 % de ramas.
- Numeración de albaranes sustituida por una reserva atómica por ejercicio, compartida por ambos servicios y validada con 20 reservas concurrentes.
- Migraciones verificadas sobre MySQL 8.4 con Testcontainers: instalación limpia y actualización V21→V22 preservando datos.
- Corregidas incompatibilidades MySQL en V12/V14 y añadidas las tablas base ausentes para proveedores y pedidos de compra.
- Suite actualizada a 172 pruebas; cobertura global del 36,51 % de líneas y 23,51 % de ramas.
