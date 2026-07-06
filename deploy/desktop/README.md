# ERP Tahona — despliegue como app de escritorio

Lanzador para que el cliente use la aplicación como un programa de escritorio: un
doble clic levanta el stack Docker (MySQL + app + proxy TLS) y abre el navegador.
El único requisito en su equipo es **tener Docker instalado**.

Hay dos formas de entregarlo:

- **Instalador `.exe` (recomendado)** — un único `ERP-Tahona-Setup.exe` con
  asistente que explica cómo funciona, detecta Docker, instala la app y deja un
  **icono de escritorio**. Ver [«Instalador de escritorio»](#instalador-de-escritorio).
- **Carpeta comprimida** — la carpeta autocontenida en `.zip`; el cliente
  descomprime y hace doble clic en `iniciar-erp.cmd`. Ver [«Flujo del cliente»](#flujo-del-cliente-usar-la-app).

## Contenido

| Fichero | Para quién | Qué hace |
|---|---|---|
| `iniciar-erp.cmd` / `.ps1` | Cliente | Arranca Docker si hace falta, genera `.env` (1ª vez), carga la imagen, levanta el stack, espera salud y abre el navegador |
| `parar-erp.cmd` / `.ps1` | Cliente | `docker compose stop` — para la app **conservando los datos** |
| `empaquetar-cliente.ps1` | Desarrollador | Construye la imagen, la exporta a `.tar` y arma la carpeta a entregar |
| `build-installer.ps1` | Desarrollador | Empaqueta y compila el instalador `ERP-Tahona-Setup.exe` (Inno Setup) |
| `installer\ERPTahona.iss` | Desarrollador | Guion de Inno Setup: páginas del asistente, iconos, detección de Docker |
| `installer\crear-icono.ps1` | Desarrollador | Genera `erp-icono.ico` (marca) sin dependencias externas |
| `build-exe.ps1` | Desarrollador (opcional) | Convierte los `.ps1` en `.exe` con icono (vía `ps2exe`) |

## Instalador de escritorio

Produce un único `ERP-Tahona-Setup.exe` que el cliente ejecuta como cualquier
instalador de Windows. El asistente:

1. Muestra una página **«Cómo funciona»** (requisito de Docker, qué pasa el primer
   arranque, contraseña inicial de `admin`, uso diario).
2. **Detecta Docker Desktop** y, si falta, ofrece abrir su página de descarga.
3. Instala la app **por usuario** (sin pedir administrador) y crea el **icono de
   escritorio «ERP Tahona»** más los accesos del menú Inicio (iniciar, detener,
   LEEME, desinstalar).

El icono de escritorio ejecuta `iniciar-erp.cmd`, que **arranca Docker si está
apagado, levanta la aplicación y abre el navegador** cuando está lista.

### Compilarlo (desarrollador)

Requiere **Inno Setup 6** una sola vez:

```powershell
winget install --id JRSoftware.InnoSetup -e
```

Luego, con Docker en marcha:

```powershell
# Construye la imagen, la exporta y compila el instalador en dist\
powershell -ExecutionPolicy Bypass -File .\deploy\desktop\build-installer.ps1

# Si el paquete (dist\erp-desktop) ya está construido y solo cambió el instalador:
powershell -ExecutionPolicy Bypass -File .\deploy\desktop\build-installer.ps1 -SkipPackage
```

Resultado: `dist\ERP-Tahona-Setup-<versión>.exe`. Ese único `.exe` es lo que
entregas al cliente.

## Flujo del desarrollador (preparar la entrega)

```powershell
# Desde la raíz del repo, con Docker en marcha:
powershell -ExecutionPolicy Bypass -File .\deploy\desktop\empaquetar-cliente.ps1
```

Genera `dist\erp-desktop\` con: `docker-compose.yml`, `deploy\Caddyfile`, los
lanzadores, `certs\` y **`erp-tahona-image.tar`** (la app pre-construida). Comprime
esa carpeta en un `.zip` y entrégala.

> Para un `.exe` con icono (opcional): `Install-Module ps2exe -Scope CurrentUser`
> y luego `.\deploy\desktop\build-exe.ps1 -IconPath ruta\al\icono.ico`.

## Flujo del cliente (usar la app)

1. Instalar **Docker Desktop** (una sola vez): https://www.docker.com/products/docker-desktop
2. Descomprimir la carpeta recibida.
3. Doble clic en **`iniciar-erp.cmd`**.
   - La 1ª vez muestra la **contraseña inicial de `admin`** (anotarla; se pedirá cambiarla).
   - Al terminar abre `https://localhost`. Si el navegador avisa del certificado
     (CA interna en modo local), aceptar continuar.
4. Para cerrar: doble clic en **`parar-erp.cmd`** (los datos se conservan).

## Notas de arquitectura

- **Secretos**: se generan aleatoriamente en el primer arranque y se guardan en `.env`.
  El lanzador **nunca los regenera** si `.env` ya existe (cambiarlos invalidaría los
  datos cifrados y las contraseñas). Guardar copia de `.env` es responsabilidad del despliegue.
- **Datos**: viven en volúmenes Docker (`mysql_data`, `erp_backups`, …) y sobreviven a
  apagados y a actualizaciones de imagen.
- **Actualizar la app**: entregar un nuevo `erp-tahona-image.tar`; el lanzador lo carga
  (`docker load`) y `docker compose up -d` recrea el contenedor conservando los volúmenes.
- **TLS**: por defecto `ERP_DOMAIN=localhost` con la CA interna de Caddy (avisos del
  navegador salvo que se instale su CA raíz). Para acceso por red/dominio, editar `.env`
  (`ERP_DOMAIN`, `ERP_TLS_MODE=tu-email@dominio` para Let's Encrypt).
- **Licencia de Docker**: Docker Desktop es gratuito para uso personal y empresas
  pequeñas; grandes empresas requieren suscripción. Alternativas sin licencia: Podman,
  Rancher Desktop.
