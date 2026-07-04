# Pipeline de remediación pre-producción con IA — ERP Tahona

> Generado a partir de la revisión de código del 2026-07-04 (rama `refactor-general`, 247 tests verdes).
> Cada fase es una sesión de trabajo independiente con un prompt listo para copiar en Claude Code.
> Ejecuta las fases **en orden**: cada una asume que la anterior está mergeada y en verde.

---

## Cómo usar este pipeline

1. Abre una sesión nueva de Claude Code en la raíz del repo por cada fase (contexto limpio = mejores resultados).
2. Copia el bloque **PROMPT** de la fase tal cual. Los prompts ya incluyen ficheros, líneas y criterios de aceptación: no hace falta re-explicar el proyecto.
3. Al terminar cada fase, ejecuta la **verificación** indicada antes de dar la fase por cerrada.
4. Commit por fase, con el prefijo indicado, para poder revertir de forma quirúrgica.

**Regla general para todos los prompts:** exige tests nuevos para cada fix (la suite actual, 247 tests, no detecta ninguno de estos problemas), y prohíbe refactors oportunistas fuera del alcance de la fase.

---

## FASE 1 — Cumplimiento fiscal y agujero crítico (½ día) 🔴

**Objetivo:** cerrar la mutabilidad de facturas emitidas (RRSIF/VeriFactu) y el guard del endpoint AEAT.

**Hallazgos que resuelve:**
- **C1 (Crítico):** `WebChildEntityController` (`src/main/java/alicanteweb/erp/controller/rest/WebChildEntityController.java:34-36` y `:77-140`) permite POST/PUT/DELETE sobre `factura-lineas` y `factura-compra-lineas` vía `EntityManager` directo, sin comprobar el estado de la factura padre ni pasar por `FacturaService.save()` (que sí protege en `FacturaService.java:101-115`). Además no recalcula el total del padre.
- **M1 (Medio):** `application-prod.properties:51` deja el endpoint VeriFactu de **pre**producción (`prewww2.aeat.es`) como default mientras el QR tributario (`application.properties:45`) apunta a producción.

### PROMPT

```text
Actúa como ingeniero senior de Spring Boot con foco en cumplimiento fiscal español (RRSIF/VeriFactu).
No hagas refactors fuera del alcance descrito. Trabaja en una rama nueva `fix/fase1-inmutabilidad-facturas`.

TAREA 1 — Inmutabilidad de líneas de factura:
En src/main/java/alicanteweb/erp/controller/rest/WebChildEntityController.java, los módulos
"factura-lineas" y "factura-compra-lineas" son escribibles por cualquier usuario autenticado
mediante create/update/delete (líneas ~77-140), persistiendo con EntityManager directamente.
Esto se salta la protección de FacturaService.save() (FacturaService.java:101-115), que impide
modificar facturas EMITIDA o con verifactuEnviada=true.

Implementa:
1. Un guard en create/update/delete de WebChildEntityController: si el hijo es factura-lineas o
   factura-compra-lineas, cargar la factura padre y rechazar con IllegalStateException si su
   estado NO es BORRADOR/REVISION o si verifactuEnviada es true. Sigue el estilo del guard
   READ_ONLY_MODULES que ya existe en WebEntityController.java:45-47.
2. Tras crear/editar/borrar una línea permitida (factura en borrador), recalcular y persistir el
   total de la factura padre (mira cómo lo hace DocumentoService.guardarLineasFactura con
   FinancialMath para ser consistente en redondeos).
3. Revisa si otros hijos del mapa CHILDREN tienen el mismo problema con padres que tengan
   máquina de estados (p. ej. asiento-lineas sobre asientos cerrados) y repórtalo, sin arreglarlo
   salvo que sea trivial.

TAREA 2 — Guard de arranque VeriFactu:
En la configuración (busca DatabaseStartupChecker en alicanteweb/erp/config), añade una
validación de arranque: si verifactu.aeat.enabled=true y verifactu.aeat.endpoint contiene
"prewww", la aplicación debe fallar al arrancar con un mensaje claro ("endpoint de pruebas AEAT
con remisión real habilitada"). Añade también un WARN al arrancar si el QR
(verifactu.qr.base-url) apunta a producción mientras el endpoint es de pruebas.

TESTS OBLIGATORIOS:
- Test de integración (MockMvc con @WithMockUser rol USER) que intente POST/PUT/DELETE en
  /api/web/children/factura-lineas/{id} sobre una factura EMITIDA y espere 4xx, y verifique que
  la línea NO cambió en BD.
- Test de que sobre una factura BORRADOR sí se puede y el total del padre queda recalculado.
- Test unitario del guard de arranque VeriFactu (enabled+prewww → excepción).

Al terminar: ejecuta ./mvnw test, confirma 0 fallos, y resume qué cambiaste con file:line.
```

