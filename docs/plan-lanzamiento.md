# Plan de Lanzamiento — ERPTahona
**Última actualización:** 2026-06-25  
**Estado del build:** BUILD SUCCESS (3 tests fallando, 1 error)  
**Objetivo:** Aplicación 100% operativa y segura en producción

---

## Estado actual de partida

| Métrica | Valor | Objetivo |
|---------|-------|----------|
| Build | ✅ OK | ✅ |
| Tests pasando | 211 / 215 | 215 / 215 |
| Cobertura líneas | 50.27% | ≥ 60% |
| Cobertura ramas | 29.92% | ≥ 35% |
| Entidades JPA | 66 | — |
| Migraciones Flyway | V0–V30 | — |
| VeriFactu validado | ❌ No | ✅ Sí |

---

## FASE 0 — Reparar tests y errores críticos conocidos
*Condición de entrada: ninguna. Hacer esto primero.*

### 0.1 · Eliminar `'unsafe-eval'` de la CSP
> **Resultado (2026-07):** ⚠️ **Excepción aceptada, no eliminada.** La migración al build `@alpinejs/csp`
> se abortó porque 6 plantillas dependen de expresiones inline no soportadas por ese build. La CSP mantiene
> `'unsafe-eval'` en `script-src` como desviación conocida y documentada (ver [estado-actual](estado-actual.md)
> y [architecture](architecture.md)). El resto de la CSP usa nonce por petición. Lo de abajo queda como
> registro del intento.

**Archivo:** `src/main/java/alicanteweb/erp/config/CspNonceFilter.java:29`  
**Problema:** El test `CspNonceFilterTest` falla porque espera que la CSP NO contenga `'unsafe-eval'`, pero sí lo contiene. Además anula la protección XSS del nonce.  
**Acción:**
1. Abrir `CspNonceFilter.java` y quitar `'unsafe-eval'` de `script-src`.
2. Auditar los templates Thymeleaf y scripts Alpine.js en `src/main/resources/static/` en busca de cualquier uso de `eval()`, `new Function()`, o `x-html`. Eliminarlos o reescribirlos sin eval.
3. Si Alpine.js lo requiere, cambiar al build `@alpinejs/csp` (versión compatible con CSP estricta) en lugar del bundle estándar.
4. Verificar que `CspNonceFilterTest` pasa.

**Criterio de aceptación:** `mvn test -Dtest=CspNonceFilterTest` → verde. Navegador sin errores CSP en consola.

---

### 0.2 · Corregir autorización web: 403 en lugar de 302
**Archivo:** `src/test/java/alicanteweb/erp/WebSecurityTest.java`  
**Problema:** Un usuario sin permiso que accede a `/web/usuarios` recibe 302 (redirect a login) en lugar de 403 (acceso denegado). Significa que Spring Security no está rechazando en la capa HTTP — lo rechaza solo cuando el método de servicio se ejecuta.  
**Acción:**
1. En `SecurityConfig.java`, añadir reglas URL explícitas **antes** del `.anyRequest().authenticated()`:
   ```java
   .requestMatchers("/web/usuarios/**", "/web/backups/**", "/web/auditoria/**")
       .hasRole("ADMIN")
   .requestMatchers("/web/contabilidad/**", "/web/fiscal/**", "/web/tesoreria/**")
       .hasAnyRole("ADMIN", "CONTABLE")
   ```
2. Revisar el test `WebSecurityTest` para confirmar qué URLs están fallando y añadir las reglas que falten.
3. Verificar que el redirect por 403 vaya a `/web/acceso-denegado`.

**Criterio de aceptación:** `mvn test -Dtest=WebSecurityTest` → verde.

---

### 0.3 · Corregir error en `ContabilidadServiceTest`
**Archivo:** `src/test/java/alicanteweb/erp/service/ContabilidadServiceTest.java`  
**Problema:** `generarAsientoFactura_conUsuario_audita` lanza error al crear el asiento. `AuditoriaService` puede no estar correctamente mockeado o el servicio tiene una dependencia circular.  
**Acción:**
1. Leer el stack trace completo del test ejecutando: `mvn test -Dtest=ContabilidadServiceTest`.
2. Verificar que `ContabilidadService` tiene `AuditoriaService` inyectado como `@Autowired` o en el constructor.
3. En el test, asegurarse de que `AuditoriaService` está declarado como `@MockBean`.
4. Corregir la causa raíz (inyección o setup de mock).

