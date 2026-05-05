# ERP Tahona

Aplicacion de escritorio JavaFX + Spring Boot para gestion ERP con soporte de facturacion, trazabilidad y adaptacion progresiva a VeriFactu.

## Requisitos

- JDK 17 a JDK 23
- Maven 3.9+
- MySQL 8+ para uso normal

## Arranque local

1. Configura `JAVA_HOME` a un JDK compatible.
2. Si tienes MySQL local, define estas variables:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/tahona?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="<tu-password>"
```

3. Ejecuta la aplicacion:

```powershell
.\mvnw.cmd javafx:run
```

MySQL es obligatorio por defecto. Si quieres permitir un arranque temporal en H2 solo para diagnostico local, activa explicitamente:

```powershell
$env:ERP_FALLBACK_H2_ENABLED="true"
.\mvnw.cmd javafx:run
```

## Produccion

- Usa `SPRING_PROFILES_ACTIVE=prod`.
- No se permite fallback automatico a H2.
- Define siempre las variables obligatorias de `application-prod.properties`.
- Mantén `spring.jpa.hibernate.ddl-auto=validate`; en produccion no se permite `update`.
- Usa `.env.production.example` como plantilla, sin guardar secretos reales en Git.

Ejemplo:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:SPRING_DATASOURCE_URL="jdbc:mysql://mysql-host:3306/tahona?useSSL=true&requireSSL=true&serverTimezone=Europe/Madrid&characterEncoding=UTF-8&useUnicode=true"
$env:SPRING_DATASOURCE_USERNAME="erp_app"
$env:SPRING_DATASOURCE_PASSWORD="<tu-password>"
$env:CIFRADO_AES_KEY="<clave-base64-32-bytes>"
$env:SECURITY_PBKDF2_SECRET="<secreto-pbkdf2>"
.\mvnw.cmd javafx:run
```

## VeriFactu

- El soporte actual genera registros de alta y anulación con trazabilidad interna.
- La validacion XSD incluida es estructural e interna al proyecto.
- Antes de dar cumplimiento completo en España, debe sustituirse por el esquema oficial completo y validarse contra la especificacion final de AEAT/Orden HAC/1177/2024.

## Build

```powershell
.\mvnw.cmd -DskipTests compile
```

Si Maven falla por version de Java, revisa `JAVA_HOME`. El proyecto rechaza JDK 25 o superior porque la cadena actual de compilacion no es compatible.