### Verificación de cierre
```bash
./mvnw test
# Manual: con la app levantada y un usuario NO admin, intentar
# curl -X DELETE /api/web/children/factura-lineas/{facturaEmitidaId}/{lineaId} → debe dar 4xx
```
**Commit:** `fix(fiscal): inmutabilidad de líneas de factura emitida y guard endpoint AEAT`

---

## FASE 2 — Autorización de la API REST (1 día) 🟠

**Objetivo:** que el modelo de permisos granulares (`PermisoEvaluador` + `rolService.tienePermiso`) proteja los datos, no solo las vistas.

**Hallazgo que resuelve:**
- **A1 (Alto):** 0 usos de `@PreAuthorize` en los 19 controladores de `controller/rest/` (frente a 72 en `controller/web/`). Un usuario con rol limitado (p. ej. REPARTIDOR) puede llamar a `POST /api/web/facturas/{id}/emitir`, `/anular`, crear documentos, etc. (`DocumentoRestController.java:99-128`).

### PROMPT

```text
Actúa como ingeniero senior de Spring Security. Rama nueva `fix/fase2-autorizacion-api`.
El proyecto tiene un evaluador de permisos por rol ya hecho: @permisos.puede('modulo','accion')
(alicanteweb/erp/config/PermisoEvaluador.java, respaldado por RolService.tienePermiso). Se usa en
los controladores web (@PreAuthorize) pero NO en ningún controlador de controller/rest/.

PASO 1 — Inventario:
Lista todos los endpoints de src/main/java/alicanteweb/erp/controller/rest/ en una tabla:
método HTTP, ruta, qué hace, y qué par (módulo, acción) del modelo de permisos le corresponde.
Usa como referencia los pares módulo/acción que ya usan los web controllers
(grep @permisos.puede en controller/web/). Enséñame la tabla ANTES de tocar código y espera mi OK.

PASO 2 — Aplicación:
- Anota con @PreAuthorize("@permisos.puede('X','Y')") todos los endpoints de acción/escritura:
  como mínimo DocumentoRestController (crear/editar pedido, albarán, factura; emitir; anular;
  anular-rectificativa; convertir; factura-desde-albaranes), TPV, devoluciones, producción,
  inventario, recepciones, extractos bancarios y cierre contable.
- En WebEntityController.requireAccess y en WebChildEntityController, además de los grupos
  admin/contable actuales, integra rolService.tienePermiso(rolId, modulo, accion) mapeando
  GET→'ver', POST→'crear', PUT→'editar', DELETE→'eliminar'. Mantén el comportamiento actual
  para ADMIN/ADMINISTRADOR (acceso total).
- No rompas la app móvil del repartidor: RepartidorMovilController ya filtra por usuario;
  su permiso debe ser el del módulo hojas de ruta con acción 'ver'/'editar', no admin.
- OJO: las respuestas de error deben seguir el formato JSON del RestExceptionHandler existente
  (403 FORBIDDEN), no páginas HTML.

PASO 3 — Tests:
- Un test parametrizado de seguridad por controlador REST: usuario con rol sin permisos → 403;
  usuario con el permiso concreto → 2xx; ADMIN → 2xx. Usa @WithMockUser o el
  ErpUserPrincipal real según lo que ya haga WebSecurityTest.
- Verifica que el frontend no se rompe: los roles por defecto sembrados en V7__roles_permisos.sql
  deben conservar acceso a lo que hoy usan sus pantallas. Si detectas un permiso que falta en el
  seed, añade una migración Flyway nueva (V42+), nunca edites migraciones existentes.

Al terminar: ./mvnw test en verde y resumen de endpoints protegidos (tabla final).
```

### Verificación de cierre
```bash
./mvnw test
# Manual: login con un usuario de rol restringido y comprobar que sus pantallas siguen funcionando
# y que un curl a /api/web/facturas/1/emitir devuelve 403 JSON.
```
**Commit:** `fix(security): permisos granulares en la API REST`

---

## FASE 3 — Operaciones: TLS, restore y observabilidad (½–1 día) 🟠

**Objetivo:** cerrar los bloqueantes operativos: HTTPS real, restore de backup ensayado y documentado, y detección de reinicios en bucle.

**Hallazgos que resuelve:**
- **A2 (Alto):** sin TLS; `SESSION_COOKIE_SECURE=false` por defecto (`application-prod.properties:25`, `docker-compose.yml:48`). HSTS se emite pero es teatro sin HTTPS.
- **A3 (Alto):** `BackupService.restaurarBackup` (`BackupService.java:198`) no tiene ningún llamador ni test: el restore jamás se ha ejercitado. Además `restaurarBackup`/`deleteBackup` aceptan rutas arbitrarias (path traversal latente si algún día se exponen).
- **M6 (Medio):** observabilidad mínima (solo `/actuator/health`).

