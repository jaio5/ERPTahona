# ERP PANADERIA TAHONA - GUIA RAPIDA

## Estado: OPERATIVO

---

## INICIO RAPIDO

### Opcion 1: Test completo (RECOMENDADO)
```
TEST_RAPIDO.bat
```
Este script:
- Verifica Java instalado
- Verifica Maven instalado
- Verifica MySQL corriendo
- Verifica base de datos tahona
- Arranca la aplicacion

### Opcion 2: Arranque directo
```
ARRANCAR_APP.bat
```

### Opcion 3: Solo verificar archivos
```
VERIFICAR_APP.bat
```

---

## CREDENCIALES

```
Usuario:    admin
Contrasena: admin
```

---

## REQUISITOS

- [x] Java 17 o superior
- [x] Maven 3.6 o superior
- [x] MySQL 8.0 corriendo en puerto 3306
- [x] Base de datos "tahona" con datos

---

## SOLUCION DE PROBLEMAS

### MySQL no arranca
```bash
net start MySQL80
```

### Base de datos no existe
```sql
CREATE DATABASE tahona;
-- Importar: basesdedatos\definitivo\tahonaerp.sql
```

### Error de compilacion
```bash
mvn clean install -Dmaven.test.skip=true
```

---

## CODIFICACION UTF-8

El proyecto ahora maneja correctamente:
- Acentos (á, é, í, ó, ú)
- Enyes (ñ)
- Caracteres especiales

Cambios aplicados:
- [x] Maven configurado con UTF-8
- [x] Spring Boot configurado con UTF-8
- [x] MySQL configurado con UTF-8
- [x] Archivos BAT sin caracteres especiales

Ver detalles en: `CORRECCION_UTF8.txt`

---

## MODULOS DISPONIBLES

- Clientes
- Articulos
- Proveedores
- Facturas
- Albaranes
- Presupuestos
- Pedidos
- Almacenes
- Caja
- Contabilidad
- Usuarios
- Y mas...

---

## DOCUMENTACION COMPLETA

- `CORRECCION_UTF8.txt` - Correccion de codificacion
- `RESUMEN_REPARACION.txt` - Resumen de reparacion
- `GUIA_PRUEBA_RAPIDA.txt` - Guia de pruebas
- `INDICE_DOCUMENTACION.txt` - Indice completo

---

## SOPORTE

Si tienes problemas:
1. Lee: `CORRECCION_UTF8.txt`
2. Lee: `COMO_VER_ERRORES.md`
3. Ejecuta: `VERIFICAR_APP.bat`

---

**Estado**: COMPLETAMENTE OPERATIVO
**Ultima actualizacion**: 08/01/2026
**Version**: 0.0.1

