# Seguridad y autorizacion

## Principios

- Produccion debe usar perfil `prod`.
- Los secretos se cargan desde entorno o `.env.production.local`.
- `.env.production.local` esta ignorado por Git.
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

La matriz JSON existe en el modelo y `RolService` puede consultarla, pero actualmente no se aplica de forma general en los controladores y servicios. No debe considerarse todavía una barrera de seguridad efectiva.

Las protecciones efectivas actuales se basan principalmente en roles de Spring Security:

- Usuarios, empresa, backups y auditoria requieren `ADMIN` o `ADMINISTRADOR`.
- Fiscalidad y contabilidad requieren `ADMIN`, `ADMINISTRADOR` o `CONTABLE`.
- Las eliminaciones mediante CRUD generico requieren rol administrativo.
- El resto de superficies web y API exige un usuario autenticado.
- CSRF permanece activo para formularios y APIs basadas en sesion.

Pendiente: integrar los permisos JSON mediante seguridad de metodo y añadir pruebas negativas por modulo y accion.

## Recomendacion de operacion

- Usar `ADMIN` solo para configuracion inicial y mantenimiento.
- Dar `GESTOR` al responsable operativo.
- Dar `VENDEDOR` a personal de mostrador o ventas.
- Dar `CONTABLE` a asesoria/administracion.
- Revisar usuarios activos periodicamente.
- Cambiar contrasenas si hay rotacion de personal o sospecha de acceso indebido.