**Criterio de aceptación:** `mvn test -Dtest=ContabilidadServiceTest` → verde.

---

### 0.4 · Corregir race conditions en numeración de documentos
Los siguientes servicios tienen race conditions que en producción generarán duplicados o errores:

#### 0.4.a — `AsientoContableRepository` SUBSTRING off-by-one (CRÍTICO — crashea en el segundo asiento del año)
**Archivo:** `src/main/java/alicanteweb/erp/repository/AsientoContableRepository.java:42`  
El formato del número es `YYYY-NNNN`. La JPQL actual:
```sql
CAST(SUBSTRING(a.numero, 5) AS int)
```
`SUBSTRING(numero, 5)` en JPQL es 1-based, extrae desde posición 5 → da `-NNNN` (con el guión), y `CAST('-0001' AS int)` = -1. El siguiente número siempre es 0 → unique constraint falla en el segundo asiento del año.  
**Fix:** Cambiar a `SUBSTRING(a.numero, 6)` (saltarse el guión) y añadir `@Lock(LockModeType.PESSIMISTIC_WRITE)` al método.

#### 0.4.b — `ContabilidadService.generarNumeroAsiento()` sin lock
**Archivo:** `src/main/java/alicanteweb/erp/service/ContabilidadService.java` (~línea 384)  
Lectura de MAX + INSERT sin lock → dos threads concurrentes generan el mismo número y uno pierde el asiento.  
**Fix:** Añadir `@Lock(PESSIMISTIC_WRITE)` en `AsientoContableRepository.findMaxNumeroByYear`, exactamente igual que en `FacturaSerieSequenceRepository`.

#### 0.4.c — `AlbaranNumeroService` — `LAST_INSERT_ID()` rompe entre conexiones
**Archivo:** `src/main/java/alicanteweb/erp/service/AlbaranNumeroService.java:25`  
`INSERT … ON DUPLICATE KEY` + `SELECT LAST_INSERT_ID()` en dos queries separadas. HikariCP puede darlas en conexiones distintas → `LAST_INSERT_ID()` devuelve 0 → todos los albaranes desde el segundo reciben el número 0.  
**Fix:** Reescribir usando el mismo patrón que `FacturaSerieSequence`: una entidad `AlbaranSerieSequence` con `@Lock(PESSIMISTIC_WRITE)` + `saveAndFlush` dentro de una sola transacción.

#### 0.4.d — `OrdenProduccionService.generarNumero()` sin lock
**Archivo:** `src/main/java/alicanteweb/erp/service/OrdenProduccionService.java:97-101`  
Lee MAX e inserta en dos pasos sin transacción atómica. Además, el `SUBSTRING` extrae solo 4 caracteres del final, lo que rompe con >9999 órdenes.  
**Fix:** Crear tabla `orden_produccion_serie_sequence` (migración V31) y seguir el mismo patrón de `FacturaSerieSequence`.

#### 0.4.e — `FacturaService` race condition en primera secuencia del año
**Archivo:** `src/main/java/alicanteweb/erp/service/FacturaService.java:111-119`  
`orElseGet(() -> crearSecuenciaFactura(...))` no está protegido: dos threads concurrentes crean dos filas de secuencia.  
**Fix:** Usar `INSERT … ON DUPLICATE KEY UPDATE ultimo_numero = ultimo_numero` + `@Lock(PESSIMISTIC_WRITE)` en la búsqueda. O bien un `@Transactional` con `SERIALIZABLE` isolation en `crearSecuenciaFactura`.

**Criterio de aceptación:** Tests de numeración pasan. Ejecutar `ContabilidadServiceTest`, `AlbaranNumeroServiceTest`, `FacturaServiceTest` → verde.

---

