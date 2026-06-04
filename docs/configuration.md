# Configuracion

La configuracion de produccion se carga desde variables de entorno. En Windows se recomienda usar `.env.production.local`, creado a partir de `.env.production.example`.

## Archivo local

```powershell
Copy-Item .env.production.example .env.production.local
notepad .env.production.local
```

No guardes `.env.production.local` en Git.

## Base de datos

| Variable | Uso |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC de MySQL. |
| `SPRING_DATASOURCE_USERNAME` | Usuario de aplicacion. |
| `SPRING_DATASOURCE_PASSWORD` | Password del usuario MySQL. |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` | Maximo de conexiones del pool. |

Ejemplo:

```properties
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/tahona?useSSL=true&requireSSL=true&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&useUnicode=true
SPRING_DATASOURCE_USERNAME=erp_app
SPRING_DATASOURCE_PASSWORD=...
```

## Seguridad

| Variable | Uso |
| --- | --- |
| `CIFRADO_AES_KEY` | Clave de cifrado de datos sensibles. |
| `CIFRADO_AES_USE_PBKDF2` | Derivar la clave desde password/salt. |
| `CIFRADO_AES_PBKDF2_SALT` | Salt si se usa PBKDF2 para cifrado. |
| `CIFRADO_AES_PBKDF2_ITERATIONS` | Iteraciones PBKDF2 para cifrado. |
| `SECURITY_PBKDF2_SECRET` | Secreto para hash de passwords. |
| `SECURITY_PBKDF2_ITERATIONS` | Iteraciones PBKDF2 para passwords. |
| `SECURITY_PBKDF2_HASH_WIDTH` | Anchura del hash. |
| `ADMIN_DEFAULT_PASSWORD` | Password temporal inicial del administrador. |

Los scripts de prechequeo rechazan placeholders evidentes y valores demasiado cortos.

## VERI*FACTU

| Variable | Uso |
| --- | --- |
| `VERIFACTU_CERT_PATH` | Ruta al certificado PKCS12. |
| `VERIFACTU_CERT_PASSWORD` | Password del almacen. |
| `VERIFACTU_KEY_ALIAS` | Alias de la clave dentro del certificado. |
| `VERIFACTU_KEY_PASSWORD` | Password de la clave. |
| `VERIFACTU_AEAT_ENABLED` | Habilita remision AEAT. |
| `VERIFACTU_AEAT_ENDPOINT` | Endpoint AEAT del entorno validado. |

## Backups y logs

| Variable | Uso |
| --- | --- |
| `ERP_LOG_FILE` | Ruta del log principal. |
| `ERP_BACKUP_DIRECTORY` | Directorio de backups. |
| `ERP_BACKUP_RETENTION_DAYS` | Dias de retencion. |

## Autocompletado de clientes

| Variable | Uso |
| --- | --- |
| `CLIENTES_AUTOCOMPLETAR_ENABLED` | Activa/desactiva el servicio externo. |
| `CLIENTES_AUTOCOMPLETAR_ENDPOINT` | Endpoint del proveedor. |
| `CLIENTES_AUTOCOMPLETAR_API_KEY` | API key. |
| `CLIENTES_AUTOCOMPLETAR_TIMEOUT_SECONDS` | Timeout. |
| `CLIENTES_AUTOCOMPLETAR_CIF_PARAM` | Nombre del parametro CIF. |
| `CLIENTES_AUTOCOMPLETAR_NOMBRE_PARAM` | Nombre del parametro de busqueda por nombre. |

## Actualizaciones

| Variable | Uso |
| --- | --- |
| `APP_UPDATE_ENABLED` | Activa integracion de actualizacion. |
| `APP_UPDATE_REMOTE` | Remoto Git. |
| `APP_UPDATE_BRANCH` | Rama de actualizacion. |
| `APP_UPDATE_TIMEOUT_SECONDS` | Timeout. |
| `APP_UPDATE_WORKDIR` | Directorio de trabajo alternativo. |

## Validacion

```powershell
.\scripts\check-production-env.ps1
```

El prechequeo valida Java, base de datos, secretos, carpetas, certificado y configuracion VERI*FACTU.
