# Guía de instalación en cliente (para el instalador)

Pasos que **tú** das para dejar ERP Tahona funcionando en el equipo del cliente.
Hay dos caminos; elige uno:

- **Camino A — Instalador `.exe`** (recomendado para un PC de mostrador Windows).
- **Camino B — Docker Compose directo** (para un servidor/LAN o si prefieres control manual).

Ambos usan Docker por debajo. El **único requisito** en el equipo del cliente es Docker.

---

## 0. Antes de ir (preparación en tu equipo)

- [ ] Repo actualizado (`git pull`, rama `main`).
- [ ] Docker Desktop arrancado en tu equipo (para construir la imagen/instalador).
- [ ] Decidir el **modo TLS** (ver §4) y el **dominio/IP** de acceso.
- [ ] (Opcional, si van a facturar a AEAT desde el día 1) tener el **certificado `.p12` real** de la empresa y su contraseña. Si no, se configura después.

---

## 1. Requisitos del equipo del cliente

- **Windows 10/11** (Camino A) o **cualquier SO con Docker Engine** (Camino B).
- **Docker Desktop** instalado (gratis para uso personal y pequeñas empresas):
  https://www.docker.com/products/docker-desktop
- **4 GB de RAM libres** como mínimo; disco con espacio para datos/backups.
- **Puertos 80 y 443 libres** en el equipo (los usa el proxy Caddy).

---

## 2. Camino A — Instalador `.exe` (recomendado)

### 2.1 Construir el instalador (en tu equipo, una vez)
Requiere Inno Setup 6 (`winget install --id JRSoftware.InnoSetup -e`) la primera vez.

```powershell
# Con Docker arrancado, desde la raíz del repo:
powershell -ExecutionPolicy Bypass -File .\deploy\desktop\build-installer.ps1
```
Resultado: `dist\ERP-Tahona-Setup-<versión>.exe`. Ese único fichero es lo que llevas.

### 2.2 Instalar en el equipo del cliente
1. Copia el `.exe` al equipo y ejecútalo.
2. El asistente:
   - Detecta **Docker Desktop** (si falta, ofrece descargarlo — instálalo antes de seguir).
   - Paso opcional de **VeriFactu**: elige *No configurar* / *Pruebas* / *Producción*.
     Si tienes el `.p12`, selecciónalo aquí; si no, deja *No configurar* y se hace luego (§5).
   - Instala **por usuario** (sin admin) y crea el **icono de escritorio "ERP Tahona"**.
3. Al terminar, marca **"Iniciar ERP Tahona ahora"** (o doble clic en el icono).
   - La **primera vez** genera el `.env` con secretos aleatorios y **muestra la contraseña inicial de `admin`** → **anótala**.
   - Cuando esté lista, abre el navegador en `https://localhost`.

> El icono de escritorio ejecuta `iniciar-erp.cmd`: arranca Docker si está apagado,
> levanta la app y abre el navegador. Para cerrar: **`parar-erp.cmd`** (conserva datos).

Continúa en §4 (puesta en marcha).

---

## 3. Camino B — Docker Compose directo (servidor/LAN)

En el equipo destino, con el repo copiado y Docker disponible:

```powershell
# 1. Crear el .env a partir de la plantilla
Copy-Item .env.example .env        # o .env.prod.example si vas a facturar a AEAT

# 2. Rellenar en .env (obligatorio):
#    ADMIN_DEFAULT_PASSWORD  -> contraseña inicial de admin (se cambia al 1er acceso)
#    CIFRADO_AES_KEY         -> openssl rand -base64 32
#    SECURITY_PBKDF2_SECRET  -> openssl rand -base64 32
#    MYSQL_ROOT_PASSWORD / MYSQL_PASSWORD -> contraseñas largas
#    ERP_DOMAIN / ERP_TLS_MODE -> ver §4

# 3. Validar que no falta ningún secreto
docker compose config -q

# 4. Levantar el stack (Flyway crea el esquema al arrancar)
docker compose up -d --build

# 5. Comprobar salud (esperar db y app "healthy")
docker compose ps
docker compose logs -f app         # esperar "Started ErpWebApplication"
```

Queda en `https://<ERP_DOMAIN>` (por defecto `https://localhost`).

> ⚠️ **Nunca** uses `docker compose down -v`: la `-v` borra los volúmenes (base de datos, backups y certificados TLS).

---

## 4. Puesta en marcha (común a A y B)

