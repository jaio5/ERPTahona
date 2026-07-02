# ERP Tahona

Aplicacion web Spring Boot + Thymeleaf para gestion ERP con soporte de facturacion, compras, ventas, clientes, proveedores, almacen, caja, auditoria, backups y preparacion VERI*FACTU.

## Requisitos

- JDK 17 a JDK 23
- Maven 3.9+
- MySQL 8+ para uso normal

## Documentacion

- [Indice de documentacion](docs/index.md)
- [Estado actual](docs/estado-actual.md)
- [Hoja de ruta hasta completar la aplicacion](docs/hoja-ruta.md)
- [Despliegue de produccion](docs/production-deployment.md)
- [Checklist de produccion](docs/production-checklist.md)
- [Operacion diaria](docs/operations.md)
- [Configuracion](docs/configuration.md)
- [Seguridad y autorizacion](docs/security-and-authorization.md)
- [VERI*FACTU](docs/verifactu.md)
- [Arquitectura](docs/architecture.md)
- [Manual de la aplicacion](docs/manual-aplicacion.md)
- [Auditoria actual (19 de junio de 2026)](docs/auditoria-aplicacion-2026-06-19.md)

Estado técnico validado:

- 209 pruebas, sin fallos.
- 50,27 % de cobertura de líneas.
- 29,92 % de cobertura de ramas.
- Gate JaCoCo mínimo: 50 % de líneas y 20 % de ramas.

## Arranque local

1. Configura `JAVA_HOME` a un JDK compatible.
2. Si tienes MySQL local, define estas variables:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/tahona?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="<tu-password>"
```

3. Ejecuta la aplicacion web:

```powershell
.\mvnw.cmd spring-boot:run
```

En desarrollo, si MySQL no esta disponible, la aplicacion puede reintentar con H2 en memoria para diagnostico local. Para forzarlo explicitamente:

```powershell
$env:ERP_FALLBACK_H2_ENABLED="true"
.\mvnw.cmd spring-boot:run
```

Para impedir ese fallback local:

```powershell
$env:ERP_FALLBACK_H2_ENABLED="false"
.\mvnw.cmd spring-boot:run
```

## Produccion

- Usa `SPRING_PROFILES_ACTIVE=prod`.
- No se permite fallback automatico a H2.
- Define siempre las variables obligatorias de `application-prod.properties`.
- Manten `spring.jpa.hibernate.ddl-auto=validate`; en produccion no se permite `update`.
- Usa `.env.production.example` como plantilla, sin guardar secretos reales en Git.

Ejemplo:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:SPRING_DATASOURCE_URL="jdbc:mysql://mysql-host:3306/tahona?useSSL=true&requireSSL=true&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&useUnicode=true"
$env:SPRING_DATASOURCE_USERNAME="erp_app"
$env:SPRING_DATASOURCE_PASSWORD="<tu-password>"
$env:CIFRADO_AES_KEY="<clave-base64-32-bytes>"
$env:SECURITY_PBKDF2_SECRET="<secreto-pbkdf2>"
.\mvnw.cmd spring-boot:run
```

## Estado de cumplimiento legal

- El proyecto contiene soporte local para registros de alta y anulacion VeriFactu, encadenamiento de huellas, evidencias, eventos de facturacion, auditoria, RGPD basico y exportacion operativa.
- El modo `prod` exige MySQL, migraciones Flyway, claves reales de cifrado/PBKDF2, certificado VeriFactu y desactiva el fallback H2.
- Segun el texto consolidado del Real Decreto 1007/2023, modificado por el Real Decreto-ley 15/2025, los plazos vigentes son:
  - Antes del 1 de enero de 2027 para obligados del articulo 3.1.a) del reglamento, normalmente contribuyentes del Impuesto sobre Sociedades.
  - Antes del 1 de julio de 2027 para el resto de obligados del articulo 3.1.
- La Orden HAC/1177/2024 exige, entre otros puntos, integridad, inalterabilidad, trazabilidad, conservacion, legibilidad, exportacion de registros, registro de eventos, XML UTF-8, huella/hash y firma electronica de registros cuando corresponda.
- Antes de considerar la aplicacion conforme de forma plena, hay que validar el XML, la firma, el QR y el envio contra las especificaciones oficiales y el entorno de pruebas de AEAT. La validacion XSD incluida es una ayuda interna, no una certificacion legal.

La configuracion SIF y el diagnostico de preparacion fiscal estan documentados en `docs/fiscal-compliance.md`.

## Build

```powershell
.\mvnw.cmd -DskipTests compile
```

Si Maven falla por version de Java, revisa `JAVA_HOME`. El proyecto rechaza JDK 25 o superior porque la cadena actual de compilacion no es compatible.

Para generar el JAR de produccion:

```powershell
.\scripts\build-production.ps1
```

Para arrancar el ultimo JAR generado con el perfil `prod`:

```powershell
.\scripts\run-production.ps1
```