### PROMPT

```text
Actúa como ingeniero de plataforma/DevOps con experiencia en Spring Boot + Docker Compose.
Rama nueva `ops/fase3-tls-restore-observabilidad`. El stack actual: docker-compose.yml con
mysql:8.0 + app (Dockerfile multistage, usuario no root, healthcheck a /actuator/health).

TAREA 1 — TLS con reverse proxy:
Añade un servicio Caddy al docker-compose.yml como reverse proxy TLS delante de la app:
- Caddyfile versionado en el repo (directorio deploy/ o similar) con dominio parametrizado por
  variable de entorno; para LAN sin dominio público, documenta la alternativa con certificado
  interno de Caddy (tls internal).
- La app deja de publicar el puerto 8080 al host (solo red interna del compose).
- SESSION_COOKIE_SECURE pasa a true por defecto en el compose.
- Comprueba que server.forward-headers-strategy=native (ya configurado) hace que el rate
  limiter de login vea la IP real detrás de Caddy, y déjalo anotado en el Caddyfile.

TAREA 2 — Restore de backup seguro y ensayado:
En BackupService.java:
- Endurece restaurarBackup(String) y deleteBackup(String): resolver la ruta contra
  backup.directory con Path.normalize() y rechazar cualquier ruta que escape del directorio
  (defensa ante path traversal). Acepta solo ficheros backup_*.sql.
- Mueve el fichero temporal de credenciales de createDefaultsFile a java.io.tmpdir SIEMPRE
  (hoy se crea dentro del directorio de backups persistido, BackupService.java:134).
- Expón el restore en BackupWebController SOLO para ADMIN, con confirmación explícita
  (parámetro de confirmación con el nombre del fichero) y registro en auditoría.
- Test de integración con Testcontainers-MySQL (ya está como dependencia): crear backup,
  insertar un registro, restaurar, verificar que el registro desapareció. Y test del guard
  anti-traversal (ruta con ../ → excepción).

TAREA 3 — Runbook y observabilidad:
- Escribe docs/RUNBOOK.md: arranque/parada, rotación de secretos del .env, procedimiento de
  backup y de restore paso a paso (incluye el comando manual con docker exec por si la app no
  arranca), actualización de versión, y qué mirar cuando el healthcheck falla.
- Expón management.endpoints adicionales (metrics) restringidos a ADMIN (ya hay patrón en
  application.properties:27-29).
- Añade al runbook cómo detectar restart-loop: docker inspect + RestartCount, y deja un
  ejemplo de alerta simple (script cron o healthcheck de Uptime Kuma).

Al terminar: ./mvnw test en verde, docker compose config sin errores, y resumen.
```

### Verificación de cierre
```bash
./mvnw test
docker compose up -d --build
# https:// responde con certificado, http://:8080 ya no está publicado,
# crear backup + restaurar desde la UI como ADMIN funciona,
# ensayo de restore documentado en docs/RUNBOOK.md ejecutado UNA VEZ de verdad.
```
**Commit:** `ops: TLS con Caddy, restore de backup endurecido y ensayado, runbook`

---

## FASE 4 — Deuda técnica y pulido (½ día) 🟡

**Objetivo:** cerrar los hallazgos medios/bajos restantes que no bloquean el lanzamiento.

**Hallazgos que resuelve:** M2, M3, M4, M5, B1–B5.

### PROMPT

