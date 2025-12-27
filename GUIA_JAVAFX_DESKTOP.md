# 🖥️ GUÍA COMPLETA - APLICACIÓN JAVAFX DESKTOP

**ERP Panadería Tahona - Aplicación de Escritorio**  
**Fecha:** 26 de diciembre de 2025

---

## ✅ ESTADO ACTUAL

La aplicación **FASE 1** está completamente funcional como **aplicación de escritorio JavaFX**.

### Incluye:
- ✅ Login con JavaFX
- ✅ Gestión de usuarios
- ✅ RGPD completo
- ✅ VeriFactu con QR
- ✅ Facturación
- ✅ Validaciones
- ✅ 12 servicios funcionando

---

## 🚀 CÓMO EJECUTAR LA APLICACIÓN

### **OPCIÓN 1: Script BAT (Más simple)**

```bash
# Doble click en:
ejecutar_javafx.bat
```

### **OPCIÓN 2: Script PowerShell**

```powershell
# Click derecho → Ejecutar con PowerShell:
.\ejecutar_javafx.ps1
```

### **OPCIÓN 3: Comando directo Maven**

```bash
cd D:\Programación\ERP

# Compilar
mvn clean compile -DskipTests

# Ejecutar JavaFX
mvn javafx:run
```

### **OPCIÓN 4: Desde IDE**

1. Abre el proyecto en **IntelliJ IDEA**
2. Click derecho en `ErpLauncher.java`
3. Run 'ErpLauncher.main()'

---

## 📋 REQUISITOS

### Software necesario:

1. **Java 17+** (JDK)
   - Descarga: https://adoptium.net/
   - Verificar: `java -version`

2. **Maven 3.8+**
   - Verificar: `mvn -version`

3. **MySQL 8+** (para base de datos)
   - Debe estar ejecutándose
   - Base de datos: `erp_alicante`

### Configuración base de datos:

```bash
# Crear base de datos
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS erp_alicante;"

# Ejecutar script FASE 1
mysql -u root -p erp_alicante < basesdedatos/fase1_legalizacion.sql
```

---

## 🎯 FLUJO DE EJECUCIÓN

### Paso 1: Preparar base de datos

```bash
cd D:\Programación\ERP

# Iniciar MySQL (si no está iniciado)
net start MySQL

# Crear BD y ejecutar script
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS erp_alicante;"
mysql -u root -p erp_alicante < basesdedatos/fase1_legalizacion.sql
```

### Paso 2: Compilar proyecto

```bash
mvn clean compile -DskipTests
```

### Paso 3: Ejecutar aplicación

```bash
mvn javafx:run
```

### Paso 4: Login

```
Usuario:    admin
Contraseña: admin123
```

**⚠️ Cambiar contraseña en primer login**

---

## 🖥️ VENTANAS DE LA APLICACIÓN

### Pantalla de Login (`login.fxml`)
- Usuario y contraseña
- Recordar sesión
- Recuperar contraseña
- Validación en tiempo real

### Pantalla Principal (`main_panel.fxml`)
- Menú de navegación
- Panel de facturas
- Panel de clientes
- Panel de artículos
- Panel de proveedores
- Panel de VeriFactu

---

## ⚙️ CONFIGURACIÓN

### `application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/erp_alicante
spring.datasource.username=root
spring.datasource.password=tu_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JavaFX (sin servidor web)
spring.main.web-application-type=none
```

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Problema: "Java no encontrado"

**Solución:**
```bash
# Verificar instalación
java -version

# Si no está instalado, descargar de:
# https://adoptium.net/temurin/releases/
```

### Problema: "Maven no encontrado"

**Solución:**
```bash
# Verificar instalación
mvn -version

# Si no está instalado:
# 1. Descargar: https://maven.apache.org/download.cgi
# 2. Extraer a C:\Program Files\apache-maven-3.9.x
# 3. Agregar a PATH
```

### Problema: "No se puede conectar a BD"

**Solución:**
```bash
# Verificar MySQL ejecutándose
net start MySQL

# Verificar usuario y password en application.properties
```

### Problema: "Error al cargar FXML"

**Solución:**
```bash
# Verificar que los archivos FXML existen:
dir src\main\resources\ui\*.fxml

# Debe existir:
# - login.fxml
# - main_panel.fxml
# - facturas_panel.fxml
# etc.
```

### Problema: "JavaFX no encontrado"

**Solución:**
Ya está incluido en el pom.xml, pero si falla:
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.5</version>
</dependency>
```

