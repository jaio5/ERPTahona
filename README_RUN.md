# Arranque rápido

ERP web (Spring Boot + Thymeleaf) para la gestión de una tahona/panadería: facturación, clientes, proveedores, compras, ventas, producción, inventario, reparto, tesorería, contabilidad, auditoría, copias y evidencias VERI*FACTU en España.

Esta guía cubre el arranque. Para la operación completa (backups, restore, rotación de secretos, actualización, diagnóstico de salud) usa el **[RUNBOOK](docs/RUNBOOK.md)**, que es la guía canónica.

## Arranque con Docker (recomendado)

Solo necesitas Docker Desktop / Docker Engine con Compose. El stack levanta MySQL 8 + aplicación + proxy Caddy (TLS). La app no publica puertos: todo entra por HTTPS a través de Caddy.

```powershell
# 1. Copiar la plantilla de secretos y rellenarla (una sola vez)
Copy-Item .env.example .env
#    - ADMIN_DEFAULT_PASSWORD: contraseña inicial de admin (se exige cambiarla al entrar)
#    - CIFRADO_AES_KEY y SECURITY_PBKDF2_SECRET: generar con  openssl rand -base64 32

# 2. Validar el compose (comprueba que no falte ningún secreto)
docker compose config -q

# 3. Levantar todo (el esquema lo crea Flyway al arrancar)
docker compose up -d --build

# 4. Ver estado / logs
docker compose ps                 # esperar a que db y app estén "healthy"
docker compose logs -f app        # esperar "Started ErpWebApplication"
```

La aplicación queda en `https://<ERP_DOMAIN>` (por defecto `https://localhost`).

Notas:
- Con `ERP_TLS_MODE=internal` (por defecto) Caddy usa su CA interna; instala la CA raíz en los clientes para evitar avisos del navegador (ver [RUNBOOK](docs/RUNBOOK.md) §1). Con dominio público, `ERP_TLS_MODE=admin@midominio.es` activa Let's Encrypt.
- Backups, impresiones (declaraciones/PDFs) y logs persisten en volúmenes (`erp_backups`, `erp_impresiones`, `erp_logs`).
- **Nunca** uses `docker compose down -v`: `-v` borra los volúmenes (base de datos, backups y certificados TLS).
- Para VeriFactu con certificado real: copia el `.p12` a `certs/` y define `VERIFACTU_CERT_PATH=certs/<fichero>.p12` y su password en `.env`.

## Desarrollo local sin Docker

Requiere JDK 17–23 (`JAVA_HOME`) y MySQL 8 local. El perfil `dev` funciona como `prod`: Flyway activo y `ddl-auto=validate` (Hibernate solo valida, no crea ni altera tablas).

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/tahona?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="<tu-password>"
.\mvnw.cmd spring-boot:run
```

Si tu base local es anterior al cambio a `validate` (la creó Hibernate con `update`) o Flyway falla al validar, regenérala desde cero (se pierden los datos locales; haz backup antes si los necesitas):

```powershell
mysql -u root -p -e "DROP DATABASE IF EXISTS tahona; CREATE DATABASE tahona CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

Si tocas una entidad JPA, escribe la migración Flyway correspondiente en `src/main/resources/db/migration`; `validate` fallará hasta que lo hagas (ese es el objetivo).

## Guard de arranque en `prod`

El arranque con `SPRING_PROFILES_ACTIVE=prod` falla si:

- Faltan secretos reales de BD (`SPRING_DATASOURCE_*`), cifrado/passwords (`CIFRADO_AES_KEY`, `SECURITY_PBKDF2_SECRET`, `ADMIN_DEFAULT_PASSWORD`) o firma VeriFactu (`VERIFACTU_CERT_*`, `VERIFACTU_KEY_*`).
- `spring.jpa.hibernate.ddl-auto` no es `validate`.
- `ERP_FALLBACK_H2_ENABLED=true` (en `prod` el fallback H2 está siempre bloqueado).
- Se usan placeholders inseguros (`change-me`, `changeme`, `base64-32-byte-key`, `admin`, `password`, `root`).
- VeriFactu se intenta activar apuntando al sandbox de pruebas.

## Marco legal (VERI*FACTU)

- Real Decreto 1007/2023 (texto consolidado, modificado por el Real Decreto-ley 15/2025) y Orden HAC/1177/2024: los sistemas de facturación deben garantizar integridad, conservación, accesibilidad, legibilidad, trazabilidad e inalterabilidad, con huella/hash encadenado, XML UTF-8, QR y firma electrónica cuando corresponda.
- Plazos vigentes: **1 de enero de 2027** para obligados del artículo 3.1.a) (normalmente Impuesto sobre Sociedades) y **1 de julio de 2027** para el resto de obligados del artículo 3.1.
- La aplicación incluye la base técnica (registro de alta/anulación, huella encadenada, QR, XML oficial, firma con certificado, cliente SOAP AEAT), pero **la conformidad legal requiere validar XML, QR, firma y envío contra el entorno de pruebas oficial de AEAT** con certificado real antes de operar. Ver [VERI*FACTU](docs/verifactu.md) y [cumplimiento fiscal](docs/fiscal-compliance.md).

## Documentación

- [Índice de documentación](docs/index.md)
- [Estado actual](docs/estado-actual.md)
- [RUNBOOK de operación](docs/RUNBOOK.md)
- [Plan de lanzamiento](docs/plan-lanzamiento.md)
- [Seguridad y autorización](docs/security-and-authorization.md)
- [Manual de la aplicación](docs/manual-aplicacion.md)
- [Arquitectura](docs/architecture.md)
