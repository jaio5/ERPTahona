# Auditoría de la aplicación ERP Tahona

**Fecha:** 19 de junio de 2026  
**Alcance:** estado local de la rama `main`, incluidos archivos modificados y no versionados  
**Tipo de revisión:** arquitectura, seguridad, datos, calidad, pruebas, despliegue y operación

## Resumen ejecutivo

ERP Tahona es una aplicación web monolítica de gestión empresarial para panadería construida con Java 17, Spring Boot 3.5.7, Thymeleaf, Spring Security, JPA, Flyway y MySQL.

El proyecto compila, empaqueta y supera el conjunto de pruebas que puede ejecutarse en el entorno local. Desde la auditoría del 18 de junio se han corregido riesgos importantes: CSRF está activo, existen restricciones por rol para superficies sensibles, se añadieron bloqueos de stock, control optimista, secuencias de albaranes y pruebas de migración MySQL.

El estado actual es apto para continuar desarrollo y validación preproductiva, pero no debe declararse listo para producción hasta cerrar los problemas de autorización fina, despliegue Docker, pruebas reales de migración y consistencia de inventario por almacén.

## Resultado

| Área | Estado | Observación |
| --- | --- | --- |
| Compilación | Correcto | 281 fuentes Java compiladas con Java 17 |
| Pruebas automáticas | Correcto con limitación | 209 pruebas, 0 fallos; MySQL/Testcontainers no se ejecutó |
| Cobertura | Gate alcanzado | 50,27 % de líneas y 29,92 % de ramas |
| Seguridad web | Mejorada | Autenticación, CSRF y restricciones de rol activas |
| Autorización fina | Riesgo alto | Los permisos JSON documentados no se aplican en controladores |
| Datos y migraciones | Parcial | Flyway en producción; validación MySQL no comprobada en esta ejecución |
| Inventario | Riesgo alto | Stock total global aunque los movimientos referencian almacenes |
| Despliegue Docker | No operativo como producción | Faltan secretos obligatorios y se usa conexión MySQL sin TLS |
| Operabilidad | Parcial | Hay scripts y backups, pero un precheck usa variables antiguas |

## Validaciones ejecutadas

Comando:

```powershell
.\mvnw.cmd clean verify
```

Resultado:

- `BUILD SUCCESS`.
- 281 clases fuente y 49 clases de prueba compiladas.
- 209 pruebas ejecutadas, sin fallos ni errores.
- JAR generado en `target/ERP-0.0.1.jar`.
- JaCoCo: 4.924 de 9.796 líneas cubiertas, 50,27 %.
- JaCoCo: 1.128 de 3.770 ramas cubiertas, 29,92 %.
- Los gates configurados de 50 % de líneas y 20 % de ramas se cumplen.

La clase `FlywayMySqlMigrationTest` intentó usar Testcontainers, pero Docker no estaba disponible por acceso denegado a `\\.\pipe\docker_engine`. Sus tres pruebas no se ejecutaron. El build continúa porque la clase aborta mediante una asunción.

También se ejecutó:

```powershell
.\mvnw.cmd dependency:analyze
```

El análisis finaliza correctamente, pero produce numerosos falsos positivos habituales con starters de Spring y algunas advertencias reales sobre dependencias directas/transitivas. Conviene usarlo como señal de mantenimiento, no como gate en su estado actual.

## Hallazgos críticos y altos

### A-01 — El modelo de permisos JSON no se aplica

**Evidencia**

- `Rol.permisos` almacena permisos por módulo y acción.
- `RolService.tienePermiso(...)` existe, pero no tiene consumidores en la aplicación.
- `SecurityConfig` decide por roles estáticos: `ADMIN`, `ADMINISTRADOR` y `CONTABLE`.
- El resto de `/api/**` y `/web/**` solo exige autenticación.
- `docs/security-and-authorization.md` afirma que menú y acciones comprueban permisos finos.

**Impacto**

Los roles personalizados y permisos como `crear`, `editar`, `eliminar`, `exportar`, `restaurar` o `enviar` no constituyen un control de seguridad efectivo. Un usuario autenticado puede acceder a módulos operativos que la matriz JSON podría marcar como denegados.

