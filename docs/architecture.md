# Arquitectura

## Stack

- JavaFX para interfaz de escritorio.
- Spring Boot para inyeccion, servicios y configuracion.
- Spring Data JPA/Hibernate para persistencia.
- MySQL 8 en produccion.
- Flyway para migraciones.
- Maven Wrapper para build.
- JUnit para tests.

## Estructura principal

```text
src/main/java/alicanteweb/erp
  config/        Configuracion Spring, seguridad, perfiles y arranque de datos
  controller/    Controladores JavaFX
  entities/      Entidades JPA
  repository/    Repositorios Spring Data
  service/       Logica de negocio
  tools/         Utilidades ejecutables internas
  ui/            Utilidades de dialogos
  util/          Utilidades generales

src/main/resources
  db/migration/  Migraciones Flyway
  ui/            FXML
  styles/        CSS JavaFX
```

## Perfiles

- `dev`: desarrollo local.
- `prod`: produccion, MySQL obligatorio, sin fallback H2 y con validaciones estrictas.
- `test`: pruebas automatizadas.

## Base de datos

En produccion:

- Flyway aplica migraciones.
- Hibernate valida el esquema con `ddl-auto=validate`.
- No se debe usar `ddl-auto=update`.
- El backup resuelve la base desde `SPRING_DATASOURCE_URL`.

## Capas

Los controladores deben limitarse a UI y coordinacion. La logica de negocio vive en servicios. El acceso a datos se realiza mediante repositorios. Las validaciones de seguridad se concentran en `AutenticacionService` y se aplican desde controladores y servicios donde corresponde.

## Scripts

- `scripts/load-env-file.ps1`: carga archivos `KEY=value`.
- `scripts/check-production-env.ps1`: valida variables, Java, MySQL y VERI*FACTU.
- `scripts/check-verifactu-production.ps1`: valida certificado y entorno VERI*FACTU.
- `scripts/build-production.ps1`: compila y empaqueta.
- `scripts/run-production.ps1`: carga entorno, valida y arranca.
