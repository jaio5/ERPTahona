# Documentacion del proyecto ERP Tahona

ERP Tahona es una aplicacion web con Spring Boot y Thymeleaf para gestion de una tahona/panaderia: ventas, compras, clientes, proveedores, articulos, almacen, caja, contabilidad operativa, auditoria, backups y preparacion VERI*FACTU.

## Guias principales

- [Estado actual](estado-actual.md): build, capacidades, cambios recientes y riesgos abiertos.
- [Manual de la aplicacion](manual-aplicacion.md): módulos, flujos operativos, rutas y TPV.
- [Referencia de API REST](api-reference.md): todos los endpoints REST con métodos, rutas y parámetros.
- [Arquitectura](architecture.md): estructura técnica, capas, migraciones y Alpine.js.
- [Seguridad y autorizacion](security-and-authorization.md): usuarios, roles, permisos y contraseñas.
- [VERI*FACTU](verifactu.md): configuración, flujo legal de inicio y validaciones previas.
- [Operacion diaria](operations.md): uso operativo, backups, facturación y mantenimiento.
- [Configuracion](configuration.md): variables de entorno y secretos.
- [Despliegue de produccion](production-deployment.md): instalación, variables, prechequeos y arranque.
- [Checklist de produccion](production-checklist.md): lista de verificación antes de usar datos reales.
- [Hoja de ruta](hoja-ruta.md): fases, entregables y definición objetiva de aplicación completada.
- [Auditoria de la aplicacion del 19 de junio de 2026](auditoria-aplicacion-2026-06-19.md): auditoría fechada y riesgos detectados.

## Capacidades de produccion

El proyecto dispone de:

- Perfil `prod` separado.
- MySQL obligatorio en produccion.
- Migraciones Flyway.
- Validacion de entorno antes de arrancar.
- Secretos fuera de Git mediante `.env.production.local`.
- Roles con restricciones para superficies administrativas y fiscales.
- Cambio obligatorio de contrasena inicial cuando corresponde.
- Copias de seguridad con base de datos resuelta desde la URL JDBC.
- Estado legal VERI*FACTU sin boton simple de activar/desactivar.

Antes de un despliegue real deben cerrarse los riesgos de la [auditoria actual](auditoria-aplicacion-2026-06-19.md), especialmente autorizacion fina, Docker, migraciones MySQL y stock por almacen.

El seguimiento de esos trabajos se realiza en la [hoja de ruta](hoja-ruta.md).

## Lo que no debe guardarse en Git

- `.env.production.local`
- Certificados `.p12`, `.pfx`, claves privadas o passwords.
- Backups reales.
- Logs con datos personales o fiscales.
- Exportaciones VERI*FACTU reales.

## Comandos basicos

```powershell
.\mvnw.cmd test
.\scripts\build-production.ps1
.\scripts\package-production.ps1
.\scripts\check-production-env.ps1
.\scripts\run-production.ps1
```

## Primer despliegue resumido

1. Instalar JDK 17, 21 o 23.
2. Crear base MySQL 8 y usuario de aplicacion.
3. Copiar `.env.production.example` a `.env.production.local`.
4. Rellenar secretos, certificado y rutas reales.
5. Ejecutar `.\scripts\check-production-env.ps1`.
6. Ejecutar `.\mvnw.cmd test`.
7. Ejecutar `.\scripts\build-production.ps1`.
8. Arrancar con `.\scripts\run-production.ps1`.
9. Configurar empresa, usuarios, roles, series, backups y VERI*FACTU.
