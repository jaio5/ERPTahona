# 🍞 ERP Panadería Tahona

Sistema de gestión empresarial completo para panaderías, desarrollado con JavaFX, Spring Boot y MySQL.

---

## 🎉 ¡FRONTEND MODERNO COMPLETADO!

El ERP ahora cuenta con una **interfaz gráfica moderna, profesional y completamente funcional**.

### ✨ Características del Frontend
- 🎨 **Diseño Material Design** con tema personalizado de panadería
- 📊 **6 módulos funcionales**: Clientes, Proveedores, Artículos, Facturas, Almacenes, Verifactu
- 🔍 **Búsqueda en tiempo real** en todos los módulos
- 🎯 **Navegación intuitiva** con sidebar persistente
- 💫 **Animaciones suaves** y efectos hover profesionales
- 🎨 **Iconos FontAwesome** en toda la interfaz
- 📱 **Diseño responsive** y adaptativo

---

## 🚀 Inicio Rápido

### 1. Ejecutar la Aplicación

**Opción A: Usar script (RECOMENDADO)**
```bash
.\run.bat
```

**Opción B: Desde IntelliJ IDEA**
1. Abrir el proyecto
2. Ejecutar `ErpLauncher.java`

**Opción C: Maven**
```bash
mvn clean compile -DskipTests
mvn dependency:copy-dependencies -DincludeScope=runtime
java --module-path "target/dependency" --add-modules javafx.controls,javafx.fxml -cp "target/classes;target/dependency/*" alicanteweb.erp.ErpLauncher
```

### 2. Configurar Base de Datos

Ver instrucciones detalladas en: **[README_DB.md](README_DB.md)**

---

## 📚 Documentación

### Guías del Frontend
- **[INICIO_RAPIDO.md](INICIO_RAPIDO.md)** - Guía de inicio rápido
- **[README_FRONTEND.md](README_FRONTEND.md)** - Documentación completa del frontend
- **[FRONTEND_COMPLETO.md](FRONTEND_COMPLETO.md)** - Resumen ejecutivo
- **[CHECKLIST_FRONTEND.md](CHECKLIST_FRONTEND.md)** - Lista de verificación
- **[docs/UI_UX_GUIDE.md](docs/UI_UX_GUIDE.md)** - Guía de diseño UI/UX
- **[docs/VISTA_PREVIA_UI.md](docs/VISTA_PREVIA_UI.md)** - Vista previa visual

### Otras Guías
- **[README_DB.md](README_DB.md)** - Configuración de base de datos
- **[docs/estructura.md](docs/estructura.md)** - Estructura del proyecto
- **[docs/PROYECTO_ERP_PANADERIA.md](docs/PROYECTO_ERP_PANADERIA.md)** - Documentación del proyecto

---

## 📦 Tecnologías

### Frontend
- **JavaFX 21.0.5** - Framework de interfaz gráfica
- **FXML** - Diseño declarativo de vistas
- **CSS3** - Estilos personalizados (Material Design)
- **Ikonli FontAwesome** - Iconografía profesional

### Backend
- **Spring Boot 3.5.7** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Hibernate** - ORM
- **MySQL / H2** - Base de datos

### Build & Tools
- **Maven 3.x** - Gestión de dependencias
- **Java 17** - Lenguaje de programación
- **Lombok** - Reducción de código boilerplate

---

## 🎨 Vista Previa del Frontend

### Dashboard Principal
```
┌─────────────┐
│  MÓDULOS    │   ┌──────────┬──────────┬──────────┐
├─────────────┤   │ Clientes │Proveed.  │Artículos │
│ 👥 Clientes │   ├──────────┼──────────┼──────────┤
│ 🚚 Proveed. │   │ Facturas │Almacenes │Verifactu │
│ 📦 Artículos│   └──────────┴──────────┴──────────┘
│ 💰 Facturas │
│ 🏭 Almacenes│
│ 🛡️ Verifactu│
└─────────────┘
```

### Módulos Disponibles

| Módulo | Icono | Descripción | Columnas | Estado |
|--------|-------|-------------|----------|--------|
| Clientes | 👥 | Gestión de clientes y contactos | 8 | ✅ |
| Proveedores | 🚚 | Gestión de proveedores | 8 | ✅ |
| Artículos | 📦 | Catálogo de productos | 8 | ✅ |
| Facturas | 💰 | Facturación y ventas | 7 | ✅ |
| Almacenes | 🏭 | Control de inventario | 3 | ✅ |
| Verifactu | 🛡️ | Sistema de verificación | 5 + Stats | ✅ |

---

## 🎯 Funcionalidades

### Implementadas ✅
- ✅ **Dashboard interactivo** con 6 módulos
- ✅ **Navegación fluida** entre vistas
- ✅ **Búsqueda en tiempo real** en todos los módulos
- ✅ **Visualización de datos** desde base de datos
- ✅ **Eliminación de registros** con confirmación
- ✅ **Actualización de datos** (refresh)
- ✅ **Filtrado inteligente** por múltiples campos
- ✅ **Tema visual personalizado** para panadería
- ✅ **Responsive design** adaptativo

### En Desarrollo 🔄
- 🔄 Formularios de alta/edición
- 🔄 Validación de campos
- 🔄 Notificaciones visuales (toasts)
- 🔄 Exportación a PDF
- 🔄 Gráficos y estadísticas