**Recomendación**

Implementar autorización de método con `@EnableMethodSecurity` y un componente central que evalúe usuario, módulo y acción. Aplicarla en servicios y acciones mutables; ocultar menús solo como complemento visual. Añadir pruebas negativas por rol y permiso.

### A-02 — `docker-compose.yml` no puede arrancar el perfil productivo

**Evidencia**

- El servicio `app` activa `prod`.
- `DatabaseStartupChecker` exige `CIFRADO_AES_KEY`, `SECURITY_PBKDF2_SECRET`, `ADMIN_DEFAULT_PASSWORD` y cuatro variables de certificado.
- `docker-compose.yml` no suministra esas variables.
- La URL JDBC del compose usa `useSSL=false` y credenciales con valores por defecto.

**Impacto**

El contenedor de aplicación debe fallar durante el arranque. Si se relajaran las comprobaciones, la configuración tampoco sería apropiada para producción.

**Recomendación**

Convertir el compose actual en configuración local o de demostración, o añadir `env_file`, secretos, volúmenes de certificado, healthcheck de aplicación y una URL MySQL con TLS para producción. No publicar el puerto MySQL fuera del host salvo necesidad expresa.

### A-03 — El inventario sigue teniendo una única existencia global

**Evidencia**

- `StockService` bloquea y actualiza `Articulo.stock`.
- `almacenId` solo se guarda en `MovimientoStock`.
- No existe una entidad de existencias por artículo y almacén.

**Impacto**

El sistema registra el origen o destino, pero no puede calcular stock disponible por almacén de forma fiable. Un movimiento en un almacén afecta al total global sin verificar la existencia local.

**Recomendación**

Crear `ArticuloAlmacen` con restricción única `(articulo_id, almacen_id)`, control de concurrencia y migración del stock actual. Mantener el total global como suma calculada o eliminarlo como fuente de verdad.

### A-04 — La validación MySQL/Flyway puede omitirse sin fallar el build

**Evidencia**

- `FlywayMySqlMigrationTest` contiene pruebas de base vacía, actualización y concurrencia.
- Si Docker no está disponible, `Assumptions.abort(...)` evita ejecutar la clase.
- Surefire informó 209 pruebas correctas y 0 omitidas aunque las tres pruebas MySQL no se ejecutaron.

**Impacto**

Un pipeline sin Docker puede aparecer en verde sin validar el esquema usado en producción.

**Recomendación**

Crear un perfil Maven o job CI obligatorio para migraciones MySQL que falle si no existe `MIGRATION_TEST_JDBC_URL` ni Docker. Separar claramente pruebas unitarias de pruebas de integración.

### A-05 — El CRUD genérico continúa evitando servicios de dominio

**Evidencia**

- `WebEntityController` y `WebChildEntityController` usan `EntityManager` y reflexión.
- Aunque ahora validan, protegen campos sensibles y limitan módulos de solo lectura, siguen persistiendo muchas entidades sin pasar por sus servicios.

**Impacto**

Las invariantes, auditoría y transiciones de estado pueden diferir entre la UI específica y la API genérica.

**Recomendación**

Retirar documentos, producción, inventario, fiscalidad y configuración del CRUD genérico. Mantenerlo únicamente para catálogos simples con DTO y allowlist explícita.

## Hallazgos medios

### M-01 — Importación bancaria frágil

`ExtractoBancarioRestController` divide CSV mediante una expresión regular, consulta el banco en cada fila y convierte importes a `double` antes de `BigDecimal`. No gestiona correctamente campos entrecomillados ni todos los formatos decimales.

Usar una biblioteca CSV, cargar y validar el banco una sola vez, parsear directamente a `BigDecimal` y limitar tamaño y número de filas.

### M-02 — No hay límites explícitos para archivos subidos

No se configuran `spring.servlet.multipart.max-file-size` ni `spring.servlet.multipart.max-request-size`.

Definir límites por entorno y validar extensión, MIME, contenido y número de registros.

