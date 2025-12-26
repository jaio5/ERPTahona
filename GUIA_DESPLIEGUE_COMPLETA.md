# 🚀 GUÍA COMPLETA DE DESPLIEGUE - ERP TAHONA
## Sistema de Gestión para GRUPO BABO, S.Coop.V.L.

**Versión:** 1.0.0  
**Fecha:** 25 de Diciembre de 2025  
**Empresa:** GRUPO BABO, S.Coop.V.L. (CIF: F54059985)

---

## 📋 ÍNDICE

1. [Requisitos Previos](#requisitos-previos)
2. [Instalación Paso a Paso](#instalación-paso-a-paso)
3. [Configuración de Base de Datos](#configuración-de-base-de-datos)
4. [Configuración de Verifactu](#configuración-de-verifactu)
5. [Primera Ejecución](#primera-ejecución)
6. [Verificación del Sistema](#verificación-del-sistema)
7. [Uso Diario](#uso-diario)
8. [Mantenimiento](#mantenimiento)
9. [Resolución de Problemas](#resolución-de-problemas)
10. [Soporte](#soporte)

---

## 📦 REQUISITOS PREVIOS

### Software Necesario

| Software | Versión Mínima | Descarga |
|----------|---------------|----------|
| **Java JDK** | 17 o superior | https://adoptium.net/ |
| **Maven** | 3.8+ | Incluido en el proyecto (mvnw) |
| **MySQL** | 8.0+ | https://dev.mysql.com/downloads/ |
| **JavaFX** | 17+ | Incluido en dependencias |

### Hardware Recomendado

- **Procesador:** Intel Core i5 o superior
- **RAM:** 8 GB mínimo (16 GB recomendado)
- **Disco:** 500 MB para aplicación + espacio para datos
- **Pantalla:** 1366x768 mínimo (1920x1080 recomendado)

### Sistema Operativo

- **Windows:** 10 o superior (64 bits)
- **Linux:** Ubuntu 20.04+ o equivalente
- **macOS:** 11 Big Sur o superior

---

## 🔧 INSTALACIÓN PASO A PASO

### Paso 1: Verificar Java

```bash
# Verificar instalación de Java
java -version

# Debería mostrar: openjdk version "17.x.x" o superior
```

Si no está instalado:
1. Descargar de https://adoptium.net/
2. Instalar siguiendo el asistente
3. Reiniciar la terminal

### Paso 2: Verificar MySQL

```bash
# Verificar MySQL
mysql --version

# Debería mostrar: mysql Ver 8.0.x
```

Si no está instalado:
1. Descargar MySQL Installer
2. Instalar MySQL Server
3. Configurar contraseña de root
4. Iniciar el servicio

### Paso 3: Crear Base de Datos

```sql
-- Abrir MySQL Workbench o línea de comandos:
mysql -u root -p

-- Crear la base de datos:
CREATE DATABASE IF NOT EXISTS tahona 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verificar:
SHOW DATABASES;

-- Salir:
EXIT;
```

### Paso 4: Configurar Datos de Empresa

```bash
# Ejecutar script de configuración automática:
cd C:\Programación\segundoJAVA\springboot\demo\ERPTahona
configurar_bd.bat

# O manualmente:
mysql -u root -p tahona < basesdedatos\agregar_estado_facturas_y_empresa.sql
```

Este script:
- ✅ Crea tabla `empresa_config`
- ✅ Inserta datos de GRUPO BABO
- ✅ Añade campos de estado a facturas
- ✅ Configura campos de Verifactu

### Paso 5: Verificar Configuración

```sql
mysql -u root -p tahona

-- Ver datos de GRUPO BABO:
SELECT * FROM empresa_config WHERE activo = TRUE;

-- Debería mostrar:
-- nombre_empresa: GRUPO BABO, S.Coop.V.L.
-- cif: F54059985
-- telefono: 965 68 73 58
-- etc.

-- Ver estructura de facturas:
DESCRIBE facturas;

-- Debería incluir:
-- estado
-- verifactu_enviada
-- fecha_emision_verifactu
-- observaciones_revision
```

---

## 🗄️ CONFIGURACIÓN DE BASE DE DATOS

### Archivo de Configuración

Editar `src/main/resources/application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?useSSL=false&serverTimezone=Europe/Madrid
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA_AQUI

# Zona horaria de España
spring.jackson.time-zone=Europe/Madrid
```

### Configuración de Producción

Para usar en producción, activar perfil prod:

```bash
# Copiar plantilla
cp src/main/resources/application-prod.properties.example src/main/resources/application-prod.properties

# Editar con datos reales
notepad src/main/resources/application-prod.properties

# Ejecutar con perfil de producción
java -jar -Dspring.profiles.active=prod erp-tahona.jar
```

---

## 🔐 CONFIGURACIÓN DE VERIFACTU

### Obtener Certificado Digital

1. **Solicitar certificado** a la FNMT (Fábrica Nacional de Moneda y Timbre)
   - URL: https://www.sede.fnmt.gob.es/
   - Tipo: Certificado de persona jurídica
   - Para: GRUPO BABO, S.Coop.V.L. (CIF F54059985)

2. **Exportar a formato PKCS12** (.p12)
   ```
   Nombre: mi_certificado.p12
   Contraseña: (crear contraseña segura)
   ```

3. **Copiar al proyecto**
   ```
   Copiar a: src/main/resources/certs/mi_certificado.p12
   ```

4. **Configurar en application.properties**
   ```properties
   verifactu.keystore.path=/certs/mi_certificado.p12
   verifactu.keystore.password=TU_CONTRASEÑA
   verifactu.key.alias=mi_certificado
   verifactu.key.password=TU_CONTRASEÑA
   ```

### Modo de Pruebas

Para desarrollo sin certificado:

```properties
# En application.properties
verifactu.test.mode=true
verifactu.aeat.enabled=false
```

El sistema funcionará pero NO enviará a AEAT (solo guarda evidencias locales).

---

## 🎯 PRIMERA EJECUCIÓN

### Compilar el Proyecto

```bash
cd C:\Programación\segundoJAVA\springboot\demo\ERPTahona

# Compilar (primera vez puede tardar)
.\mvnw clean compile

# Si hay errores, intentar:
.\mvnw clean install -DskipTests
```

### Arrancar la Aplicación

```bash
# Opción 1: Usar script de arranque
.\arrancar.bat

# Opción 2: Con Maven
.\mvnw spring-boot:run

# Opción 3: JAR ejecutable
.\mvnw package
java -jar target/erp-tahona-1.0.0.jar
```

### Primera Pantalla

Al arrancar verás:
```
================================================
      ERP TAHONA - GRUPO BABO
================================================
Inicializando...
✓ Base de datos conectada
✓ Empresa configurada: GRUPO BABO, S.Coop.V.L.
✓ CIF: F54059985
✓ Verifactu: Habilitado
================================================
Aplicación lista en: http://localhost:8080
Interfaz JavaFX iniciando...
================================================
```

---

## ✅ VERIFICACIÓN DEL SISTEMA

### Checklist Post-Instalación

- [ ] MySQL instalado y funcionando
- [ ] Base de datos "tahona" creada
- [ ] Script SQL ejecutado correctamente
- [ ] Datos de GRUPO BABO en empresa_config
- [ ] Java 17+ instalado
- [ ] Aplicación compila sin errores
- [ ] Aplicación arranca correctamente
- [ ] Interfaz gráfica se muestra
- [ ] Se pueden ver los módulos (Clientes, Facturas, etc.)

### Pruebas Básicas

#### 1. Probar Módulo de Clientes

1. Ir a Dashboard
2. Clic en "Clientes"
3. Clic en "Nuevo Cliente"
4. Rellenar datos
5. Guardar
6. Verificar que aparece en la lista

#### 2. Probar Módulo de Artículos

1. Ir a "Artículos"
2. Crear artículos de ejemplo:
   - BARRA GALLEGA (IVA 4% - pan común)
   - CROISSANT (IVA 10% - bollería)
   - TARTA (IVA 21% - repostería)

#### 3. Probar Flujo de Facturación

1. Crear una factura (estado: BORRADOR)
2. Añadir líneas
3. Enviar a REVISION
4. Aprobar y Emitir (envía a Verifactu)
5. Verificar estado: EMITIDA
6. Comprobar en BD:
   ```sql
   SELECT * FROM verifactu_evidence WHERE numero = 'FAC001';
   ```

---

## 💼 USO DIARIO

### Flujo de Trabajo Recomendado

#### Mañana
1. **Arrancar sistema**
   ```bash
   .\arrancar.bat
   ```

2. **Revisar pendientes**
   - Facturas en REVISION
   - Albaranes sin facturar
   - Pagos pendientes

#### Durante el Día
1. **Crear albaranes** según pedidos
2. **Emitir facturas** al final del día
3. **Registrar pagos** recibidos

#### Al Cerrar
1. **Revisar facturas emitidas**
2. **Hacer backup** (automático a las 2:00 AM)
3. **Cerrar aplicación** correctamente

### Gestión de Estados de Facturas

```
BORRADOR → REVISION → EMITIDA → (opcional) ANULADA
```

**BORRADOR**
- Crear y editar libremente
- No se envía a AEAT
- Se puede eliminar

**REVISION**
- Pendiente de aprobación
- Se pueden añadir observaciones
- No se puede eliminar

**EMITIDA**
- Enviada a Verifactu/AEAT
- NO se puede modificar
- Solo se puede anular

**ANULADA**
- Factura cancelada
- Registro histórico
- Generar rectificativa si es necesario

---

## 🔧 MANTENIMIENTO

### Backup Automático

El sistema hace backup automático cada día a las 2:00 AM:

```
Ubicación: C:/backups/erp-tahona/
Formato: tahona_YYYYMMDD_HHMMSS.sql
Retención: 30 días
```

### Backup Manual

```bash
# Backup de base de datos
mysqldump -u root -p tahona > backup_tahona_%date:~-4,4%%date:~-7,2%%date:~-10,2%.sql

# Backup de archivos
xcopy C:\Programación\segundoJAVA\springboot\demo\ERPTahona\data C:\backups\erp-data\ /E /Y
```

### Actualización del Sistema

```bash
# 1. Hacer backup completo
mysqldump -u root -p tahona > backup_antes_actualizar.sql

# 2. Descargar nueva versión
git pull origin main

# 3. Compilar
.\mvnw clean compile

# 4. Ejecutar scripts de migración (si los hay)
mysql -u root -p tahona < basesdedatos/migraciones/v1.1.0.sql

# 5. Arrancar y verificar
.\arrancar.bat
```

### Limpieza de Logs

Los logs se rotan automáticamente pero puedes limpiar manualmente:

```bash
# Limpiar logs antiguos (más de 30 días)
forfiles /p "logs" /s /m *.log /d -30 /c "cmd /c del @path"
```

---

## 🆘 RESOLUCIÓN DE PROBLEMAS

### Problema: No arranca la aplicación

**Síntoma:** Error al ejecutar arrancar.bat

**Soluciones:**
1. Verificar Java:
   ```bash
   java -version
   # Debe ser 17 o superior
   ```

2. Verificar MySQL:
   ```bash
   mysql -u root -p -e "STATUS"
   ```

3. Ver logs:
   ```bash
   type logs\erp-tahona.log
   ```

### Problema: Error de conexión a BD

**Síntoma:** "Cannot connect to database"

**Soluciones:**
1. Verificar que MySQL está arrancado:
   ```bash
   sc query MySQL
   # O
   net start MySQL
   ```

2. Verificar credenciales en application.properties

3. Probar conexión manual:
   ```bash
   mysql -u root -p tahona
   ```

### Problema: No aparecen datos de GRUPO BABO

**Síntoma:** Error "No hay configuración de empresa activa"

**Soluciones:**
1. Verificar en BD:
   ```sql
   SELECT * FROM empresa_config WHERE activo = TRUE;
   ```

2. Si está vacío, ejecutar:
   ```bash
   mysql -u root -p tahona < basesdedatos\agregar_estado_facturas_y_empresa.sql
   ```

3. Reiniciar aplicación

### Problema: Verifactu no funciona

**Síntoma:** Error al emitir facturas

**Soluciones:**
1. Verificar modo de pruebas:
   ```properties
   verifactu.test.mode=true
   verifactu.aeat.enabled=false
   ```

2. Verificar certificado:
   - Existe en src/main/resources/certs/
   - Contraseña correcta
   - No ha caducado

3. Ver logs de Verifactu:
   ```bash
   type logs\erp-tahona.log | findstr "Verifactu"
   ```

### Problema: Interfaz no responde

**Síntoma:** Ventanas no se abren o se quedan congeladas

**Soluciones:**
1. Cerrar y reiniciar aplicación

2. Si persiste, limpiar caché:
   ```bash
   del /Q target\classes\*
   .\mvnw clean compile
   ```

3. Verificar memoria:
   ```bash
   # Arrancar con más memoria
   java -Xmx2G -jar target/erp-tahona-1.0.0.jar
   ```

---

## 📞 SOPORTE

### Contacto

**Empresa:** GRUPO BABO, S.Coop.V.L.  
**CIF:** F54059985  
**Teléfono:** 965 68 73 58  
**Email:** administracion@grupobaelo.com  

### Documentación

- **Guía de Usuario:** `MANUAL_USUARIO.md`
- **Documentación Técnica:** `docs/`
- **FAQ:** `FAQ.md`
- **Changelog:** `CHANGELOG.md`

### Recursos Online

- **AEAT Verifactu:** https://www.agenciatributaria.gob.es/verifactu
- **MySQL Docs:** https://dev.mysql.com/doc/
- **Spring Boot:** https://spring.io/projects/spring-boot
- **JavaFX:** https://openjfx.io/

---

## 📝 CHECKLIST FINAL

### Antes de Usar en Producción

- [ ] Base de datos configurada correctamente
- [ ] Datos de GRUPO BABO verificados
- [ ] Certificado Verifactu instalado (producción)
- [ ] Backup automático configurado
- [ ] Sistema probado con datos de prueba
- [ ] Formación de usuarios realizada
- [ ] Manuales entregados
- [ ] Contacto de soporte establecido

### Verificación Semanal

- [ ] Backup funcionando correctamente
- [ ] Logs sin errores críticos
- [ ] Espacio en disco suficiente
- [ ] Facturas emitidas correctamente
- [ ] Evidencias Verifactu guardadas

### Verificación Mensual

- [ ] Actualizar sistema si hay nuevas versiones
- [ ] Revisar certificado Verifactu (caducidad)
- [ ] Limpieza de datos antiguos
- [ ] Optimización de base de datos
- [ ] Renovar backup externo

---

## 🎉 ¡SISTEMA LISTO!

El ERP Tahona está completamente configurado para:

✅ Gestión de clientes y proveedores  
✅ Control de inventario y almacenes  
✅ Emisión de albaranes  
✅ Facturación con estados (BORRADOR → REVISION → EMITIDA)  
✅ Integración Verifactu con datos de GRUPO BABO  
✅ Validaciones fiscales españolas  
✅ Impresión de documentos  
✅ Backup automático  

**¡Bienvenido al ERP Tahona!** 🚀

---

**Versión del documento:** 1.0.0  
**Última actualización:** 25/12/2025  
**Próxima revisión:** 25/01/2026

