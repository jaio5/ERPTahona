# RUNBOOK — Operación de ERP Tahona en producción

Guía operativa del stack Docker Compose: `db` (MySQL 8.0) + `app` (Spring Boot)
+ `proxy` (Caddy, TLS). Pensada para ejecutarse desde el directorio del proyecto
en el servidor (donde están `docker-compose.yml` y `.env`).

---

## 1. Arranque y parada

### Primer arranque

```bash
cp .env.example .env          # y rellenar TODOS los secretos
docker compose config -q      # valida el compose y que no falte ningún secreto
docker compose up -d --build
docker compose ps             # esperar a que db y app estén "healthy"
```

La aplicación queda en `https://<ERP_DOMAIN>` (por defecto `https://localhost`).
La app **no** publica el puerto 8080 al host: todo entra por Caddy (80/443).

### TLS en LAN sin dominio público

Con `ERP_TLS_MODE=internal` (por defecto) Caddy emite certificados con su CA
interna. Para que los navegadores no avisen, instala la CA raíz en cada equipo
cliente:

```bash
# Extraer la CA raíz del volumen de Caddy
docker compose exec proxy cat /data/caddy/pki/authorities/local/root.crt > tahona-ca.crt
# Windows: importar en "Entidades de certificación raíz de confianza" (certmgr.msc)
# Firefox usa su propio almacén: Ajustes → Privacidad → Certificados → Importar
```