### Planificadas 📝
- 📝 Gestión de usuarios y permisos
- 📝 Reportes personalizados
- 📝 Tema oscuro
- 📝 Exportación a Excel
- 📝 Impresión de documentos

---

## 📁 Estructura del Proyecto

```
ERP/
├── src/
│   ├── main/
│   │   ├── java/alicanteweb/erp/
│   │   │   ├── ErpLauncher.java          ← Punto de entrada
│   │   │   ├── controller/               ← Controladores UI
│   │   │   ├── entities/                 ← Entidades JPA
│   │   │   ├── repository/               ← Repositorios
│   │   │   └── service/                  ← Servicios de negocio
│   │   └── resources/
│   │       ├── ui/                       ← Vistas FXML (7)
│   │       ├── css/                      ← Estilos CSS
│   │       ├── images/                   ← Iconos e imágenes
│   │       └── application.properties    ← Configuración
│   └── test/                             ← Tests
├── docs/                                 ← Documentación
├── basesdedatos/                         ← Scripts SQL
├── run.bat                               ← Script de ejecución
├── run-jar.bat                           ← Script alternativo
└── pom.xml                               ← Configuración Maven
```

---

## ⚙️ Requisitos

### Software Necesario
- ☕ **Java 17** o superior
- 📦 **Maven 3.6+**
- 🗄️ **MySQL 8.0+** o **MariaDB**
- 💻 **Windows** (o adaptar scripts para Linux/Mac)

### Opcional
- 🔧 **IntelliJ IDEA** (recomendado)
- 🎨 **Scene Builder** (para editar FXML)

---

## 🔧 Configuración

### Base de Datos MySQL

1. **Crear la base de datos**
```sql
CREATE DATABASE tahonaerp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Importar datos**
```bash
mysql -u root -p tahonaerp < basesdedatos/definitivo/tahonaerp.sql
```

3. **Configurar credenciales**

Editar `src/main/resources/application-dev.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tahonaerp
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

### Perfiles de Ejecución

- **Desarrollo con MySQL**: `application-dev.properties`
- **Desarrollo con H2**: `application-dev-h2.properties`
- **Producción**: `application.properties`

---

## 🎨 Tema Visual

### Paleta de Colores
- 🟡 **Primario (Dorado)**: `#D4922B` - Representa el pan dorado
- 🟤 **Secundario (Marrón)**: `#8B4513` - Color del pan
- 🟢 **Éxito**: `#28A745`
- 🔵 **Información**: `#17A2B8`
- 🟡 **Advertencia**: `#FFC107`
- 🔴 **Peligro**: `#DC3545`
- 🟣 **Verifactu**: `#6F42C1`

### Tipografía
- **Principal**: Segoe UI, Roboto, sans-serif
- **Tamaño base**: 14px
- **Títulos**: 18-24px

---

## 🐛 Solución de Problemas

### La aplicación no arranca
```bash
# Verificar versión de Java
java -version

# Limpiar y recompilar
mvn clean compile
```

### Error de módulo JavaFX
```bash
# Usar el script run.bat
.\run.bat
```

### Los datos no se muestran
1. Verificar que MySQL está corriendo
2. Revisar credenciales en `application-dev.properties`
3. Comprobar que la base de datos tiene datos
4. Ver logs en consola

### Error de compilación
```bash
# Limpiar proyecto
mvn clean

# Recompilar
mvn compile -DskipTests
```

---

## 📊 Estadísticas del Proyecto

| Categoría | Cantidad |
|-----------|----------|
| Vistas FXML | 7 |
| Controladores | 7 |
| Entidades JPA | 15+ |
| Servicios | 7+ |
| Líneas de CSS | 500+ |
| Módulos | 6 |
| Iconos | 11+ |

---

## 🤝 Contribuir

Este es un proyecto privado para Panadería Tahona. Para sugerencias o mejoras:

1. Revisar la documentación en `/docs`
2. Consultar las guías de diseño UI/UX
3. Seguir los patrones establecidos
4. Documentar cambios

---

## 📝 Licencia

Proyecto propietario - Panadería Tahona © 2025

---

## 📞 Soporte

### Documentación
- Ver carpeta `/docs` para guías detalladas
- Consultar `INICIO_RAPIDO.md` para empezar
- Revisar `README_FRONTEND.md` para la UI

### Logs
- Archivo: `run_error.log`
- Consola: Salida estándar

---

## 🎓 Recursos de Aprendizaje

- [JavaFX Documentation](https://openjfx.io/)
- [Spring Boot Guide](https://spring.io/guides)
- [Material Design](https://material.io/design)
- [FontAwesome Icons](https://fontawesome.com/icons)

---

## 🎉 ¡Empezar Ahora!

```bash
# 1. Clonar/Abrir proyecto
cd D:\Programación\ERP

# 2. Ejecutar
.\run.bat

# 3. ¡Disfrutar del ERP moderno!
```

---

**Desarrollado con ❤️ para Panadería Tahona**  
**Versión 1.0.0** | **Diciembre 2025**

🍞 **¡Bienvenido al futuro de la gestión de panaderías!** 🥐

#   E R P T a h o n a  
 