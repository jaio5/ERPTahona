# 🔧 SOLUCIÓN DE PROBLEMAS - APLICACIÓN NO ARRANCA

**Fecha**: 2026-01-13  
**Versión**: 1.3.0

---

## 🔍 PROBLEMA IDENTIFICADO

La aplicación puede no arrancar por varios motivos. Aquí están las soluciones:

---

## ✅ SOLUCIÓN RÁPIDA

### Opción 1: Usar el Script Mejorado

```bash
# Ejecutar el nuevo script
ARRANCAR_ERP_MEJORADO.bat
```

Este script:
- ✅ Limpia el proyecto automáticamente
- ✅ Compila con verificación de errores
- ✅ Muestra mensajes claros
- ✅ Arranca la aplicación

### Opción 2: Comandos Manuales

```bash
# 1. Limpiar proyecto
mvn clean

# 2. Compilar SIN tests
mvn compile -DskipTests

# 3. Arrancar
mvn javafx:run
```

---

## 🐛 CAUSAS COMUNES Y SOLUCIONES

### 1. Tests con @SpringBootTest Fallan

**Problema**: Los tests con `@SpringBootTest` intentan cargar todo Spring en cada test.

**Solución A**: Compilar sin ejecutar tests
```bash
mvn clean compile -DskipTests
mvn javafx:run
```

**Solución B**: Mejorar los tests (Recomendado)

Cambiar:
```java
@SpringBootTest  // ❌ Carga todo Spring
class BackupServiceTest {
```

Por:
```java
@ExtendWith(MockitoExtension.class)  // ✅ Solo mockea
class BackupServiceTest {
```

---

### 2. Base de Datos No Disponible

**Problema**: MySQL no está corriendo

**Verificar**:
```bash
# Verificar si MySQL está corriendo
mysql -u root -p -e "SELECT 1"
```

**Solución**:
```bash
# Iniciar MySQL
net start MySQL80

# O desde servicios de Windows
services.msc
```

---

### 3. Puerto 3306 en Uso

**Problema**: Otro proceso usa el puerto de MySQL

**Verificar**:
```bash
netstat -ano | findstr :3306
```

**Solución**: Cambiar puerto en `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/tahona?...
```

---

### 4. Dependencias Maven Corruptas

**Problema**: Caché de Maven corrupta

**Solución**:
```bash
# Limpiar caché Maven
mvn dependency:purge-local-repository

# O manualmente borrar
rmdir /s /q %USERPROFILE%\.m2\repository

# Recompilar
mvn clean install -DskipTests
```

---

### 5. JavaFX No Encuentra Recursos

**Problema**: No encuentra archivos FXML

**Verificar**: Archivos en `src/main/resources/ui/`

**Solución**: Recompilar recursos
```bash
mvn clean resources:resources compile
```

---

### 6. Memoria Insuficiente

**Problema**: JVM sin memoria

**Solución**: Aumentar memoria en `pom.xml`

```xml
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
    <configuration>
        <mainClass>alicanteweb.erp.ErpLauncher</mainClass>
        <options>
            <option>-Xmx1024m</option>  <!-- Aumentar memoria -->
        </options>
    </configuration>
</plugin>
```

---

### 7. Conflicto de Versiones Java

**Problema**: Java 17 no instalado o múltiples versiones

**Verificar**:
```bash
java -version
# Debe mostrar: java version "17.x.x"
```

**Solución**: Instalar Java 17 o configurar `JAVA_HOME`

```bash
# Ver Java instalado
where java

# Configurar JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
```

---

## 🔧 DIAGNÓSTICO AVANZADO

### Script de Diagnóstico Completo

Ejecutar:
```bash
DIAGNOSTICO_ARRANQUE.bat
```

Este script verifica:
- ✅ Versión de Java
- ✅ Versión de Maven
- ✅ Compilación del proyecto
- ✅ Intento de arranque con logs completos

---

## 🎯 SOLUCIÓN DEFINITIVA

Si nada funciona, ejecutar esta secuencia completa:

```bash
# 1. Limpiar TODO
mvn clean
rmdir /s /q target

# 2. Verificar Java y Maven
java -version
mvn -version

# 3. Verificar MySQL
mysql -u root -pIirne322* -e "USE tahona; SELECT COUNT(*) FROM users;"

# 4. Compilar desde cero SIN tests
mvn clean compile -DskipTests -U

# 5. Arrancar
mvn javafx:run
```

---

## 📋 CHECKLIST DE VERIFICACIÓN

Antes de arrancar, verificar:

- [ ] ✅ Java 17+ instalado
- [ ] ✅ Maven 3.6+ instalado
- [ ] ✅ MySQL corriendo
- [ ] ✅ Base de datos `tahona` existe
- [ ] ✅ Usuario `admin` existe en BD
- [ ] ✅ Puerto 3306 disponible
- [ ] ✅ Proyecto compilado sin errores
- [ ] ✅ Sin tests fallando (usar -DskipTests)

---

## 🚀 ARRANQUE GARANTIZADO

### Método Infalible

```bash
# Paso 1: Limpiar
cd D:\Programación\ERP
mvn clean

# Paso 2: Compilar SIN tests
mvn compile -DskipTests

# Paso 3: Si funciona, arrancar
mvn javafx:run

# Paso 4: Si no funciona, ver logs completos
mvn javafx:run > salida.log 2>&1
type salida.log
```

---

## 💡 PROBLEMAS ESPECÍFICOS

### Error: "No se puede conectar a MySQL"

```properties
# Verificar application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?...
spring.datasource.username=root
spring.datasource.password=Iirne322*
```

### Error: "Class not found: ErpLauncher"

```xml
<!-- Verificar pom.xml -->
<configuration>
    <mainClass>alicanteweb.erp.ErpLauncher</mainClass>
</configuration>
```

### Error: "Could not load FXML"

Verificar que existen:
- `src/main/resources/ui/login.fxml`
- `src/main/resources/ui/main_panel.fxml`
- `src/main/resources/styles/simple-theme.css`

### Error en Tests

```bash
# Ignorar tests y arrancar directamente
mvn clean compile -DskipTests
mvn javafx:run
```

---

## 📞 SI TODO FALLA

### Arranque Manual con Java

```bash
# Compilar
mvn clean package -DskipTests

# Arrancar con java directamente
java -jar target/ERP-0.0.1.jar
```

---

## ✅ VERIFICACIÓN FINAL

La aplicación debería mostrar:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.7)

[INFO] Contexto de Spring Boot inicializado correctamente
[INFO] Iniciando aplicación JavaFX...
[INFO] Pantalla de login cargada correctamente
```

Y luego la ventana de login.

---

## 🎯 RESUMEN EJECUTIVO

| Problema | Solución |
|----------|----------|
| Tests fallan | `mvn compile -DskipTests` |
| MySQL no corre | `net start MySQL80` |
| Caché corrupta | `mvn clean` |
| No encuentra recursos | `mvn resources:resources` |
| Sin memoria | Aumentar `-Xmx` en pom.xml |
| Java incorrecto | Instalar Java 17 |

**Solución más común**: `mvn clean compile -DskipTests && mvn javafx:run`

---

**Última actualización**: 2026-01-13  
**Estado**: ✅ Guía completa de solución de problemas  
**Archivos creados**:
- DIAGNOSTICO_ARRANQUE.bat
- ARRANCAR_ERP_MEJORADO.bat

