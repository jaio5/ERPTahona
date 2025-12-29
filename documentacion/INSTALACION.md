# 🛠️ GUÍA DE INSTALACIÓN - ERP Tahona

## 📋 REQUISITOS PREVIOS

### Software Necesario

| Software | Versión Mínima | Versión Recomendada | Descarga |
|----------|----------------|---------------------|----------|
| Java JDK | 17 | 17 o 21 | [Oracle](https://www.oracle.com/java/technologies/downloads/) |
| Maven | 3.8 | 3.9+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| MySQL | 8.0 | 8.0+ | [MySQL](https://dev.mysql.com/downloads/mysql/) |
| Git | 2.0+ | Última | [Git](https://git-scm.com/downloads) |

### Sistema Operativo

- **Windows:** 10/11 (scripts .bat incluidos)
- **Linux:** Compatible pero requiere adaptar scripts
- **macOS:** Compatible pero requiere adaptar scripts

### Hardware Mínimo

- **RAM:** 4 GB
- **Disco:** 500 MB libres
- **Procesador:** Dual Core

---

## 📥 INSTALACIÓN PASO A PASO

### 1. Instalar Java

```powershell
# Verificar si Java está instalado
java -version

# Debe mostrar: java version "17.x.x" o superior
```

Si no está instalado:
1. Descargar JDK 17 de Oracle
2. Ejecutar instalador
3. Configurar JAVA_HOME:
   ```powershell
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```

### 2. Instalar Maven

```powershell
# Verificar Maven
mvn -version

# Debe mostrar: Apache Maven 3.8+ o superior
```

Si no está instalado:
1. Descargar Maven desde apache.org
2. Extraer a `C:\Program Files\Apache\maven`
3. Configurar PATH:
   ```powershell
   setx MAVEN_HOME "C:\Program Files\Apache\maven"
   setx PATH "%PATH%;%MAVEN_HOME%\bin"
   ```

### 3. Instalar MySQL

```powershell
# Verificar MySQL
mysql --version
```

Si no está instalado:
1. Descargar MySQL 8.0 Installer
2. Ejecutar instalador
3. Configurar:
   - **Root Password:** Anotar (ej: Iirne322*)
   - **Puerto:** 3306 (por defecto)
   - **Charset:** UTF-8
4. Iniciar servicio MySQL

### 4. Clonar el Repositorio

```powershell
# Ir a la carpeta de proyectos
cd D:\Programación

# Clonar
git clone [URL-DEL-REPOSITORIO] ERP

# Entrar al proyecto
cd ERP
```

---

## 🗄️ CONFIGURAR BASE DE DATOS

### Paso 1: Crear Base de Datos

```powershell
# Conectar a MySQL
mysql -u root -p

# Ejecutar en MySQL:
```

```sql
CREATE DATABASE tahona CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
EXIT;
```

### Paso 2: Importar Datos

```powershell
# Desde la carpeta del proyecto
mysql -u root -p tahona < basesdedatos\definitivo\tahonaerp.sql
```

Esto creará:
- ✅ Todas las tablas
- ✅ Usuario admin (user: admin, pass: admin)
- ✅ 76 clientes de prueba
- ✅ 131 artículos de prueba
- ✅ 1 almacén

### Paso 3: Verificar Datos

```sql
mysql -u root -p tahona

USE tahona;
SHOW TABLES;

-- Ver usuario admin
SELECT * FROM users WHERE username='admin';

-- Ver cantidad de datos
SELECT COUNT(*) FROM clientes;
SELECT COUNT(*) FROM articulos;

EXIT;
```

---

## ⚙️ CONFIGURAR APLICACIÓN

### Editar application.properties

Archivo: `src/main/resources/application.properties`

```properties
# Configuración MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD_AQUI

# Pool de conexiones
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

**⚠️ IMPORTANTE:** Cambiar `TU_PASSWORD_AQUI` por tu contraseña de MySQL

---

## 🚀 COMPILAR Y EJECUTAR

### Primera Compilación

```powershell
cd D:\Programación\ERP

# Limpiar y compilar
mvn clean install -DskipTests
```

Esto:
1. Descarga todas las dependencias (primera vez tarda más)
2. Compila el código
3. Genera el JAR
4. Se salta los tests

**Tiempo estimado primera vez:** 3-5 minutos

### Ejecutar Aplicación

#### Opción 1: Script (Recomendado)

```powershell
.\scripts\iniciar.bat
```

#### Opción 2: Maven

```powershell
mvn javafx:run
```

#### Opción 3: JAR Ejecutable

```powershell
# Generar JAR
mvn package -DskipTests

# Ejecutar
java -jar target\ERP-0.0.1.jar
```

### Primera Ejecución

1. La aplicación tarda ~20-30 segundos en iniciar
2. Se abrirá la ventana de login
3. Usar credenciales:
   - Usuario: `admin`
   - Contraseña: `admin`
4. Si todo está bien, verás el panel principal

---

## ✅ VERIFICAR INSTALACIÓN

### Script de Verificación

```powershell
.\scripts\verificar_sistema.bat
```

Este script verifica:
- ✅ MySQL está corriendo
- ✅ Base de datos existe
- ✅ Tablas creadas
- ✅ Usuario admin existe
- ✅ Datos de prueba cargados

### Pruebas Manuales

1. **Login:** admin/admin debe funcionar
2. **Clientes:** Ver 76 clientes en la tabla
3. **Artículos:** Ver 131 artículos
4. **Navegación:** Click en cada módulo

Si todo funciona: ✅ **Instalación Correcta**

---

## 🐛 PROBLEMAS COMUNES

### Error: "Could not find or load main class"

**Causa:** JAVA_HOME mal configurado

**Solución:**
```powershell
echo %JAVA_HOME%
# Debe apuntar a carpeta JDK

# Si no:
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```

### Error: "mvn no se reconoce como comando"

**Causa:** Maven no en PATH

**Solución:**
```powershell
# Verificar donde está Maven
where mvn

# Añadir al PATH
setx PATH "%PATH%;C:\Program Files\Apache\maven\bin"

# Reiniciar terminal
```

### Error: "Access denied for user"

**Causa:** Contraseña de MySQL incorrecta

**Solución:**
1. Verificar contraseña de MySQL
2. Editar `application.properties`
3. Cambiar `spring.datasource.password`

### Error: "Unknown database 'tahona'"

**Causa:** Base de datos no creada

**Solución:**
```sql
mysql -u root -p
CREATE DATABASE tahona;
EXIT;

mysql -u root -p tahona < basesdedatos\definitivo\tahonaerp.sql
```

### Error: "Port 3306 already in use"

**Causa:** Otro MySQL corriendo o puerto ocupado

**Solución:**
```powershell
# Ver qué usa el puerto 3306
netstat -ano | findstr :3306

# Detener MySQL si es necesario
net stop MySQL80

# O cambiar puerto en application.properties
```

### Aplicación muy lenta

**Causa:** Poca memoria asignada a JVM

**Solución:**
Editar script de inicio para añadir más memoria:
```bash
java -Xmx2G -Xms512M -jar ERP.jar
```

---

## 🔧 CONFIGURACIÓN AVANZADA

### Cambiar Puerto MySQL

Si MySQL usa otro puerto (ej: 3307):

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/tahona?...
```

### Usar Otra Base de Datos

Si la BD se llama diferente:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/MI_BD?...
```

### Configurar Verifactu

1. Obtener certificado de AEAT (.p12)
2. Colocar en `src/main/resources/certs/`
3. Editar `application.properties`:

```properties
verifactu.aeat.enabled=true
verifactu.keystore.path=certs/mi_certificado.p12
verifactu.keystore.password=MI_PASSWORD
verifactu.key.alias=mi_alias
```

### Logs Detallados

Para más logs de depuración:

```properties
logging.level.root=INFO
logging.level.alicanteweb.erp=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

---

## 📦 DESPLIEGUE EN PRODUCCIÓN

### Crear JAR Ejecutable

```powershell
mvn clean package -DskipTests
```

JAR en: `target\ERP-0.0.1.jar`

### Configuración Producción

Crear `application-prod.properties`:

```properties
spring.datasource.url=jdbc:mysql://SERVIDOR_PROD:3306/tahona
spring.datasource.username=usuario_prod
spring.datasource.password=password_seguro

# Deshabilitar logs SQL
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=ERROR
```

### Ejecutar en Producción

```powershell
java -jar -Dspring.profiles.active=prod ERP-0.0.1.jar
```

### Servicio Windows

Usar [NSSM](https://nssm.cc/) para crear servicio:

```powershell
nssm install ERP-Tahona "C:\Program Files\Java\jdk-17\bin\java.exe"
nssm set ERP-Tahona AppParameters "-jar D:\ERP\target\ERP-0.0.1.jar"
nssm start ERP-Tahona
```

---

## 🔐 SEGURIDAD

### Cambiar Contraseña Admin

Después de la instalación:

```sql
-- Generar nuevo hash BCrypt para 'mipassword'
-- Usar herramienta online o código Java

UPDATE users 
SET password = '$2a$10$NUEVO_HASH_AQUI'
WHERE username = 'admin';
```

### Backups Automáticos

Script de backup diario:

```powershell
# backup_diario.bat
@echo off
set FECHA=%date:~-4%%date:~3,2%%date:~0,2%
mysqldump -u root -p tahona > backup_%FECHA%.sql
```

Programar en Tareas Programadas de Windows.

---

## 📞 SOPORTE

### Archivos de Log

Logs en consola durante ejecución. Para guardarlos:

```powershell
mvn javafx:run > app.log 2>&1
```

### Información del Sistema

```powershell
# Java
java -version

# Maven
mvn -version

# MySQL
mysql --version
```

### Contacto

[Información de soporte técnico]

---

## ✅ CHECKLIST FINAL

Antes de poner en producción:

- [ ] Java 17+ instalado
- [ ] Maven funciona
- [ ] MySQL 8.0 corriendo
- [ ] Base de datos `tahona` creada e importada
- [ ] Usuario admin verificado
- [ ] `application.properties` configurado
- [ ] Compilación exitosa
- [ ] Login funciona
- [ ] Datos visibles en tablas
- [ ] Contraseña admin cambiada
- [ ] Backup configurado
- [ ] Verifactu configurado (si aplica)

---

**Tiempo total de instalación:** 30-45 minutos  
**Dificultad:** Media  
**Última actualización:** 27/12/2025

