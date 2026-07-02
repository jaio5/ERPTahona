# Puesta en produccion

Este ERP gestiona facturacion, clientes, proveedores, compras, ventas, caja, contabilidad operativa, auditoria, copias y evidencias VeriFactu para una tahona/panaderia en Espana.

Documentacion ampliada:

- `docs/index.md`
- `docs/production-checklist.md`
- `docs/operations.md`
- `docs/configuration.md`
- `docs/security-and-authorization.md`
- `docs/verifactu.md`
- `docs/architecture.md`

## Punto actual

- La aplicacion es JavaFX + Spring Boot, con MySQL 8 en uso normal.
- Hay migraciones Flyway (`src/main/resources/db/migration`) y en `prod` debe usarse `spring.jpa.hibernate.ddl-auto=validate`.
- Hay soporte de evidencias VeriFactu: registro de alta, anulacion, hash/huella, hash anterior, XML generado, QR, firma si existe certificado y trazabilidad de eventos.
- Hay tablas y servicios de RGPD basico: solicitudes, consentimientos y accesos a datos.
- Hay auditoria de acciones y registro encadenado de eventos de facturacion/backups.
- La compilacion local requiere JDK 17 a JDK 23. JDK 25 esta bloqueado por el `maven-enforcer-plugin`.

## Marco legal que afecta a produccion

Fuentes revisadas el 8 de mayo de 2026:

- Real Decreto 1007/2023, texto consolidado, modificado por el Real Decreto-ley 15/2025: sistemas informaticos de facturacion deben garantizar integridad, conservacion, accesibilidad, legibilidad, trazabilidad e inalterabilidad.
- Orden HAC/1177/2024: especificaciones tecnicas de registros, XML UTF-8, huella/hash, firma electronica de registros cuando corresponda, codigo QR/frase en facturas, exportacion de registros y registro de eventos.
- Plazos vigentes del texto consolidado del RD 1007/2023:
  - 1 de enero de 2027 para obligados del articulo 3.1.a).
  - 1 de julio de 2027 para el resto de obligados del articulo 3.1.

## Bloqueos obligatorios antes de producir facturas reales

1. Instalar JDK 17, 21 o 23 y definir `JAVA_HOME`.
2. Crear una base MySQL 8 dedicada y usuario de aplicacion sin permisos administrativos globales.
3. Ejecutar con `SPRING_PROFILES_ACTIVE=prod`.
4. Copiar `.env.production.example` a `.env.production.local` y definir ahi todos los secretos con valores reales. El archivo local esta ignorado por Git.
5. Configurar un certificado valido para firma VeriFactu:
   - `VERIFACTU_CERT_PATH`
   - `VERIFACTU_CERT_PASSWORD`
   - `VERIFACTU_KEY_ALIAS`
   - `VERIFACTU_KEY_PASSWORD`
6. Ejecutar `.\scripts\check-verifactu-production.ps1` para validar certificado, alias y endpoint si `VERIFACTU_AEAT_ENABLED=true`.
7. Validar el XML, QR, firma y envio con el entorno de pruebas de AEAT antes de iniciar funcionamiento VERI*FACTU.
8. Verificar que la empresa configurada tiene NIF/CIF y datos fiscales. Despues, en la pantalla VERI*FACTU, usar `Probar AEAT` y `Iniciar VERI*FACTU`.
9. Probar exportacion de evidencias y eventos para un periodo completo.
10. Probar backup y restauracion en una copia de la base antes de operar.

## Guard de produccion

El arranque en `prod` falla si faltan valores reales para:

- Base de datos: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
- Cifrado y passwords: `CIFRADO_AES_KEY`, `SECURITY_PBKDF2_SECRET`, `ADMIN_DEFAULT_PASSWORD`.
- Firma VeriFactu: `VERIFACTU_CERT_PATH`, `VERIFACTU_CERT_PASSWORD`, `VERIFACTU_KEY_ALIAS`, `VERIFACTU_KEY_PASSWORD`.

Tambien falla si:

- `spring.jpa.hibernate.ddl-auto` no es `validate`.
- `ERP_FALLBACK_H2_ENABLED=true`.
- Se usan placeholders como `change-me`, `changeme`, `base64-32-byte-key`, `admin`, `password` o `root`.

## Comandos

Compilar:

```powershell
.\mvnw.cmd -DskipTests compile
```

Generar JAR de produccion:

```powershell
.\scripts\build-production.ps1
```

Tests:

```powershell
.\mvnw.cmd test
```

Arranque local con MySQL:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd javafx:run
```

Si MySQL local no esta disponible en perfiles no productivos, el lanzador reintenta con H2 en memoria. En `prod` este fallback esta siempre bloqueado.

Arranque de produccion:

```powershell
Copy-Item .env.production.example .env.production.local
# Edita .env.production.local con valores reales.
.\scripts\run-production.ps1
```

`run-production.ps1` carga automaticamente `.env.production.local` si existe y ejecuta el precheck VERI*FACTU antes de arrancar el JAR.

En el primer arranque con `prod`, si la migracion inicial ha creado el usuario `admin` con una password insegura heredada, la aplicacion la sustituye por `ADMIN_DEFAULT_PASSWORD`, deja el usuario activo y marca `requiere_cambio_password=true`.

## Riesgo pendiente

La aplicacion no debe venderse ni declararse conforme como producto VeriFactu completo hasta superar pruebas con los esquemas, endpoints y criterios oficiales de AEAT. El repositorio contiene una implementacion interna razonable para trazabilidad y evidencias, pero la conformidad legal depende de validar contra la especificacion oficial vigente y del certificado/entorno real del obligado tributario.
