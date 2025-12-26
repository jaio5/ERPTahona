# 📦 Guía de Instalación - ERP Tahona

## Requisitos del Sistema

### Software Necesario

| Componente | Versión Mínima | Descarga |
|------------|----------------|----------|
| **Java JDK** | 17 o superior | https://adoptium.net/ |
| **MySQL** | 8.0+ | https://dev.mysql.com/downloads/ |
| **Maven** | 3.8+ | Incluido (mvnw) |

### Hardware Recomendado

- **RAM:** 8 GB mínimo
- **Disco:** 500 MB para aplicación
- **Procesador:** Intel Core i5 o equivalente

---

## Instalación Paso a Paso

### 1. Verificar Java

```bash
java -version
```

**Debe mostrar:** `openjdk version "17.x.x"` o superior

**Si no está instalado:**
1. Descargar de https://adoptium.net/
2. Instalar siguiendo el asistente
3. Reiniciar terminal

### 2. Instalar MySQL

```bash
mysql --version
```

**Si no está instalado:**
1. Descargar MySQL Installer
2. Instalar MySQL Server
3. Configurar contraseña de root
4. Iniciar servicio:
   ```bash
   net start MySQL
   ```

### 3. Crear Base de Datos

```sql
-- Conectar a MySQL
mysql -u root -p

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS tahona 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verificar
SHOW DATABASES;

-- Salir
EXIT;
```

### 4. Configurar Datos de Empresa

**Opción A: Script automático (RECOMENDADO)**
```bash
cd C:\Programación\segundoJAVA\springboot\demo\ERPTahona
configurar_bd.bat
```

**Opción B: Manual**
```bash
mysql -u root -p tahona < basesdedatos\agregar_estado_facturas_y_empresa.sql
```

Este script:
- ✅ Crea tabla `empresa_config` con datos de GRUPO BABO
- ✅ Añade campos de estado a facturas
- ✅ Configura campos Verifactu
- ✅ Inserta datos del albarán proporcionado

### 5. Verificar Configuración

```sql
mysql -u root -p tahona

-- Ver datos de GRUPO BABO
SELECT * FROM empresa_config WHERE activo = TRUE;

-- Debe mostrar:
-- nombre_empresa: GRUPO BABO, S.Coop.V.L.
-- cif: F54059985
-- telefono: 965 68 73 58

-- Ver estructura de facturas
DESCRIBE facturas;

-- Debe incluir: estado, verifactu_enviada, fecha_emision_verifactu
```

### 6. Configurar Aplicación

Editar `src/main/resources/application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?useSSL=false&serverTimezone=Europe/Madrid
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA_AQUI
```

### 7. Compilar Proyecto

```bash
.\mvnw clean compile
```

**Si hay errores:**
```bash
.\mvnw clean install -DskipTests
```

### 8. Primera Ejecución

```bash
.\arrancar.bat
```

**Debe mostrar:**
```
================================================
      ERP TAHONA - GRUPO BABO
================================================
✓ Base de datos conectada
✓ Empresa configurada: GRUPO BABO, S.Coop.V.L.
✓ CIF: F54059985
✓ Verifactu: Habilitado
================================================
```

---

## Configuración de Verifactu (Opcional)

### Para Producción

1. **Obtener certificado digital:**
   - Solicitar a FNMT (https://www.sede.fnmt.gob.es/)
   - Tipo: Certificado de persona jurídica
   - Para: GRUPO BABO (CIF F54059985)

2. **Exportar a PKCS12:**
   - Formato: `.p12`
   - Guardar con contraseña segura

3. **Copiar al proyecto:**
   ```
   src/main/resources/certs/mi_certificado.p12
   ```

4. **Configurar:**
   ```properties
   verifactu.keystore.path=/certs/mi_certificado.p12
   verifactu.keystore.password=TU_CONTRASEÑA
   verifactu.key.alias=mi_certificado
   ```

### Modo de Pruebas

Para desarrollo sin certificado:

```properties
verifactu.test.mode=true
verifactu.aeat.enabled=false
```

El sistema funcionará localmente sin enviar a AEAT.

---

## Verificación de la Instalación

### Checklist Post-Instalación

- [ ] Java 17+ instalado
- [ ] MySQL instalado y corriendo
- [ ] Base de datos "tahona" creada
- [ ] Script SQL ejecutado
- [ ] Datos de GRUPO BABO en `empresa_config`
- [ ] Aplicación compila sin errores
- [ ] Aplicación arranca correctamente
- [ ] Interfaz gráfica se muestra

### Prueba Rápida

1. **Abrir módulo Clientes**
2. **Verificar que se muestran clientes**
3. **Probar búsqueda**
4. **Crear factura de prueba**

---

## Solución de Problemas

### Error: "Cannot connect to database"

**Solución:**
```bash
# Verificar que MySQL está corriendo
sc query MySQL

# O iniciar:
net start MySQL

# Probar conexión:
mysql -u root -p tahona
```

### Error: "No hay configuración de empresa activa"

**Solución:**
```sql
-- Verificar datos
SELECT * FROM empresa_config WHERE activo = TRUE;

-- Si está vacío, ejecutar:
mysql -u root -p tahona < basesdedatos\agregar_estado_facturas_y_empresa.sql
```

### Error: "Java version not found"

**Solución:**
1. Descargar Java 17+ de https://adoptium.net/
2. Instalar
3. Añadir a PATH del sistema
4. Reiniciar terminal

### Error de compilación

**Solución:**
```bash
# Limpiar proyecto
.\mvnw clean

# Recompilar
.\mvnw clean compile -DskipTests
```

---

## Siguientes Pasos

Después de instalar:
1. Leer **[GUIA_USO.md](GUIA_USO.md)** para uso diario
2. Revisar **[GUIA_DESPLIEGUE_COMPLETA.md](../GUIA_DESPLIEGUE_COMPLETA.md)** para producción

---

**¡Instalación Completada! 🎉**