---

## 📦 EMPAQUETAR APLICACIÓN

### Crear JAR ejecutable:

```bash
mvn clean package -DskipTests
```

El JAR estará en: `target/ERP-0.0.1.jar`

### Ejecutar JAR:

```bash
java -jar target/ERP-0.0.1.jar
```

### Crear instalador Windows (opcional):

```bash
# Usar jpackage (Java 17+)
jpackage --input target --name "ERP Panaderia Tahona" ^
  --main-jar ERP-0.0.1.jar ^
  --type exe ^
  --win-shortcut ^
  --win-menu
```

---

## 🎨 PERSONALIZACIÓN UI

### Modificar estilos:

Edita: `src/main/resources/css/main_panel.css`

```css
/* Ejemplo: Cambiar color principal */
.root {
    -fx-base: #2c3e50;
}
```

### Añadir nuevas ventanas:

1. Crear FXML: `src/main/resources/ui/mi_ventana.fxml`
2. Crear Controller: `src/main/java/.../controller/MiVentanaController.java`
3. Cargar desde código:

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/mi_ventana.fxml"));
Parent root = loader.load();
Scene scene = new Scene(root);
stage.setScene(scene);
stage.show();
```

---

## 📊 ESTRUCTURA DEL PROYECTO JAVAFX

```
D:\Programación\ERP
├── src/main/
│   ├── java/
│   │   └── alicanteweb/erp/
│   │       ├── ErpLauncher.java        ← Punto de entrada JavaFX
│   │       ├── controller/              ← Controllers FXML
│   │       │   ├── LoginController.java
│   │       │   ├── FacturaController.java
│   │       │   └── ...
│   │       ├── service/                 ← Lógica de negocio
│   │       ├── entities/                ← Entidades JPA
│   │       └── repository/              ← Repositorios
│   └── resources/
│       ├── ui/                          ← Archivos FXML
│       │   ├── login.fxml
│       │   ├── main_panel.fxml
│       │   └── ...
│       ├── css/                         ← Estilos CSS
│       ├── images/                      ← Imágenes
│       └── application.properties       ← Configuración
├── pom.xml                              ← Maven config
├── ejecutar_javafx.bat                  ← Script ejecución BAT
└── ejecutar_javafx.ps1                  ← Script ejecución PS1
```

---

## ✅ CHECKLIST ANTES DE EJECUTAR

- [ ] Java 17+ instalado
- [ ] Maven instalado
- [ ] MySQL ejecutándose
- [ ] Base de datos creada
- [ ] Script SQL ejecutado
- [ ] application.properties configurado
- [ ] Proyecto compilado

---

## 🎯 COMANDOS ÚTILES

```bash
# Ver versión Java
java -version

# Ver versión Maven
mvn -version

# Limpiar proyecto
mvn clean

# Compilar
mvn compile

# Ejecutar tests
mvn test

# Empaquetar
mvn package

# Ejecutar JavaFX
mvn javafx:run

# Ver dependencias
mvn dependency:tree

# Verificar MySQL
net start MySQL
mysql -u root -p -e "SHOW DATABASES;"
```

---

## 📚 RECURSOS

### Documentación:
- **JavaFX:** https://openjfx.io/
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Maven:** https://maven.apache.org/

### Librerías usadas:
- JavaFX 21
- Spring Boot 3.5
- Hibernate/JPA
- MySQL Connector
- ControlsFX (controles avanzados)
- Ikonli (iconos FontAwesome)
- ZXing (QR codes)

---

## 🎉 ¡LISTO!

Tu aplicación JavaFX está lista para ejecutarse en escritorio.

### Para arrancar:

```bash
# Opción simple:
ejecutar_javafx.bat

# O con Maven:
mvn javafx:run
```

**La ventana de login aparecerá automáticamente.**

---

## 💡 PRÓXIMOS PASOS

1. **Ejecutar la aplicación** con alguno de los métodos
2. **Hacer login** con admin/admin123
3. **Cambiar contraseña** por seguridad
4. **Explorar las pantallas** disponibles
5. **Crear tu primer cliente/factura**

---

## 🆘 SOPORTE

Si tienes problemas:

1. Revisa `run_error.log` si la app falla
2. Verifica logs de Maven
3. Comprueba que MySQL esté corriendo
4. Verifica application.properties

---

**¡Disfruta tu ERP de escritorio con JavaFX!** 🚀

---

*Guía actualizada: 26 de diciembre de 2025*