### 0.5 · Devolucion no repone stock
**Archivo:** `src/main/java/alicanteweb/erp/service/DevolucionService.java:108-113`  
`aceptarDevolucion` cambia estado pero nunca llama `StockService.registrarEntrada()`.  
**Fix:**
```java
public Devolucion aceptarDevolucion(Long devolucionId) {
    Devolucion devolucion = devolucionRepository.findById(devolucionId)
        .orElseThrow(() -> new IllegalArgumentException("Devolución no encontrada: " + devolucionId));
    devolucion.setEstado("ACEPTADA");
    // Reponer stock
    for (DevolucionLinea linea : devolucion.getLineas()) {
        if (linea.getArticulo() != null && linea.getCantidad() != null) {
            stockService.registrarEntrada(
                linea.getArticulo().getId(),
                devolucion.getAlmacenId(),  // o el almacén por defecto
                linea.getCantidad(),
                "Devolución aceptada: " + devolucionId
            );
        }
    }
    return devolucionRepository.save(devolucion);
}
```
**Criterio de aceptación:** Test `DevolucionServiceTest.aceptarDevolucion_reponStock` pasa (crear si no existe).

---

### 0.6 · Albarán no se marca como FACTURADO al convertir
**Archivo:** `src/main/java/alicanteweb/erp/service/AlbaranService.java` (método `convertirAFactura`)  
Después de vincular el albarán a la factura, nunca se llama `albaran.setEstado("FACTURADO")`.  
**Fix:** Añadir al final del método, antes del return:
```java
albaran.setEstado("FACTURADO");
albaranRepository.save(albaran);
```
**Criterio de aceptación:** Tras convertir un albarán a factura, `albaran.estado = 'FACTURADO'` en la BD.

---

## FASE 1 — Seguridad

### 1.1 · CSRF explícito en SecurityConfig
**Archivo:** `src/main/java/alicanteweb/erp/config/SecurityConfig.java`  
Aunque Spring Security 6 activa CSRF por defecto, no hay configuración explícita. Cualquier desactivación futura pasaría desapercibida.  
**Fix:** Añadir explícitamente en el `SecurityFilterChain`:
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
)
```
Y verificar que todos los forms Thymeleaf usan `th:action` (que inyecta el token automáticamente).

**Criterio de aceptación:** `WebSecurityTest` incluye un test que verifica que un POST sin token recibe 403.

---

### 1.2 · Corregir doble incremento del contador de intentos fallidos
**Archivos:** `AutenticacionService.java:66` + `UsuarioService.java:199`  
`validarCredenciales` ya incrementa el contador. `AutenticacionService.login` lo vuelve a llamar → la cuenta se bloquea en `ceil(MAX/2)` intentos en lugar de MAX.  
**Fix:** Eliminar la llamada a `incrementarIntentosFallidos` en `AutenticacionService.login` (línea 66). `validarCredenciales` ya lo gestiona completo.

**Criterio de aceptación:** Test: 5 intentos fallidos bloquean, el 4º no.

---

### 1.3 · Añadir @PreAuthorize a controladores web críticos
Los siguientes controladores solo protegen en el servicio, no en la capa HTTP. Un usuario puede llegar al formulario antes de que se rechace la acción:

| Controlador | Permiso necesario |
|-------------|------------------|
| `UsuarioWebController` | `ADMIN` |
| `BackupWebController` | `ADMIN` |
| `AuditoriaWebController` | `ADMIN` |
| `ContabilidadWebController` | `ADMIN, CONTABLE` |
| `FiscalWebController` | `ADMIN, CONTABLE` |
| `TesoreriaWebController` | `ADMIN, CONTABLE` |
| `EmpresaWebController` | `ADMIN` |

**Fix:** Añadir `@PreAuthorize("hasRole('ADMIN')")` a nivel de clase en los controladores ADMIN-only, y `@PreAuthorize("hasAnyRole('ADMIN','CONTABLE')")` en los mixtos.

**Criterio de aceptación:** `WebSecurityTest` cubre todos los módulos listados con tests negativos (rol insuficiente → 403).

---

### 1.4 · Contraseña en texto plano en V1
**Archivo:** `src/main/resources/db/migration/V1__create_tables.sql:165`  
El INSERT semilla usa `password = 'admin'` en texto plano.  
**Fix:** Crear migración `V31__seed_admin_hash.sql` que actualice el hash:
```sql
-- Actualizar contraseña admin a hash PBKDF2 seguro (la misma lógica que V8)
UPDATE usuarios SET password = <hash_pbkdf2_de_Admin2024!> WHERE username = 'admin'
  AND password = 'admin';