### 4.1 TLS / certificado — elige según el acceso
- **LAN sin dominio (por defecto, `ERP_TLS_MODE=internal`)**: Caddy usa su CA interna.
  El navegador avisará del certificado. Para quitar el aviso, instala la **CA raíz de Caddy**
  en los equipos que accedan:
  ```powershell
  # Exportar la CA raíz desde el contenedor del proxy:
  docker cp erp-caddy:/data/caddy/pki/authorities/local/root.crt .\caddy-root.crt
  # Instálala en "Entidades de certificación raíz de confianza" de cada PC cliente.
  ```
- **Dominio público real**: en `.env` pon `ERP_DOMAIN=erp.cliente.es` y
  `ERP_TLS_MODE=admin@cliente.es` (Let's Encrypt). Requiere que el dominio resuelva
  a este equipo y los puertos 80/443 abiertos. Certificado automático, sin avisos.

### 4.2 Primer acceso
1. Abre `https://<dominio>` y entra con **`admin`** + la contraseña inicial (la del asistente en A, o `ADMIN_DEFAULT_PASSWORD` en B).
2. La app **obliga a cambiar la contraseña** en el primer acceso → pon una nueva y anótala.

### 4.3 Datos de la empresa
En **Administración → Empresa**: NIF/CIF, razón social, dirección, y (si van a usar remesas SEPA) IBAN e identificador de acreedor.

Con esto la aplicación **ya funciona** para gestión, facturación interna, PDF, TPV, etc.

---

## 5. VeriFactu — solo si van a remitir facturas a la AEAT

La emisión de facturas a la AEAT está **bloqueada hasta configurar VeriFactu** (es
un requisito legal, no un fallo). Necesitas el **certificado real** y estos pasos:

1. En el `.env` (parte de `.env.prod.example`): `VERIFACTU_CERT_PATH`, `VERIFACTU_CERT_PASSWORD`,
   `VERIFACTU_AEAT_ENABLED=true`, `VERIFACTU_AEAT_ENDPOINT` (URL de **producción** `www1...`),
   e identidad del productor (`VERIFACTU_SISTEMA_*`). Copia el `.p12` a `certs/`.
2. Dentro de la app, en **Fiscal → Cumplimiento**:
   - Rellena **Datos del SIF** (nombre del sistema, versión, id de dispositivo, NIF emisor, modalidad = VERIFACTU).
   - Pulsa **Emitir declaración responsable**.
   - Pulsa **Iniciar funcionamiento VERI*FACTU**.
3. El checklist de esa pantalla debe quedar **todo en verde**.

> **Importante:** antes de activar la remisión real, **homologa con el certificado real
> contra el entorno de pruebas (prewww) de la AEAT**. El guard de arranque impide
> producción apuntando a un endpoint de pruebas.

Plazos legales: 1-ene-2027 (Impuesto de Sociedades) y 1-jul-2027 (resto).

---

## 6. Después de instalar

- **Backups**: la app hace copias (mysqldump) que persisten en volúmenes; se pueden lanzar
  desde **Administración → Backups**. Ensaya una **restauración** antes de confiar en ellas.
- **Guardar el `.env`**: contiene los secretos; si se pierden `CIFRADO_AES_KEY`/`SECURITY_PBKDF2_SECRET`
  quedarían datos ilegibles y contraseñas inválidas. Haz copia segura del `.env`.
- **Parar / arrancar**: `parar-erp.cmd` / doble clic en el icono (Camino A), o
  `docker compose stop` / `docker compose up -d` (Camino B). Los datos se conservan.
- **Actualizar la app**: entregar nueva imagen/`.exe`; el stack la carga y recrea el
  contenedor **conservando los volúmenes**.
- **Nunca** `docker compose down -v` en el equipo del cliente (borra datos).

---

## 7. Checklist go-live (marca antes de irte)

- [ ] Docker Desktop instalado y arrancando al iniciar sesión del cliente.
- [ ] Stack `healthy` (`docker compose ps`: db, app, caddy).
- [ ] `https://<dominio>` abre el login (CA raíz instalada o Let's Encrypt sin avisos).
- [ ] Login `admin` + **contraseña cambiada** y anotada por el cliente.
- [ ] **Empresa** configurada (NIF, razón social).
- [ ] `.env` **copiado a lugar seguro**.
- [ ] Backup manual creado y **restauración ensayada**.
- [ ] (Si aplica) VeriFactu configurado y checklist de Cumplimiento en verde.
- [ ] Explicado al cliente: cómo arrancar/parar y a quién llamar ante incidencias.

---

Guía canónica de operación (backups, restore, rotación de secretos, diagnóstico):
[docs/RUNBOOK.md](RUNBOOK.md).