Con dominio público: `ERP_DOMAIN=erp.midominio.es` y `ERP_TLS_MODE=admin@midominio.es`
(activa Let's Encrypt; requiere 80/443 abiertos hacia este host).

### Parada / arranque ordinarios

```bash
docker compose stop           # parada (conserva contenedores)
docker compose start          # arranque
docker compose down           # elimina contenedores (los volúmenes SIEMPRE se conservan)
```

⚠️ Nunca uses `docker compose down -v`: `-v` borra los volúmenes, es decir,
**la base de datos, los backups y los certificados TLS**.

---

## 2. Rotación de secretos del `.env`

| Variable | ¿Rotable? | Procedimiento |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | Sí | Cambiar primero en MySQL, luego en `.env` (ver abajo) |
| `MYSQL_PASSWORD` | Sí | Cambiar primero en MySQL, luego en `.env` (ver abajo) |
| `ADMIN_DEFAULT_PASSWORD` | Sí | Solo afecta a la primera creación del admin; rotarla es inocua después |
| `CIFRADO_AES_KEY` | **NO** | Los datos cifrados quedarían ilegibles. No rotar sin plan de recifrado |
| `SECURITY_PBKDF2_SECRET` | **NO** | Las contraseñas de los usuarios dejarían de validar |
| `MAIL_PASSWORD` | Sí | Cambiar en el proveedor, actualizar `.env`, `docker compose up -d app` |
| `VERIFACTU_CERT_PASSWORD` | Con el certificado | Al renovar el .p12, actualizar fichero en `certs/` y `.env` |

Rotación de contraseñas de MySQL (root y de la app):

```bash
# 1. Cambiar la contraseña DENTRO de MySQL (el .env solo se lee al crear el volumen)
docker compose exec db mysql -uroot -p
  ALTER USER 'root'@'%' IDENTIFIED BY 'NUEVA_ROOT';
  ALTER USER 'root'@'localhost' IDENTIFIED BY 'NUEVA_ROOT';
  ALTER USER 'tahona'@'%' IDENTIFIED BY 'NUEVA_APP';
  FLUSH PRIVILEGES;
# 2. Actualizar MYSQL_ROOT_PASSWORD y MYSQL_PASSWORD en .env
# 3. Recrear la app para que tome la nueva credencial
docker compose up -d app
# 4. Verificar: docker compose ps (app healthy) y login en la web
```

Tras cualquier rotación: `chmod 600 .env` (o ACL equivalente) y comprobar que
`.env` no está en git (`git check-ignore .env` debe responder `.env`).

---

## 3. Backup

- **Automático**: cada día a las 02:00 (`backup.cron`), retención 30 días
  (`backup.retention.days`). Si falla, se envía email al administrador (si el
  email está configurado) y queda evento `BACKUP_FALLIDO` en auditoría.
- **Manual desde la web**: `Administración → Backups → Crear backup` (rol ADMIN).
- **Manual desde consola** (equivalente al de la app):

```bash
docker compose exec app sh -c 'mysqldump --single-transaction --routines --triggers \
  --add-drop-table -h db -u tahona -p tahona --result-file=/app/backups/backup_tahona_manual_$(date +%Y%m%d_%H%M%S).sql'
```

Los backups viven en el volumen `erp_backups` (`/app/backups` en el contenedor).
**Sácalos también fuera del host** (copia periódica a otro equipo/disco):

```bash
docker compose cp app:/app/backups ./backups-copia-$(date +%Y%m%d)
```

---

## 4. Restore

### 4.1 Desde la aplicación (caso normal)

1. Entrar con un usuario ADMIN → `Administración → Backups`.
2. En la fila del backup a restaurar, **teclear el nombre exacto del fichero**
   en el campo de confirmación y pulsar *Restaurar*.
3. La restauración queda registrada en auditoría (evento `RESTAURACION_BACKUP`,
   con usuario y fichero).
4. Después: pedir a todos los usuarios que cierren sesión y vuelvan a entrar, y
   revisar los últimos documentos (facturas/albaranes) contra papel.

Notas de seguridad: la app solo acepta ficheros `backup_*.sql` que estén dentro
del directorio de backups (defensa anti path-traversal), y la operación
sobrescribe las tablas incluidas en el dump (`--add-drop-table`).

### 4.2 Manual con docker exec (si la app no arranca)

```bash
# 1. Ver qué backups hay
docker compose exec app ls -lh /app/backups
# (si ni siquiera el contenedor app arranca, monta el volumen en uno auxiliar:)
docker run --rm -v erptahona_erp_backups:/backups alpine ls -lh /backups

# 2. Restaurar contra el MySQL del compose (pide MYSQL_PASSWORD del .env)
docker compose exec -T db sh -c 'mysql -utahona -p"$MYSQL_PASSWORD" tahona' \
  < ./copia-local-del-backup.sql
#    …o directamente desde el volumen de backups vía el contenedor app:
docker compose exec app sh -c 'mysql -h db -utahona -p tahona < /app/backups/backup_tahona_YYYYMMDD_HHMMSS.sql'

# 3. Levantar/reiniciar la app y comprobar salud
docker compose up -d app && docker compose ps
```

**Ensaya el restore periódicamente** (al menos tras cada cambio de esquema):
el test `BackupRestoreIntegrationTest` lo ensaya en CI contra un MySQL efímero,
pero un ensayo real en el servidor (restaurar el último backup en una BD de
prueba) sigue siendo la única prueba completa.

### 4.3 Registro de ensayos de restore

Anota aquí cada ensayo real (requisito de la checklist de lanzamiento: sin al
menos un ensayo con fecha, el restore se considera no probado).

| Fecha | Backup restaurado | Entorno | Resultado | Quién |
|-------|-------------------|---------|-----------|-------|
| _pendiente_ | — | — | — | — |

---

## 5. Actualización de versión

```bash
# 1. SIEMPRE backup antes de actualizar (web o comando de la sección 3)
# 2. Traer el código nuevo
git fetch && git checkout <tag-o-rama> && git pull
# 3. Reconstruir y levantar (Flyway aplica las migraciones al arrancar)
docker compose up -d --build app
# 4. Verificar
docker compose logs -f app        # esperar "Started ErpWebApplication"
docker compose ps                 # app "healthy"
```

Marcha atrás: `git checkout <tag-anterior>` + `docker compose up -d --build app`.
⚠️ Si la versión nueva ya aplicó migraciones de esquema, volver al binario
anterior puede no bastar: restaura también el backup previo a la actualización.

---

## 6. Cuando el healthcheck falla

`docker compose ps` muestra `app` como `unhealthy` (el healthcheck llama a
`/actuator/health`, que incluye la conexión a la BD).

Orden de diagnóstico:

```bash
# 1. ¿Qué dice la app?
docker compose logs --tail 200 app
# 2. ¿Qué devuelve exactamente el health? (desde dentro de la red)
docker compose exec app curl -s http://localhost:8080/actuator/health
# 3. ¿Está sana la BD?
docker compose ps db && docker compose logs --tail 100 db
# 4. ¿Disco lleno? (causa típica: backups + logs)
df -h && docker system df
# 5. ¿Memoria? (OOMKilled = true → subir memoria del host o límite del contenedor)
docker inspect erp-app --format '{{.State.OOMKilled}} {{.State.ExitCode}}'
```

Causas frecuentes:

| Síntoma en logs | Causa | Acción |
|---|---|---|
| `Communications link failure` | BD caída o aún arrancando | `docker compose restart db`, esperar healthy, luego `restart app` |
| `Access denied for user` | Rotación de secretos a medias | Revisar sección 2 (MySQL vs `.env` desincronizados) |
| `Flyway migration failed` | Migración incompatible con datos | NO reintentar en bucle; restaurar backup y analizar la migración |
| Arranques en bucle sin log claro | OOM | Ver punto 5 anterior |

Con un ADMIN autenticado, `https://<dominio>/actuator/health` muestra el detalle
(BD, disco) y `https://<dominio>/actuator/metrics` las métricas (JVM, pool
Hikari, HTTP). Ambos endpoints exigen rol ADMIN salvo el health sin detalles.

---

## 7. Detectar restart-loop y alerta simple

Un contenedor con `restart: unless-stopped` que muere nada más arrancar entra
en bucle de reinicios. Docker lo cuenta en `RestartCount`:

```bash
docker inspect erp-app --format '{{.RestartCount}} (estado: {{.State.Status}}, health: {{.State.Health.Status}})'
# También delata el bucle un uptime que nunca crece:
docker compose ps    # "Up 30 seconds" permanente = sospechoso
```

`RestartCount` se resetea al hacer `docker compose up -d` / recrear el
contenedor, así que compara entre revisiones, no en absoluto.

### Alerta por cron (sin infraestructura extra)

`/usr/local/bin/erp-watchdog.sh` (ajustar `MAIL_TO`; requiere `mailx` o similar):

```bash
#!/bin/sh
# Alerta si erp-app no está healthy o acumula reinicios
MAIL_TO="admin@example.com"
UMBRAL_REINICIOS=3

HEALTH=$(docker inspect erp-app --format '{{.State.Health.Status}}' 2>/dev/null || echo "no-existe")
RESTARTS=$(docker inspect erp-app --format '{{.RestartCount}}' 2>/dev/null || echo 0)

if [ "$HEALTH" != "healthy" ] || [ "$RESTARTS" -ge "$UMBRAL_REINICIOS" ]; then
    {
        echo "erp-app health=$HEALTH restarts=$RESTARTS en $(hostname) a $(date -Is)"
        docker compose -f /ruta/al/proyecto/docker-compose.yml logs --tail 50 app 2>/dev/null
    } | mail -s "[ALERTA] ERP Tahona: $HEALTH (restarts=$RESTARTS)" "$MAIL_TO"
fi
```

Crontab (cada 5 minutos):

```cron
*/5 * * * * /usr/local/bin/erp-watchdog.sh
```

### Alternativa: Uptime Kuma

Si prefieres panel con historial y notificaciones (email/Telegram), añade un
monitor HTTP(s) en Uptime Kuma apuntando a `https://<ERP_DOMAIN>/actuator/health`
con intervalo 60 s y palabra clave `UP`. Como el health sin autenticar no expone
detalles, es seguro monitorizarlo desde fuera. Un monitor caído + el watchdog de
cron cubren tanto la app colgada como el restart-loop.