```
La contraseña inicial segura se configura vía variable de entorno `ADMIN_INITIAL_PASSWORD`.  
**Nota:** Nunca dejar `password = 'admin'` en la migración. El V8 ya hace re-hashing, pero solo si el usuario existe con el hash de Hibernate.

**Criterio de aceptación:** Con la BD recién creada, `SELECT password FROM usuarios WHERE username='admin'` no devuelve `'admin'`.

---

### 1.5 · Eliminar mensaje de excepción raw en `WebErrorHandler`
**Archivo:** `src/main/java/alicanteweb/erp/config/WebErrorHandler.java:53`  
```java
model.addAttribute("mensaje", e.getMessage());  // ← expone detalles internos
```
**Fix:**
```java
model.addAttribute("mensaje", "La solicitud contiene datos inválidos. Revisa los campos e inténtalo de nuevo.");
log.warn("Argumento inválido en {}: {}", req.getRequestURI(), e.getMessage());
```

---

### 1.6 · Parámetro `sort` sin allowlist en controladores paginados
**Archivos:** `AlbaranWebController.java`, `ClienteWebController.java`, `ArticuloWebController.java`, `FacturaWebController.java`  
**Fix:** En cada controlador, añadir antes del `PageRequest`:
```java
private static final Set<String> SORT_FIELDS_ALBARAN = Set.of("fecha", "numero", "estado", "total");

