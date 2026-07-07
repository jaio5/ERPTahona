# Arquitectura

## Stack

- Java 17 + Spring Boot 3.5.7
- Thymeleaf + Spring MVC para la interfaz web
- Spring Data JPA / Hibernate para persistencia
- MySQL 8 en producción
- Flyway para migraciones de esquema
- Maven Wrapper para build y pruebas
- JUnit 5 + Mockito para tests
- Alpine.js (build estándar) para componentes interactivos del frontend
- Bootstrap 5 para estilos

## Estructura principal

```text
src/main/java/alicanteweb/erp
  config/        Spring Security, CSP nonce, email, Verifactu properties, arranque de datos
  controller/
    dto/         DTOs de petición y respuesta
    rest/        Controladores REST (/api/**)
    web/         Controladores Thymeleaf (/web/**)
  entities/      Entidades JPA y enumeraciones de estado
  repository/    Repositorios Spring Data JPA
  service/       Lógica de negocio
  util/          Utilidades (CertificateUtils, HashUtils)

src/main/resources
  db/migration/  Migraciones Flyway (V0..V42)
  templates/     Vistas Thymeleaf
  static/        CSS, JS y recursos estáticos
```

## Perfiles

- `dev`: desarrollo local, sin MySQL obligatorio, datos de demo, sin validaciones estrictas.
- `prod`: MySQL obligatorio, Hibernate en modo `validate`, sin fallback H2, validaciones de secretos.
- `test`: pruebas automatizadas con H2 en memoria.

## Base de datos

En producción:

- Flyway aplica las 43 migraciones (V0–V42) al arrancar.
- Hibernate valida el esquema con `ddl-auto=validate`.
- No se debe usar `ddl-auto=update` en ningún entorno compartido.
- El backup resuelve la base de datos desde `SPRING_DATASOURCE_URL`.
- Las migraciones usan `IF NOT EXISTS` y `ON DUPLICATE KEY UPDATE` para ser idempotentes.

## Capas

```
Browser ──► Thymeleaf (/web/**)  ──► WebController/XxxWebController ──► Service ──► Repository ──► MySQL
         ──► REST JSON (/api/**) ──► XxxRestController               ──► Service ──► Repository
```

- Los controladores coordinan petición → servicio → modelo/vista. Sin lógica de negocio.
- Los servicios contienen las invariantes y flujos de dominio.
- Los repositorios son interfaces Spring Data JPA, más queries JPQL cuando necesario.
- `WebEntityController` y `WebChildEntityController` ofrecen CRUD genérico con reflexión para catálogos simples; evitan parte de los servicios de dominio y no deben usarse para entidades con invariantes complejas.

## Seguridad

- Autenticación por formulario, sesión HTTP con JSESSIONID.
- CSRF activo en formularios y APIs de sesión (token en `<meta name="csrf-token">`).
- PBKDF2 personalizado con iteraciones configurables (`SECURITY_PBKDF2_ITERATIONS`).
- CSP con nonce por petición generado en `CspNonceFilter`. Excepción conocida: `'unsafe-eval'` en `script-src`, requerido por el build estándar de Alpine.js (la migración al build CSP se abortó: 6 plantillas con expresiones inline).
- HSTS configurado para producción.
- Permisos granulares por módulo/acción (rol JSON) aplicados en servidor sobre la API REST vía `@PreAuthorize("@permisos.puede(...)")` con guards default-deny; tests negativos en `RestApiPermisosTest`. Ver [seguridad y autorización](security-and-authorization.md).
- Roles efectivos: `ADMIN`/`ADMINISTRADOR`, `CONTABLE`, `GESTOR`, `VENDEDOR`, `USUARIO`.

## Frontend Alpine.js

Las pantallas dinámicas usan el build estándar de Alpine.js. Se intentó migrar al build CSP-safe (`@alpinejs/csp`) para eliminar la excepción `'unsafe-eval'` de la CSP, pero se abortó: 6 plantillas dependen de expresiones inline no soportadas por ese build.

- La mayoría de componentes definen su estado e `init()` en `Alpine.data()`; conviene mantener este patrón al añadir pantallas nuevas.
- `[x-cloak]` en `app.css` evita parpadeo visual antes de que Alpine procese el DOM.
- Consecuencia de seguridad: `script-src` incluye `'unsafe-eval'`. Es la única desviación conocida de la CSP con nonce (ver [estado-actual](estado-actual.md)).

## Migraciones Flyway

| Rango | Contenido |
|-------|-----------|
| V0–V1 | Esquema base: proveedores, clientes, artículos, facturas, pedidos, almacenes |
| V2–V5 | Compliance: VeriFactu, auditoría, eventos de facturación, registros múltiples |
| V6–V9 | Secuencias de pedidos de compra, roles/permisos, password hardening, desactivar admin inseguro |
| V10–V11 | Módulos de producción, reparto y permisos |
| V12–V18 | Alérgenos, APPCC, tipo impositivo, tarifas, mermas, recepciones, fianzas |
| V19–V25 | Albaran estado, índices, optimistic lock, secuencias albaranes/facturas, presupuestos, pedidos venta |
| V26–V33 | Stock por almacén, costes de horneada, permisos adicionales, albaranes-venta, series, auto-unlock, XML longtext, firma en evidencia |
| V34–V42 | Config SIF, estado legal VeriFactu, entidades faltantes, cartera cobros/pagos, remesas SEPA + mandatos, PMP, plan PGC, permisos, series de orden de producción |

## Inventario (julio 2026)

- 43 migraciones SQL (V0..V42)
- Perfiles `dev`/`prod` con Flyway activo y `ddl-auto=validate`
- Componentes Alpine.js interactivos con build estándar (ver sección Frontend)
- 267 pruebas automatizadas, gate JaCoCo cumplido

## Scripts

- `scripts/load-env-file.ps1`: carga archivos `KEY=value`
- `scripts/check-production-env.ps1`: valida variables, Java, MySQL y VeriFactu
- `scripts/check-verifactu-production.ps1`: valida certificado y entorno VeriFactu
- `scripts/build-production.ps1`: compila y empaqueta
- `scripts/run-production.ps1`: carga entorno, valida y arranca
