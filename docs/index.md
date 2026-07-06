# Documentación del proyecto ERP Tahona

ERP Tahona es una aplicación web con Spring Boot y Thymeleaf para la gestión de una tahona/panadería: ventas, compras, clientes, proveedores, artículos, almacén, producción, reparto, tesorería, contabilidad, fiscalidad (VERI*FACTU, Modelo 347, libros de IVA), auditoría y backups.

## Guías principales

- [Estado actual](estado-actual.md): build, capacidades y pendientes antes del lanzamiento.
- [RUNBOOK de operación](RUNBOOK.md): despliegue con Docker + Caddy, backups, restore, actualización de versión. **Es la guía canónica de despliegue y operación.**
- [Plan de lanzamiento](plan-lanzamiento.md): fases, checklist GO/NO-GO y decisión VeriFactu.
- [Manual de la aplicación](manual-aplicacion.md): módulos, flujos operativos, rutas y TPV.
- [Referencia de API REST](api-reference.md): endpoints REST con métodos, rutas y parámetros.
- [Arquitectura](architecture.md): estructura técnica, capas, migraciones y frontend.
- [Seguridad y autorización](security-and-authorization.md): usuarios, roles, permisos granulares y contraseñas.

## Fiscalidad

- [VERI*FACTU](verifactu.md): configuración, flujo legal de inicio y validaciones previas.
- [Arquitectura VeriFactu](verifactu-arquitectura.md): registro de alta/anulación, hash encadenado, firma, QR y remisión AEAT.
- [Cumplimiento fiscal SIF](fiscal-compliance.md): referencias normativas (RD 1007/2023, Orden HAC/1177/2024) y plazos.
- [Revisión ERP España 2026-07-02](revision-erp-espana-2026-07-02.md): módulos implementados para operar en España y pendiente A1 (certificado + validación AEAT).

## Despliegue resumido

El despliegue soportado es **Docker Compose** (MySQL + aplicación + proxy Caddy con TLS). Ver [README_RUN.md](../README_RUN.md) para el arranque rápido y el [RUNBOOK](RUNBOOK.md) para la operación completa:

```powershell
Copy-Item .env.example .env    # rellenar secretos (openssl rand -base64 32)
docker compose up -d --build
```

## Lo que no debe guardarse en Git

- `.env` y cualquier fichero con secretos reales.
- Certificados `.p12`, `.pfx`, claves privadas o passwords.
- Backups reales.
- Logs con datos personales o fiscales.
- Exportaciones VERI*FACTU reales.

## Comandos básicos

```powershell
.\mvnw.cmd verify        # tests + gate de cobertura JaCoCo
docker compose up -d --build
docker compose logs -f app
```