// En el método:
String sortField = SORT_FIELDS_ALBARAN.contains(sort) ? sort : "fecha";
PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
```
Y limitar `size`:
```java
int safeSize = Math.min(size, 100);
```

---

### 1.7 · `WebChildEntityController` sin control de acceso
**Archivo:** `src/main/java/alicanteweb/erp/controller/rest/WebChildEntityController.java`  
El controlador hace CRUD sobre líneas de facturas, asientos, ingredientes sin comprobar si el usuario tiene acceso al documento padre.  
**Fix:**
1. Añadir `@PreAuthorize("isAuthenticated()")` a nivel de clase (ya existe en SecurityConfig, pero ser explícito).
2. Para operaciones de escritura (POST/PUT/DELETE), añadir comprobación del módulo: llamar a `requireAccess(module, "WRITE")` igual que en `WebEntityController`.
3. Añadir tests en `WebChildEntityControllerTest` que verifican acceso denegado.

---

### 1.8 · Configurar timeout de sesión
**Archivo:** `src/main/resources/application.properties`  
**Fix:** Añadir:
```properties
server.servlet.session.timeout=15m
```

---

### 1.9 · Caja puede quedar en negativo
**Archivo:** `src/main/java/alicanteweb/erp/service/MovimientoCajaService.java:119`  
`registrarGasto` no verifica saldo previo.  
**Fix:**
```java
BigDecimal saldoActual = calcularSaldoCaja();
if (importe.compareTo(saldoActual) > 0) {
    throw new IllegalArgumentException(
        "Saldo insuficiente en caja. Saldo actual: " + saldoActual + ", gasto solicitado: " + importe);
}
```

---

### 1.10 · Manejar `ObjectOptimisticLockingFailureException` globalmente
**Archivos:** `WebErrorHandler.java`, `RestExceptionHandler.java`  
Las 4 entidades con `@Version` (`AlbaranVenta`, `Articulo`, `Factura`, `Presupuesto`) lanzan esta excepción bajo concurrencia pero nadie la captura → 500 genérico.  
**Fix en `WebErrorHandler.java`:**
```java
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
@ResponseStatus(HttpStatus.CONFLICT)
public String optimisticLock(HttpServletRequest req, Model model) {
    model.addAttribute("titulo", "Conflicto de edición");
    model.addAttribute("codigo", 409);
    model.addAttribute("mensaje", "Otro usuario modificó este registro mientras lo editabas. Recarga la página e inténtalo de nuevo.");
    model.addAttribute("url", req.getRequestURI());
    return "error";
}
```
**Fix en `RestExceptionHandler.java`:** Devolver `409 Conflict` con JSON.

---

## FASE 2 — Integridad de datos

### 2.1 · NPE en `AlbaranService.convertirVariosAFactura` con cliente null
**Archivo:** `src/main/java/alicanteweb/erp/service/AlbaranService.java:290-292`  
```java
if (!albaran.getCliente().getId().equals(cliente.getId()))  // NPE si getCliente() == null
```
**Fix:**
```java
if (albaran.getCliente() == null) {
    throw new IllegalArgumentException("El albarán " + albaran.getNumero() + " no tiene cliente asignado");
}
if (!albaran.getCliente().getId().equals(cliente.getId())) {
```

---

### 2.2 · NPE en `OrdenProduccionService` al completar producción
**Archivo:** `src/main/java/alicanteweb/erp/service/OrdenProduccionService.java:144`  
```java
stockService.registrarSalida(ing.getArticulo().getId(), ...)  // NPE si getArticulo() == null
```
**Fix:** Añadir el mismo guard que ya existe en `AlbaranService.marcarEntregado`:
```java
if (ing.getArticulo() == null || ing.getCantidad() == null) continue;
```

---

### 2.3 · `FacturaValidacionService` sin `@Transactional`
**Archivo:** `src/main/java/alicanteweb/erp/service/FacturaValidacionService.java`  
Accede a `factura.getCliente().getCif()` (relación lazy) sin transacción activa → `LazyInitializationException`.  
**Fix:** Añadir `@Transactional(readOnly = true)` a la clase o al método `validarParaEmision`.

---

### 2.4 · Stock por almacén diverge del stock global silenciosamente
**Archivo:** `src/main/java/alicanteweb/erp/service/StockService.java:45`  
```java
aa.setStock(nuevo.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : nuevo);
```
Clampear a cero oculta un sobre-despacho a nivel de almacén.  
**Fix:** Lanzar excepción igual que con el stock global:
```java
if (nuevo.compareTo(BigDecimal.ZERO) < 0) {
    throw new IllegalStateException(
        "Stock insuficiente en almacén " + aa.getAlmacen().getNombre() +
        " para artículo " + aa.getArticulo().getCodigo());
}
aa.setStock(nuevo);
```

---

## FASE 3 — Flyway y base de datos

### 3.1 · Migración V31 — Secuencia órdenes de producción
**Crear:** `src/main/resources/db/migration/V31__orden_produccion_sequence.sql`
```sql
CREATE TABLE IF NOT EXISTS orden_produccion_serie_sequence (
    serie    VARCHAR(20)  NOT NULL,
    ejercicio INT         NOT NULL,
    prefijo  VARCHAR(10)  NOT NULL,
    ultimo_numero INT     NOT NULL DEFAULT 0,
    PRIMARY KEY (serie, ejercicio)
);
```
Y actualizar `OrdenProduccionService` para usar esta tabla con `@Lock(PESSIMISTIC_WRITE)`.

---

### 3.2 · Habilitar Flyway en el perfil dev
**Archivo:** `src/main/resources/application-dev.properties:17`  
Cambiar:
```properties
spring.flyway.enabled=${SPRING_FLYWAY_ENABLED:false}
```
Por:
```properties
spring.flyway.enabled=true
spring.jpa.hibernate.ddl-auto=validate
```
Con Flyway activo, `ddl-auto=update` ya no es necesario y puede ocultar migraciones que faltan.

---

### 3.3 · Verificar y ejecutar migraciones en CI con Testcontainers
**Archivo:** `src/test/java/alicanteweb/erp/FlywayMySqlMigrationTest.java`  
Este test existe pero solo corre con Docker disponible.  
**Fix:** Añadir al CI pipeline (GitHub Actions / Jenkins) un job con `services: mysql` o Docker Compose que ejecute:
```bash
mvn verify -Dtest=FlywayMySqlMigrationTest -DfailIfNoTests=false
```
No hacer merge sin que este test pase.

---

### 3.4 · Ruta de backup absolute y fuera del webroot
**Archivo:** `src/main/resources/application.properties:49`  
```properties
backup.directory=backups           # ← relativa, puede quedar dentro del webroot
backup.mysqldump-path=C:/Program Files/...  # ← Windows, falla en Linux
```
**Fix en application.properties:**
```properties
backup.directory=${ERP_BACKUP_DIR:/var/backups/erptahona}
backup.mysqldump-path=${MYSQLDUMP_PATH:mysqldump}
```
Documentar las variables de entorno en `.env.example` y en el [RUNBOOK](RUNBOOK.md) §2 (rotación de secretos).

---

## FASE 4 — VeriFactu y cumplimiento fiscal

### 4.1 · Validación completa contra entorno AEAT
**Estado actual:** El endpoint apunta al sandbox `prewww2.aeat.es`. Nunca se ha validado una respuesta real.  
**Pasos obligatorios antes de ir a producción:**

1. **Obtener certificado digital** de representación para el NIF de la empresa (ACA o similar).
2. **Configurar certificado** en `VerifactuProperties.java` vía variables de entorno (`VERIFACTU_CERT_PATH`, `VERIFACTU_CERT_PASSWORD`).
3. **Enviar facturas de prueba** al sandbox y verificar:
   - El XML generado es aceptado sin errores de esquema.
   - La firma digital (XMLDSig) es válida.
   - El QR generado es correcto (URL + contenido codificado).
   - La respuesta AEAT devuelve código de aceptación.
   - La cadena de hashes es coherente (usar `VerifactuDiagnosticoService.validarCadenaIntegridad`).
4. **Cambiar endpoint** a producción (`www2.agenciatributaria.gob.es`) solo después de superar el sandbox.
5. **Activar** en `application-prod.properties`: `verifactu.aeat.enabled=true`.
6. **Nota de plazo:** Según RD 1007/2023 modificado, obligatorio para software de facturación a partir del **01/07/2027** para empresas en régimen general. Hay margen, pero mejor validar cuanto antes.

**Criterio de aceptación:** Al menos 10 facturas de prueba enviadas al sandbox con respuesta `AceptadoConErrores` o `Correcto`.

**Decisión de lanzamiento (checklist "Validación VeriFactu"):** se lanza con la
remisión a AEAT **deshabilitada explícitamente** (`VERIFACTU_AEAT_ENABLED=false`,
default del compose y de `application-prod.properties`; el guard de arranque
`DatabaseStartupChecker` impide activar la remisión apuntando al sandbox). Las
facturas siguen generando su registro, hash encadenado y QR en local.

- **Fecha límite interna para completar la validación sandbox:** `__PENDIENTE — fijar al lanzar__`
  (debe ser muy anterior al plazo legal del punto 6: 01/07/2027).
- **Responsable:** `__PENDIENTE__`
- Al completar la validación: seguir los pasos 4-5 de arriba y actualizar esta sección.

---

### 4.2 · Corregir hash chain ante fallos de envío a AEAT
**Archivo:** `src/main/java/alicanteweb/erp/service/VerifactuService.java:648-722`  
El hash anterior se avanza ANTES de confirmar con AEAT. Si falla, el registro queda en ERROR en medio de la cadena.  
**Fix:** Guardar el registro de evidencia con estado `BORRADOR`. Solo avanzar el hash chain y cambiar a `PENDIENTE` después de una respuesta exitosa de AEAT (o de la cola de reintentos).

---

### 4.3 · Cola de reintentos VeriFactu idempotente
**Descripción:** Actualmente si el envío a AEAT falla, no hay reintentos automáticos.  
**Fix:** Crear un `@Scheduled` que:
1. Busca evidencias en estado `PENDIENTE` o `ERROR` con `intentos < 3`.
2. Reintenta el envío.
3. Incrementa `intentos` en cada intento.
4. Tras 3 fallos, notifica al administrador por email.

---

### 4.4 · Modelo 347 — Validar con casos reales
Generar el fichero de declaración con datos del ejercicio actual y verificar que el formato es correcto con la herramienta oficial de la AEAT antes de la primera declaración (febrero del año siguiente).

---

## FASE 5 — Rendimiento y operación

### 5.1 · Añadir Spring Actuator con endpoints mínimos
**Archivo:** `pom.xml`  
Añadir:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
En `application-prod.properties`:
```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never
management.server.port=8081
```
Exponer en puerto separado, no accesible públicamente.

---

### 5.2 · Límite de tamaño en imports CSV
**Archivos:** `ImportWebController.java`, `application.properties`  
Añadir:
```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```
Y en `ImportService`, añadir validación de número máximo de filas:
```java
if (lineas.size() > 5000) {
    throw new IllegalArgumentException("El fichero supera el límite de 5.000 filas");
}
```

---

### 5.3 · Limitar tamaño de página en controladores paginados
(Ver también 1.6) Asegurar que ningún endpoint devuelve más de 100 filas por petición sin paginación explícita.

---

## FASE 6 — Docker y despliegue productivo

### 6.1 · Dockerfile productivo
**Archivo:** `Dockerfile` (crear en raíz)
```dockerfile
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S erp && adduser -S erp -G erp
WORKDIR /app
COPY target/erp-tahona-*.jar app.jar
USER erp
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
```

---

### 6.2 · `docker-compose.prod.yml`
**Archivo:** `docker-compose.prod.yml` (crear en raíz)
```yaml
services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backups:/var/backups/erptahona
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 10

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: db
      DB_PORT: 3306
      DB_NAME: ${DB_NAME}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      ERP_BACKUP_DIR: /var/backups/erptahona
      MYSQLDUMP_PATH: /usr/bin/mysqldump
      # Secretos adicionales vía .env (nunca en git)

volumes:
  mysql_data:
```

---

### 6.3 · Variables de entorno requeridas en producción
Crear archivo `.env.example` (sin valores reales) en la raíz:
```bash
# Base de datos
DB_NAME=erptahona
DB_USER=erp_user
DB_PASSWORD=<contraseña_segura>
DB_ROOT_PASSWORD=<contraseña_root>

# Aplicación
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
ADMIN_INITIAL_PASSWORD=<contraseña_admin_inicial>

# VeriFactu
VERIFACTU_CERT_PATH=/secrets/verifactu.p12
VERIFACTU_CERT_PASSWORD=<password_certificado>

# Backup
ERP_BACKUP_DIR=/var/backups/erptahona
MYSQLDUMP_PATH=/usr/bin/mysqldump

# Email (notificaciones)
SPRING_MAIL_HOST=smtp.tudominio.com
SPRING_MAIL_USERNAME=erp@tudominio.com
SPRING_MAIL_PASSWORD=<password_smtp>

# Integraciones externas (opcionales)
CLIENTES_AUTOCOMPLETAR_API_KEY=<api_key>
```

---

### 6.4 · Nginx como proxy inverso con TLS
**Archivo:** `nginx/nginx.conf` (crear)
- Redirigir HTTP → HTTPS (301)
- TLS 1.2/1.3, HSTS `max-age=31536000; includeSubDomains`
- Proxy a `http://app:8080`
- Denegar acceso a `/actuator`
- Límite de cuerpo: `client_max_body_size 10M`

---

## FASE 7 — Calidad y cobertura

### 7.1 · Objetivo de cobertura
| Métrica | Actual | Objetivo |
|---------|--------|----------|
| Líneas | 50.27% | 60% |
| Ramas | 29.92% | 35% |

Añadir tests en:
- `DevolucionServiceTest` — aceptar + reponer stock
- `MovimientoCajaServiceTest` — saldo negativo bloqueado
- `AlbaranServiceTest` — estado FACTURADO tras conversión
- `StockServiceTest` — stock almacén no va a negativo
- `ContabilidadServiceTest` — numeración secuencial sin duplicados
- `WebSecurityTest` — cobertura de todos los módulos protegidos

---

### 7.2 · Tests de concurrencia
Para los servicios de numeración (Fase 0.4), añadir tests que lancen 10 threads simultáneos y verifican que no hay números duplicados:
```java
@Test
void generarNumeroAlbaran_concurrente_sinDuplicados() throws Exception {
    int threads = 10;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    List<Future<String>> futures = IntStream.range(0, threads)
        .mapToObj(i -> pool.submit(() -> albaranNumeroService.siguiente("A", 2026)))
        .collect(toList());
    Set<String> numeros = futures.stream().map(f -> f.get()).collect(toSet());
    assertThat(numeros).hasSize(threads);  // sin duplicados
}
```

---

### 7.3 · Tests E2E de flujos críticos
Con Playwright o Selenium, cubrir:
1. **Login + navegación**: usuario válido → dashboard
2. **Ciclo de venta completo**: crear cliente → presupuesto → pedido → albarán → factura → cobro
3. **Ciclo de compra**: pedido compra → recepción → factura compra → pago
4. **Producción**: orden de producción → horneada → stock actualizado
5. **Reparto**: hoja de ruta → marcar entregado → trazabilidad

---

## CHECKLIST FINAL ANTES DE ARRANCAR PRODUCCIÓN

### Código
- [ ] 0 tests fallando (`mvn verify`)
- [ ] `'unsafe-eval'` eliminado de la CSP
- [ ] Race conditions de numeración corregidas (Fase 0.4)
- [ ] VeriFactu validado contra sandbox AEAT
- [ ] Certificado digital real configurado

### Base de datos
- [ ] V31 aplicada (secuencia órdenes producción)
- [ ] Flyway migration test pasa con MySQL real
- [ ] Sin contraseñas en texto plano en tablas
- [ ] Backup funcional y restauración probada desde cero

### Seguridad
- [ ] CSRF explícito en SecurityConfig
- [ ] @PreAuthorize en todos los controladores sensibles
- [ ] Timeout de sesión configurado (15m)
- [ ] HTTPS + HSTS activo en proxy Nginx
- [ ] Headers de seguridad verificados con `securityheaders.com`
- [ ] `.env.example` creado, `.env` en `.gitignore`
- [ ] `application-local.properties` en `.gitignore` (ya está)

### Operación
- [ ] Docker Compose prod probado en servidor limpio
- [ ] Nginx con TLS configurado y certificado válido (Let's Encrypt)
- [ ] Backup automático verificado (cron 02:00 → fichero en `/var/backups/erptahona`)
- [ ] Restauración de backup probada en entorno separado
- [ ] Notificaciones email funcionando (SMTP configurado)
- [ ] Log de aplicación rotado (`logback-spring.xml` con rolling)
- [ ] Healthcheck `/actuator/health` responde
- [ ] Monitorización básica activa (UptimeRobot o similar)

### Negocio
- [ ] EmpresaConfig con datos reales (NIF, nombre, dirección, logo)
- [ ] Plan contable configurado para la empresa
- [ ] Al menos un almacén activo creado
- [ ] Roles y usuarios de producción creados (no usar admin para el día a día)
- [ ] Contraseña admin cambiada en el primer acceso
- [ ] Serie de facturación del año en curso configurada

---

## Orden de ejecución recomendado

```
Semana 1:  Fase 0 completa (tests verde, race conditions, devolucion, albarán)
Semana 2:  Fase 1 completa (seguridad: CSRF, permisos, session, caja, optimistic lock)
Semana 3:  Fase 2 (NPEs, lazy, stock almacén) + Fase 3 (Flyway, backup)
Semana 4:  Fase 4 (VeriFactu sandbox + certificado real)
Semana 5:  Fase 5 + 6 (Docker, Nginx, entorno prod)
Semana 6:  Fase 7 (cobertura, E2E) + Checklist final + Arranque
```

---

*Documento generado: 2026-06-25. Actualizar este fichero al cerrar cada ítem.*