### M-03 — Rutas arbitrarias en operaciones internas de backup

`BackupService.restaurarBackup` y `deleteBackup` aceptan rutas completas sin comprobar que estén dentro de `backup.directory`. Actualmente la UI solo expone creación y listado, pero el límite de seguridad del servicio es débil.

Normalizar la ruta, resolverla contra el directorio configurado y rechazar cualquier escape.

### M-04 — Script de preparación con nombres de variables obsoletos

`scripts/check-production-readiness.ps1` consulta `DB_URL`, `DB_USERNAME`, `AES_SECRET_KEY`, `PBKDF2_SECRET` y variables `VERIFACTU_KEYSTORE_*`, mientras la aplicación usa `SPRING_DATASOURCE_*`, `CIFRADO_AES_KEY`, `SECURITY_PBKDF2_SECRET` y `VERIFACTU_CERT_*`.

El script puede producir falsos fallos o una valoración incorrecta. Debe alinearse con `.env.production.example` y `DatabaseStartupChecker`.

### M-05 — Documentación de seguridad desalineada

La documentación afirma que se comprueban permisos por módulo y acción, pero el código aplica roles estáticos. Debe describirse el control realmente desplegado y marcar la matriz JSON como pendiente.

### M-06 — Recursos frontend duplicados

Coexisten `static/app.js` y `static/js/app.js`, además de `static/app.css` y `static/css/app.css`, junto con páginas Thymeleaf y una superficie SPA. Esto aumenta el coste de cambios y pruebas.

### M-07 — Datos de formularios en `localStorage`

`static/app.js` guarda borradores por módulo sin caducidad. En equipos compartidos pueden persistir datos comerciales.

Usar `sessionStorage`, caducidad y limpieza al cerrar sesión, o limitar el borrador a campos no sensibles.

### M-08 — Reempaquetado duplicado

El build ejecuta `spring-boot:repackage` dos veces. Debe conservarse una única ejecución.

### M-09 — Tests con configuración duplicada

`src/test/resources/application.properties` y `application-test.properties` definen bases y estrategias DDL distintas. Los tests observados usan la primera y no activan el perfil `test`.

### M-10 — CSP permite código inline y `unsafe-eval`

La política de seguridad de contenido restringe orígenes, pero mantiene `'unsafe-inline'` y `'unsafe-eval'` para scripts. Es una protección parcial frente a XSS.

Migrar scripts inline a archivos o nonces y retirar `unsafe-eval`.

## Aspectos positivos

- CSRF está activo también para APIs consumidas con sesión.
- Las superficies administrativas y fiscales tienen restricciones de rol.
- Contraseñas mediante PBKDF2, compatibilidad con BCrypt y secreto externo.
- El perfil productivo rechaza secretos ausentes o placeholders.
- `open-in-view=false` en todos los entornos relevantes.
- Flyway y `ddl-auto=validate` en producción.
- Bloqueo pesimista de artículos al modificar stock.
- `@Version` en artículos y documentos críticos.
- Numeración concurrente de albaranes mediante tabla de secuencia.
- DTOs y paginación incorporados en varias APIs recientes.
- Gates de cobertura activos en Maven.
- Backups, auditoría, RGPD, APPCC y evidencias VeriFactu están modelados.

## Prioridades recomendadas

1. Aplicar permisos reales por módulo y acción.
2. Corregir o reclasificar el despliegue Docker.
3. Hacer obligatoria la prueba MySQL/Flyway en CI.
4. Diseñar stock por almacén.
5. Limitar el CRUD genérico a catálogos simples.
6. Corregir el importador bancario y límites de subida.
7. Alinear scripts y documentación con la configuración real.
8. Consolidar frontend y elevar cobertura de flujos críticos.

## Limitaciones de esta auditoría

No se realizó:

- Prueba de restauración real de backup.
- Prueba contra servicios AEAT.
- Certificación legal o fiscal.
- Prueba de carga, concurrencia completa o pentest.
- Validación visual en todos los navegadores.
- Prueba MySQL/Flyway, por no estar disponible Docker.
