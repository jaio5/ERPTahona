# Arquitectura

## Stack

- Java 17 + Spring Boot 3.5.7
- Thymeleaf + Spring MVC para la interfaz web
- Spring Data JPA / Hibernate para persistencia
- MySQL 8 en producción
- Flyway para migraciones de esquema
- Maven Wrapper para build y pruebas
- JUnit 5 + Mockito para tests
- Alpine.js CSP (`@alpinejs/csp`) para componentes interactivos del frontend
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
  db/migration/  Migraciones Flyway (V0..V27)
  templates/     Vistas Thymeleaf
  static/        CSS, JS y recursos estáticos
```

## Perfiles

- `dev`: desarrollo local, sin MySQL obligatorio, datos de demo, sin validaciones estrictas.
- `prod`: MySQL obligatorio, Hibernate en modo `validate`, sin fallback H2, validaciones de secretos.
- `test`: pruebas automatizadas con H2 en memoria.

## Base de datos

En producción:

- Flyway aplica las 27 migraciones (V0–V27) al arrancar.
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
- CSP con nonce por petición generado en `CspNonceFilter`; Alpine.js usa el build CSP-safe.
- HSTS configurado para producción.
- `@PreAuthorize` en servicios críticos (usuarios, backups, asientos contables).
- Roles efectivos: `ADMIN`/`ADMINISTRADOR`, `CONTABLE`, `GESTOR`, `VENDEDOR`, `USUARIO`.

## Frontend Alpine.js

Las pantallas dinámicas usan `@alpinejs/csp`, que no permite `new Function()`. Restricciones:

- **No soportado en directivas HTML**: asignaciones directas (`@click="tab='x'"`)
- **Soportado**: llamadas a métodos (`@click="setTab('x')"`), comparaciones (`x-show="isTab('x')"`), ternarios, `||`, `&&`
- Todos los componentes Alpine definen su estado e `init()` en `Alpine.data()`, sin `x-init` en el HTML.
- `[x-cloak]` en `app.css` evita parpadeo visual antes de que Alpine procese el DOM.

## Migraciones Flyway

| Rango | Contenido |
|-------|-----------|
| V0–V1 | Esquema base: proveedores, clientes, artículos, facturas, pedidos, almacenes |
| V2–V5 | Compliance: VeriFactu, auditoría, eventos de facturación |
| V6–V9 | Secuencias de pedidos de compra, roles, password hardening, estado legal VeriFactu |
| V10–V11 | Módulos de producción, reparto y permisos |
| V12–V18 | Alérgenos, APPCC, tipo impositivo, tarifas, mermas, recepciones, fianzas |
| V19–V25 | Albaran estado, índices, optimistic lock, secuencias albaranes/facturas, presupuestos, pedidos venta |
| V26–V27 | Stock por almacén (articulo_almacen), costes reales de producción (horneadas) |

## Inventario (junio 2026)

- ~285 clases Java de producción
- 62 controladores (38 web, ~16 REST, 8 otros)
- ~71 servicios
- ~80 entidades y enumeraciones
- ~100 plantillas HTML
- 27 migraciones SQL (V0..V27)
- 6 componentes Alpine.js interactivos

## Scripts

- `scripts/load-env-file.ps1`: carga archivos `KEY=value`
- `scripts/check-production-env.ps1`: valida variables, Java, MySQL y VeriFactu
- `scripts/check-verifactu-production.ps1`: valida certificado y entorno VeriFactu
- `scripts/build-production.ps1`: compila y empaqueta
- `scripts/run-production.ps1`: carga entorno, valida y arranca