```text
Actúa como ingeniero senior haciendo limpieza pre-lanzamiento. Rama `chore/fase4-deuda`.
Cambios pequeños e independientes: UN COMMIT POR PUNTO, en este orden. Si alguno se complica
más de lo descrito, páralo y repórtalo en vez de forzarlo.

1. CifradoService.java:203-204 — hashPassword loguea en DEBUG hasta 200 chars del hash (el hash
   PBKDF2 completo). Deja solo longitud y prefijo de algoritmo (p. ej. "{pbkdf2}"), nunca contenido.

2. SecurityConfig.java:307 (ErpUserPrincipal) — el principal retiene el hash de la contraseña en
   la sesión. Pasa null en el campo password del record al construirlo desde Usuario y verifica
   que nada lo consume (grep getPassword() sobre el principal).

3. GitUpdateService / app.update.* — decide conmigo antes de tocar: la feature hace git pull del
   workdir, inoperante en Docker (la app corre desde JAR) y con defaults de rama inconsistentes
   ("producción" con tilde en application.properties:92 vs "produccion" en GitUpdateService.java:31).
   Propón: (a) eliminarla junto con su UI y tests, o (b) dejarla solo documentada para despliegue
   desde fuente. Espera mi elección y ejecútala.

4. CSP sin unsafe-eval — sustituye el webjar de alpinejs por @alpinejs/csp (misma versión mayor),
   quita 'unsafe-eval' de CspNonceFilter.java:32 y recorre las plantillas Thymeleaf buscando
   expresiones Alpine no soportadas por el build CSP (x-data con expresiones inline complejas).
   Si hay más de ~5 plantillas afectadas, aborta este punto y repórtalo como tarea aparte.

5. Flyway en dev — en application-dev.properties activa Flyway por defecto y cambia
   ddl-auto a validate, para que el drift de esquema se detecte en dev y no en el arranque de
   prod. Documenta en README_RUN.md cómo regenerar la BD local desde cero.

6. OrdenProduccionService.java:84,97 — synchronized solo vale con una instancia. Migra la
   numeración al patrón de secuencia con lock pesimista que ya usa FacturaSerieSequenceRepository,
   o si prefieres no tocarlo, deja un comentario explícito de la restricción single-instance.

7. Higiene de repo — elimina del control de versiones (git rm --cached + .gitignore): audit/*.png,
   screenshots/, verify-*.png, FRONTEND_ANALISIS.md si está obsoleto, test-erp-flows.mjs si ya no
   se usa, y los startup*.log de la raíz. NO borres nada del disco sin confirmar, solo del índice.

8. Logs con emojis (✅❌🔄 en BackupService, ContabilidadService, Modelo347Service...) — se ven
   como "?" en consolas no UTF-8. Sustitúyelos por prefijos ASCII ([OK], [ERROR], [BACKUP]).

Tras cada punto: ./mvnw test. Al final, resumen de los 8 puntos con estado (hecho/abortado/decisión).
```

### Verificación de cierre
```bash
./mvnw test          # 247+ tests en verde
git log --oneline    # un commit por punto
```
**Commit(s):** `chore: ...` (uno por punto)

---

## FASE 5 — Verificación final y checklist de lanzamiento (2-3 h) ✅

**Objetivo:** validación cruzada de todo el pipeline antes del go-live. Sesión de solo-verificación: no se arregla nada aquí, solo se reporta.

### PROMPT

```text
Actúa como auditor de seguridad y release manager. NO modifiques código: solo verifica y reporta.

1. Re-verifica los fixes de las fases 1-4 leyendo el código actual (no te fíes del historial):
   - factura-lineas y factura-compra-lineas rechazan escritura sobre facturas EMITIDA/verifactuEnviada.
   - Todos los endpoints de escritura/acción de controller/rest/ tienen @PreAuthorize o guard
     equivalente conectado a rolService.tienePermiso. Lista cualquier endpoint sin proteger.
   - restaurarBackup/deleteBackup validan la ruta contra backup.directory.
   - No queda 'unsafe-eval' en la CSP ni hash de password en logs.

2. Ejecuta ./mvnw verify (tests + gate de cobertura JaCoCo) y reporta el resultado.

3. Repasa la checklist de lanzamiento y marca cada punto con evidencia (fichero:línea o comando):
   [ ] TLS activo y SESSION_COOKIE_SECURE=true
   [ ] Restore de backup ensayado (fecha del ensayo en docs/RUNBOOK.md)
   [ ] Guard de arranque VeriFactu (enabled+prewww → falla)
   [ ] .env de producción con todos los secretos generados (openssl rand -base64 32), sin defaults
   [ ] admin.default.password definido y requiereCambioPassword operativo en el primer login
   [ ] Flyway en prod: baseline-on-migrate=false y última migración aplicada
   [ ] Logs persistidos en volumen y con rotación (logging.file.name)
   [ ] Validación VeriFactu contra el entorno de pruebas de AEAT hecha (o remisión deshabilitada
       explícitamente para el lanzamiento, con fecha límite anotada)

4. Entrega un informe GO / NO-GO con los puntos abiertos priorizados.
```

---

## Resumen de hallazgos → fase

| ID | Severidad | Hallazgo | Fase |
|----|-----------|----------|------|
| C1 | Crítico | Líneas de factura escribibles saltando inmutabilidad VeriFactu | 1 |
| A1 | Alto | Sin permisos granulares en la API REST | 2 |
| A2 | Alto | TLS pendiente, cookie de sesión sin Secure | 3 |
| A3 | Alto | Restore de backup jamás ejercitado; rutas sin validar | 3 |
| M1 | Medio | Endpoint AEAT de pruebas como default con QR de producción | 1 |
| M2 | Medio | Hash de contraseña en logs DEBUG | 4 |
| M3 | Medio | Auto-update Git inoperante/inconsistente | 4 |
| M4 | Medio | CSP con `unsafe-eval` | 4 |
| M5 | Medio | Deriva de esquema dev (ddl-auto=update, Flyway off) | 4 |
| M6 | Medio | Observabilidad mínima | 3 |
| B1-B5 | Bajo | Principal con hash, temp de credenciales, synchronized, higiene repo, emojis en logs | 4 |
