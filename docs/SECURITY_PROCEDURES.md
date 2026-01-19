SECURITY PROCEDURES — Backups y Cifrado

Objetivo

Documentar procedimientos mínimos y seguros para:
- Generar y configurar la clave AES (para `CifradoService`).
- Realizar backups y restauraciones seguras (evitar exponer contraseñas en la línea de comandos).
- Requisitos operativos y recomendaciones (permisos, rotación, almacenamiento de secretos).

1) Clave AES (recomendado: Base64 32 bytes -> AES-256)

- En producción la aplicación exige que `cifrado.aes.key` sea una cadena Base64 que represente 16/24/32 bytes. Se recomienda 32 bytes (AES-256).
- Generar una clave segura (opciones):
  - Usando Java (desde un bean / REPL) llamar a `alicanteweb.erp.service.CifradoService.generarKeyAES()` y copiar la salida.
  - Usando OpenSSL en una máquina segura:

```bash
# Genera 32 bytes aleatorios y los imprime en Base64
openssl rand -base64 32
```

- Configurar la aplicación (properties / env):
  - NO almacenar secretos en el repositorio.
  - Preferible: usar gestor de secretos o variables de entorno. En application.properties (no recomendado en repo):

```properties
cifrado.aes.key=BASE64_GENERATED_KEY_HERE
```

- En entornos "prod" (profile `prod` o `production`) la aplicación fallará si la clave no está correctamente configurada.

2) Backups seguros (mysqldump)

Requisitos
- `mysqldump` y `mysql` deben estar disponibles en el PATH del servidor que realiza backups/restauraciones.
- La cuenta de BD usada para backups debe tener permisos mínimos necesarios (SELECT, LOCK TABLES o uso de --single-transaction según motor).

Evitar pasar contraseña en línea de comandos
- La aplicación ya usa `--defaults-extra-file` con un archivo temporal (mycnf) que contiene:

```
[client]
user=usuario
password=contraseña
```

y borra el archivo al terminar. En POSIX intenta fijar permisos 600. En Windows, proteger el fichero con ACLs.

Comando ejemplo (manual) en PowerShell:

```powershell
# Crear fichero temporal
$credFile = "C:\ruta\segura\mybackup.cnf"
@"[client]
user=tu_usuario
password=tu_password
"@ | Out-File -Encoding ascii $credFile
# Ejecutar mysqldump (no pasar -p en la línea)
mysqldump --defaults-extra-file=$credFile --single-transaction --routines --triggers --add-drop-table tu_basedatos --result-file=C:\backups\backup.sql
# Eliminar fichero de credencial
Remove-Item $credFile
```

En Linux / Bash (ejemplo):

```bash
credfile=$(mktemp /tmp/mycnf.XXXXXX)
cat > "$credfile" <<EOF
[client]
user=${DB_USER}
password=${DB_PASS}
EOF
chmod 600 "$credfile"
mysqldump --defaults-extra-file="$credfile" --single-transaction --routines --triggers --add-drop-table ${DB_NAME} --result-file="/var/backups/backup.sql"
rm -f "$credfile"
```

Permisos y protección
- Asegurar que el directorio de backups y los ficheros temporales estén accesibles solo por el usuario de sistema que ejecuta las tareas de backup (chmod/ACLs).
- Evitar incluir backups en repositorios o ubicaciones no cifradas.

Rotación y retención
- Mantener política de retención (por ejemplo, 30 días). La aplicación ya implementa eliminación basada en `backup.retention.days`.
- Realizar pruebas de restauración periódicas en un entorno aislado.

Registro y auditoría
- Registrar quién y cuándo realiza restauraciones. `BackupService` ya añade log con usuario OS y timestamp al restaurar.
- Guardar logs de auditoría en un sistema seguro y con retención separada.

3) Configuración segura de la aplicación

- No incluir secretos en el código fuente.
- Usar variables de entorno o gestores de secretos (Vault, AWS Secrets Manager, Azure Key Vault, etc.).
- Proteger `application.properties` con mecanismos adecuados o usar perfiles que carguen secretos desde el entorno.

4) Recomendaciones adicionales

- Revisar que los backups no incluyan datos sensibles en texto plano: si necesario, cifrar los dumps después de crearlos con GPG o AES con clave gestionada por servidor de secretos.
- Limitar acceso a servidores donde se realizan backups y restauraciones.
- Establecer alertas por fallos en backups automáticos.

5) Comandos de verificación desde la app
- Verificar disponibilidad de mysqldump: la aplicación expone `BackupService.verificarDisponibilidad()` (llamar desde la UI o logs iniciales).

6) Contacto y procedimientos de emergencia
- Equipo responsable: operaciones / admin DB.
- Procedimiento rápido de restauración: seguir el proceso documentado y probar primero en staging.


Fin del documento.

