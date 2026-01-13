# 🏢 ERP Panadería Tahona

Sistema ERP completo para gestión de panadería/pastelería desarrollado con **Spring Boot** y **JavaFX**.

---

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 17 o superior
- Maven 3.8+
- MySQL 8.0+
- IntelliJ IDEA (recomendado)

### Configuración de Base de Datos

1. **Crear base de datos:**
```sql
CREATE DATABASE tahona CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Configurar conexión:**
Editar `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tahona
spring.datasource.username=root
spring.datasource.password=tu_contraseña
```

3. **Inicializar Plan Contable (primera vez):**
```bash
INIT_PLAN_CONTABLE.bat
```

### Arrancar la Aplicación

```bash
# Opción 1: Script directo
ARRANCAR.bat

# Opción 2: Maven
mvn javafx:run

# Opción 3: IntelliJ IDEA
ABRIR_EN_INTELLIJ.bat
```

### Credenciales de Login
```
Usuario: admin
Contraseña: admin
```

---

## 📦 Módulos Implementados

### ✅ Completamente Funcionales

| Módulo | Funcionalidad |
|--------|---------------|
| **Clientes** | Crear, editar, listar, buscar clientes |
| **Artículos** | Gestión completa con precios, stock, IVA |
| **Proveedores** | Gestión de proveedores |
| **Facturas** | Emisión, edición, impresión de facturas |
| **Usuarios** | Control de acceso, roles, permisos |
| **Almacenes** | Control de ubicaciones y stock |
| **Auditoría** | Registro completo de todas las operaciones |

### ⚠️ Parcialmente Implementados

| Módulo | Estado |
|--------|--------|
| **Albaranes** | Lista funcional, formularios pendientes |
| **Presupuestos** | Lista funcional, formularios pendientes |
| **Pedidos** | Lista funcional, formularios pendientes |
| **Caja** | Lista funcional, formularios pendientes |
| **Contabilidad** | Asientos automáticos, manual pendiente |
| **VeriFacTu** | Simulado, integración real pendiente |

---

## 🛠️ Tecnologías

- **Backend:** Spring Boot 3.5.7
- **Frontend:** JavaFX 21
- **Base de Datos:** MySQL 8.0 + Hibernate/JPA
- **Seguridad:** BCrypt para contraseñas
- **Logging:** SLF4J + Logback
- **Build:** Maven

---

## 📁 Estructura del Proyecto

```
ERP/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── alicanteweb/erp/
│   │   │       ├── controller/     # Controladores JavaFX
│   │   │       ├── entities/       # Entidades JPA
│   │   │       ├── repository/     # Repositorios Spring Data
│   │   │       ├── service/        # Lógica de negocio
│   │   │       └── ErpLauncher.java
│   │   └── resources/
│   │       ├── ui/                 # Archivos FXML
│   │       ├── styles/             # CSS
│   │       └── application.properties
│   └── test/                       # Tests
├── docs/                           # Documentación
├── scripts/                        # Scripts SQL
├── basesdedatos/                   # Backups y SQL
├── pom.xml                         # Configuración Maven
└── README.md
```

---

## 📚 Documentación

La documentación completa está en la carpeta `docs/`:

- **FORMULARIOS_CORREGIDOS_FINAL.md** - Estado de todos los formularios
- **PROBLEMA_RESUELTO.md** - Últimas correcciones aplicadas
- **ESTADO_FORMULARIOS.md** - Lista completa de módulos
- **README_CORRECCIONES.md** - Resumen de correcciones

---

## 🔧 Desarrollo

### Compilar
```bash
mvn clean compile
```

### Ejecutar Tests
```bash
mvn test
```

### Generar JAR
```bash
mvn clean package
```

### Ver Logs
Los logs se guardan en `logs/` automáticamente.

---

## ✅ Últimas Correcciones

### Filtros de Categoría y Estado en Artículos (2026-01-13)
- ✅ Implementados filtros por Categoría (7 opciones)
- ✅ Implementados filtros por Estado (Todos/Activos/Inactivos)
- ✅ Combinación de múltiples filtros (texto + categoría + estado)
- ✅ Filtrado automático al cambiar selección
- ✅ Contador actualizado de resultados

### Corrección Botón "Dar de Baja" en Artículos (2026-01-13)
- ✅ Implementado método `onDarBaja()` que faltaba
- ✅ Ahora se pueden dar de baja y reactivar artículos
- ✅ Implementado método `onVer()` para ver detalles
- ✅ Validaciones y mensajes de confirmación

### Corrección de Campos Numéricos (2026-01-13)
- ✅ Solucionado problema del cursor saltando en campos de precio
- ✅ Solucionado cálculo automático que impedía escribir
- ✅ Validación mejorada de campos decimales

### Integración de Formularios
- ✅ UsuarioFormController integrado completamente
- ✅ Todos los formularios principales funcionan

### Limpieza del Proyecto (2026-01-13)
- ✅ 112 archivos obsoletos movidos a `docs_obsoletos/`
- ✅ Documentación organizada en `docs/`
- ✅ Estructura profesional del proyecto

---

## 🎯 Cumplimiento Legal (España)

- ✅ IVA configurado (4%, 10%, 21%)
- ✅ Numeración de facturas
- ✅ Auditoría completa de operaciones
- ✅ Plan Contable Español
- ⚠️ Modelo 347 (implementado, necesita ajustes)
- ⚠️ VeriFacTu (simulado, integración real pendiente)

---

## 🤝 Soporte

Para reportar problemas o solicitar funcionalidades:
1. Revisar la documentación en `docs/`
2. Verificar que la base de datos esté correctamente configurada
3. Consultar los logs en `logs/`

---

## 📝 Licencia

Proyecto privado - Todos los derechos reservados.

---

## 🏗️ Estado del Proyecto

**Versión:** 0.0.1  
**Estado:** ✅ Funcional para producción  
**Última actualización:** 13 de enero de 2026

**Funcionalidad:** 70% completo
- Core business: 100%
- Funciones avanzadas: 40%

