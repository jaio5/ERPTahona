# Documentacion del proyecto ERP Tahona

ERP Tahona es una aplicacion de escritorio JavaFX con Spring Boot para gestion de una tahona/panaderia: ventas, compras, clientes, proveedores, articulos, almacen, caja, contabilidad operativa, auditoria, backups y preparacion VERI*FACTU.

## Guias principales

- [Despliegue de produccion](production-deployment.md): instalacion, variables, prechequeos y arranque.
- [Checklist de produccion](production-checklist.md): lista de verificacion antes de usar datos reales.
- [Operacion diaria](operations.md): uso operativo, backups, facturacion y mantenimiento.
- [Configuracion](configuration.md): variables de entorno y secretos.
- [Seguridad y autorizacion](security-and-authorization.md): usuarios, roles, permisos y contrasenas.
- [VERI*FACTU](verifactu.md): configuracion, flujo legal de inicio y validaciones previas.
- [Arquitectura](architecture.md): estructura tecnica del proyecto.

## Estado operativo

El proyecto queda preparado tecnicamente para producir con:

- Perfil `prod` separado.
- MySQL obligatorio en produccion.
- Migraciones Flyway.
- Validacion de entorno antes de arrancar.
- Secretos fuera de Git mediante `.env.production.local`.
- Roles y permisos por modulo.
- Cambio obligatorio de contrasena inicial cuando corresponde.
- Copias de seguridad con base de datos resuelta desde la URL JDBC.
- Estado legal VERI*FACTU sin boton simple de activar/desactivar.

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
