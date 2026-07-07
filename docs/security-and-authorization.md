# Seguridad y autorizacion

## Principios

- Produccion debe usar perfil `prod`.
- Los secretos se cargan desde variables de entorno; con Docker Compose, desde el fichero `.env` (plantilla: `.env.example`).
- `.env` esta ignorado por Git.
- No se debe compartir el usuario `admin` para trabajo diario.
- Cada usuario debe tener permisos minimos suficientes.

## Passwords

Variables relevantes:

- `ADMIN_DEFAULT_PASSWORD`: contrasena temporal inicial del administrador.
- `SECURITY_PBKDF2_SECRET`: secreto global para hashing de passwords.
- `SECURITY_PBKDF2_ITERATIONS`: iteraciones PBKDF2.
- `SECURITY_PBKDF2_HASH_WIDTH`: anchura de hash.

En produccion, `ADMIN_DEFAULT_PASSWORD` debe ser temporal. La aplicacion marca el usuario para cambio de contrasena cuando detecta una contrasena inicial heredada o insegura.

## Roles de sistema

Los roles iniciales se crean por migracion:

- `ADMIN`: acceso completo.
- `GESTOR`: operativa general sin administracion de usuarios ni backups.
- `VENDEDOR`: ventas, clientes, catalogo y almacen en modo comercial.
- `CONTABLE`: contabilidad, fiscalidad, compras, tesoreria, auditoria y VERI*FACTU operativo.
- `USUARIO`: consulta operativa basica.

## Modelo de permisos

Los permisos se guardan como JSON por modulo y accion:

```json
{
  "clientes": {
    "ver": true,
    "crear": true,
    "editar": true,
    "eliminar": false
  }
}
```

Modulos principales:

- `dashboard`
- `clientes`
- `proveedores`
- `articulos`
- `ventas`
- `compras`
- `almacen`
- `tesoreria`
- `contabilidad`
- `fiscal`
- `usuarios`
- `auditoria`
- `backup`
- `configuracion`
- `verifactu`

Acciones habituales:

- `ver`
- `crear`
- `editar`
- `eliminar`
- `exportar`
- `restaurar`
- `enviar`

## Estado de aplicacion de permisos

La matriz JSON de permisos **se aplica en el servidor** (no solo en la UI) desde julio de 2026. Hay dos barreras complementarias:

1. **Reglas por URL y rol Spring Security** (`SecurityConfig`):
   - Usuarios, roles, empresa, backups y auditoria requieren `ADMIN` o `ADMINISTRADOR`.
   - Fiscalidad, contabilidad, tesoreria y el ajuste de inventario requieren ademas `CONTABLE` o rol administrativo.
   - Las eliminaciones mediante CRUD generico requieren rol administrativo.
   - El resto de superficies web y API exige un usuario autenticado.
   - CSRF permanece activo para formularios y APIs basadas en sesion.

2. **Permisos granulares por modulo/accion** (`PermisoEvaluador`, bean `@permisos`):
   - Los endpoints REST llevan `@PreAuthorize("@permisos.puede('<modulo>', '<accion>')")` metodo a metodo (GET→ver, POST→crear, PUT→editar, DELETE→eliminar).
   - Las APIs genericas (`WebEntityController`, `WebChildEntityController`) resuelven el modulo de la entidad y deniegan por defecto si no hay mapeo.
   - Los modulos `facturas` y `facturas-compra` son de solo lectura en la API generica (inalterabilidad RRSIF); las lineas de facturas emitidas o enviadas a VeriFactu rechazan cualquier escritura.
   - Los ADMIN conservan acceso total.

Las pruebas negativas por modulo/accion estan en `RestApiPermisosTest` (403 sin permiso, operacion correcta con el).

## Recomendacion de operacion

- Usar `ADMIN` solo para configuracion inicial y mantenimiento.
- Dar `GESTOR` al responsable operativo.
- Dar `VENDEDOR` a personal de mostrador o ventas.
- Dar `CONTABLE` a asesoria/administracion.
- Revisar usuarios activos periodicamente.
- Cambiar contrasenas si hay rotacion de personal o sospecha de acceso indebido.
