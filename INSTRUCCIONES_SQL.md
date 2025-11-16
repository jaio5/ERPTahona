# Script SQL Mejorado para MySQL - Base de Datos Tahona ERP

## 📋 Descripción

Este archivo contiene el script SQL mejorado `tahona_mysql_mejorado.sql` generado automáticamente desde el archivo original `tahonaOriginal.sql`, optimizado específicamente para MySQL 8.0+.

## ✨ Mejoras Implementadas

### 1. **Tipos de Datos Optimizados**
   - `TEXT` → `VARCHAR(500)` para mejor rendimiento en índices
   - `INTEGER` → `INT` (sintaxis estándar MySQL)
   - `REAL` → `DECIMAL(15,2)` para precisión en valores monetarios

### 2. **Motor de Almacenamiento**
   - Todas las tablas usan `ENGINE=InnoDB` para soporte de transacciones y claves foráneas
   - Configuración `DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci` para soporte completo de caracteres especiales y emojis

### 3. **Configuración de Seguridad**
   - Guardado de configuración original antes de ejecutar
   - Desactivación temporal de verificaciones para importación rápida
   - Restauración automática de configuración al finalizar

### 4. **SQL Mode Estricto**
   - `ONLY_FULL_GROUP_BY`: Asegura consultas GROUP BY correctas
   - `STRICT_TRANS_TABLES`: Previene datos inválidos
   - `NO_ZERO_IN_DATE`: Evita fechas con ceros
   - `ERROR_FOR_DIVISION_BY_ZERO`: Manejo correcto de divisiones por cero

## 🚀 Instrucciones de Uso

### Opción 1: MySQL Workbench (Recomendado)

1. **Abrir MySQL Workbench**
2. **Conectarse a tu servidor MySQL**
3. **Abrir el archivo SQL**:
   - Menu: `File` → `Open SQL Script...`
   - Seleccionar: `tahona_mysql_mejorado.sql`
4. **Ejecutar el script**:
   - Click en el icono del rayo ⚡ (Execute)
   - O presionar `Ctrl + Shift + Enter`
5. **Esperar a que termine** (puede tardar unos minutos dependiendo del tamaño)
6. **Verificar**:
   - Refrescar el panel de esquemas
   - La base de datos `tahona` debería aparecer con todas sus tablas y datos

### Opción 2: Línea de Comandos MySQL

```bash
mysql -u root -p < tahona_mysql_mejorado.sql
```

O si prefieres ejecutarlo desde dentro del cliente MySQL:

```bash
mysql -u root -p
```

```sql
SOURCE D:/Programación/ERP/tahona_mysql_mejorado.sql;
```

### Opción 3: phpMyAdmin

1. Acceder a phpMyAdmin
2. Ir a la pestaña "Import"
3. Seleccionar el archivo `tahona_mysql_mejorado.sql`
4. Click en "Go"

## 📊 Contenido del Script

- **Tablas**: Aproximadamente 80+ tablas del sistema ERP
- **Datos**: Miles de registros con:
  - Artículos (productos de panadería)
  - Clientes
  - Proveedores
  - Facturas
  - Albaranes
  - Cuentas contables
  - Balances
  - Y mucho más...

## ⚙️ Requisitos

- MySQL 8.0 o superior (recomendado)
- MySQL 5.7 o superior (mínimo)
- Aproximadamente 50 MB de espacio libre en disco
- Permisos para crear bases de datos

## 🔧 Configuración Post-Instalación

Después de ejecutar el script, puedes:

1. **Verificar las tablas creadas**:
```sql
USE tahona;
SHOW TABLES;
```

2. **Ver algunos datos de ejemplo**:
```sql
SELECT * FROM Articulos LIMIT 10;
SELECT * FROM Clientes LIMIT 10;
```

3. **Verificar el engine de las tablas**:
```sql
SELECT 
    TABLE_NAME, 
    ENGINE, 
    TABLE_COLLATION 
FROM 
    information_schema.TABLES 
WHERE 
    TABLE_SCHEMA = 'tahona' 
LIMIT 10;
```

## 🛡️ Seguridad

El script incluye:
- Eliminación de la base de datos existente (si existe)
- Configuraciones temporales que se restauran al finalizar
- Modo SQL estricto para prevenir datos inválidos

**⚠️ ADVERTENCIA**: Este script **eliminará** cualquier base de datos existente llamada `tahona`. Asegúrate de hacer un backup si ya tienes datos en esa base de datos.

## 📝 Notas Adicionales

- El script está configurado para UTF-8 (utf8mb4), lo que permite almacenar cualquier carácter Unicode incluyendo emojis
- Los valores decimales tienen precisión de 15 dígitos con 2 decimales
- Todas las fechas están almacenadas como VARCHAR para mantener compatibilidad con el formato original

## 🐛 Solución de Problemas

### Error: "Access denied"
Asegúrate de tener permisos de creación de bases de datos:
```sql
GRANT ALL PRIVILEGES ON tahona.* TO 'tu_usuario'@'localhost';
FLUSH PRIVILEGES;
```

### Error: "Packet too large"
Aumenta el límite de paquetes en tu configuración MySQL:
```sql
SET GLOBAL max_allowed_packet=1073741824;
```

### Error de encoding
Asegúrate de que tu cliente MySQL esté configurado para UTF-8:
```sql
SET NAMES utf8mb4;
```

## 📞 Soporte

Para problemas o preguntas sobre la estructura de la base de datos, revisa el archivo original `tahonaOriginal.sql` o consulta la documentación del sistema ERP.

---

**Generado**: 2025-11-16  
**Versión**: 1.0  
**Compatible con**: MySQL 8.0+

