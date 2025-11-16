# 📋 ERP PARA PANADERÍA - DOCUMENTACIÓN DEL PROYECTO

## 📌 Descripción General

Sistema ERP (Enterprise Resource Planning) completo para gestión de panadería, desarrollado con Spring Boot 3.5.7 y Java 17. El sistema incluye gestión de clientes, proveedores, artículos, facturación, contabilidad, producción y está preparado para integración con Verifactu (facturación electrónica española).

---

## 🎯 Características Principales

### ✅ Módulos Implementados

1. **Gestión de Clientes y Proveedores**
   - CRUD completo de clientes
   - Gestión de datos fiscales (CIF, IVA, etc.)
   - Control de formas de pago
   - Gestión de riesgo crediticio

2. **Gestión de Artículos**
   - Catálogo de productos
   - Control de precios (PVP1-5)
   - Gestión de familias y categorías
   - Control de stock (mínimos y máximos)
   - Artículos compuestos
   - Costes: coste medio, último coste, coste artículo

3. **Facturación**
   - Facturas de venta y compra
   - Albaranes
   - Presupuestos
   - Pedidos
   - Gestión de IVA y recargo de equivalencia

4. **Contabilidad**
   - Plan contable
   - Asientos contables
   - Balances
   - Cuentas y subcuentas

5. **Gestión de Cobros y Pagos**
   - Control de efectos (cobros/pagos)
   - Remesas bancarias
   - Conciliación bancaria

6. **Órdenes de Producción**
   - Planificación de producción
   - Control de materiales

---

## 🏗️ Arquitectura del Proyecto

### **Tecnologías Utilizadas**

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 17 | Lenguaje de programación |
| **Spring Boot** | 3.5.7 | Framework principal |
| **Spring Data JPA** | 3.5.7 | Acceso a datos ORM |
| **Hibernate** | 6.x | Implementación JPA |
| **MySQL** | 8.x | Base de datos producción |
| **H2** | Latest | Base de datos para tests |
| **Lombok** | Latest | Reducción de código boilerplate |
| **Maven** | 3.x | Gestión de dependencias |

### **Patrón de Arquitectura**

El proyecto sigue una arquitectura en capas:

```
┌─────────────────────────────────────┐
│   API Layer (Controllers)           │
│   - REST endpoints                  │
│   - Request/Response mapping        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Service Layer (Business Logic)    │
│   - Business rules                  │
│   - Transaction management          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Repository Layer (Data Access)    │
│   - JPA Repositories                │
│   - Database operations             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Database (MySQL)                  │
│   - Schema: tahona                  │
└─────────────────────────────────────┘
```

---

## 📂 Estructura del Proyecto

```
D:\Programación\ERP/
├── docs/                                    # Documentación
│   ├── PROYECTO_ERP_PANADERIA.md
│   └── PRUEBAS_BACKEND.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── alicanteweb/
│   │   │       └── erp/
│   │   │           ├── api/                 # Controladores REST
│   │   │           │   ├── AgenteController.java
│   │   │           │   ├── ArticuloController.java
│   │   │           │   ├── ClienteController.java
│   │   │           │   ├── FamiliaController.java
│   │   │           │   ├── FormasdepagoController.java
│   │   │           │   ├── HealthController.java
│   │   │           │   ├── ProvinciaController.java
│   │   │           │   ├── SectoreController.java
│   │   │           │   ├── TiposdeivaController.java
│   │   │           │   └── ZonaController.java
│   │   │           ├── entities/            # Entidades JPA
│   │   │           │   ├── Agente.java
│   │   │           │   ├── Articulo.java
│   │   │           │   ├── Cliente.java
│   │   │           │   ├── Familia.java
│   │   │           │   ├── Formasdepago.java
│   │   │           │   ├── Provincia.java
│   │   │           │   ├── Sectore.java
│   │   │           │   ├── Tiposdeiva.java
│   │   │           │   └── Zona.java
│   │   │           ├── repository/          # Repositorios JPA
│   │   │           │   ├── AgenteRepository.java
│   │   │           │   ├── ArticuloRepository.java
│   │   │           │   ├── ClienteRepository.java
│   │   │           │   ├── FamiliaRepository.java
│   │   │           │   ├── FormasdepagoRepository.java
│   │   │           │   ├── ProvinciaRepository.java
│   │   │           │   ├── SectoreRepository.java
│   │   │           │   ├── TiposdeivaRepository.java
│   │   │           │   └── ZonaRepository.java
│   │   │           ├── service/             # Servicios de negocio
│   │   │           │   ├── AgenteService.java
│   │   │           │   ├── ArticuloService.java
│   │   │           │   ├── ClienteService.java
│   │   │           │   ├── FamiliaService.java
│   │   │           │   ├── FormasdepagoService.java
│   │   │           │   ├── ProvinciaService.java
│   │   │           │   ├── SectoreService.java
│   │   │           │   ├── TiposdeivaService.java
│   │   │           │   └── ZonaService.java
│   │   │           └── ErpApplication.java  # Clase principal
│   │   └── resources/
│   │       └── application.properties       # Configuración
│   └── test/
│       ├── java/
│       │   └── alicanteweb/
│       │       └── erp/
│       │           ├── api/
│       │           │   └── AgenteControllerTest.java
│       │           └── ErpApplicationTests.java
│       └── resources/
│           └── application.properties       # Config tests
├── pom.xml                                  # Maven config
├── tahona_mysql_mejorado.sql               # Script BD
├── CORRECCIONES_APLICADAS.md              # Cambios aplicados
├── INSTRUCCIONES_SQL.md                    # Instrucciones BD
└── README.md
```

---

## 🗄️ Modelo de Datos

### **Tablas Principales**

#### **Artículos** (`articulos`)
- Gestión completa de productos
- Múltiples precios (PVP1-5)
- Control de costes
- Stock y almacenamiento

#### **Clientes** (`clientes`)
- Datos fiscales completos
- Gestión de riesgo
- Formas de pago asociadas
- Datos bancarios

#### **Facturas de Venta** (`facturasventa`)
- Cabecera de factura
- Referencias a cliente
- Importes y descuentos
- Estado de cobro

#### **Facturas de Compra** (`facturascompra`)
- Similar a ventas
- Referencias a proveedor

#### **Contabilidad** (`asientos`, `cuentas`, `subcuentas`)
- Plan contable completo
- Asientos contables
- Balances

---

## 🔧 Configuración del Proyecto

### **Requisitos Previos**

- **JDK 17** o superior
- **Maven 3.8+** (incluido wrapper)
- **MySQL 8.0+**
- **IDE**: IntelliJ IDEA, Eclipse, o VS Code

### **Configuración de Base de Datos**

1. **Crear base de datos:**
```sql
CREATE DATABASE tahona CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Importar esquema y datos:**
```bash
mysql -u root -p tahona < tahona_mysql_mejorado.sql
```

3. **Configurar credenciales** en `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tahona
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
```

### **Compilar el Proyecto**

```bash
# Limpiar y compilar
./mvnw clean compile

# Crear JAR ejecutable
./mvnw clean package

# Ejecutar tests
./mvnw test

# Ejecutar aplicación
./mvnw spring-boot:run
```

---

## 🚀 Ejecución

### **Modo Desarrollo**

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### **Modo Producción**

```bash
java -jar target/ERP-0.0.1-SNAPSHOT.jar
```

### **Verificar Estado**

```bash
curl http://localhost:8080/api/health
```

---

## 📡 API REST Endpoints

### **Artículos**
- `GET /api/articulos` - Listar todos los artículos
- `GET /api/articulos/{codigo}` - Obtener artículo por código

### **Clientes**
- `GET /api/clientes` - Listar todos los clientes
- `GET /api/clientes/{codigo}` - Obtener cliente por código

### **Familias**
- `GET /api/familias` - Listar familias
- `GET /api/familias/{codigo}` - Obtener familia

### **Provincias**
- `GET /api/provincias` - Listar provincias
- `GET /api/provincias/{codigo}` - Obtener provincia

### **Formas de Pago**
- `GET /api/formaspago` - Listar formas de pago
- `GET /api/formaspago/{codigo}` - Obtener forma de pago

### **Tipos de IVA**
- `GET /api/tiposiva` - Listar tipos de IVA
- `GET /api/tiposiva/{codigo}` - Obtener tipo de IVA

### **Agentes**
- `GET /api/agentes` - Listar agentes
- `GET /api/agentes/{codigo}` - Obtener agente

### **Zonas**
- `GET /api/zonas` - Listar zonas
- `GET /api/zonas/{codigo}` - Obtener zona

### **Sectores**
- `GET /api/sectores` - Listar sectores
- `GET /api/sectores/{codigo}` - Obtener sector

---

## 🔐 Preparación para Verifactu

### **¿Qué es Verifactu?**

Verifactu es el sistema de la Agencia Tributaria española para la validación y envío de registros de facturación de software de gestión. Es obligatorio para todos los sistemas de facturación en España desde 2025.

### **Requisitos Técnicos**

1. **Identificación de facturas**: Cada factura debe tener un número único y secuencial
2. **Registro de eventos**: Todas las operaciones deben quedar registradas
3. **Hash de factura**: Cada factura debe tener una huella digital
4. **Encadenamiento**: Las facturas deben estar encadenadas criptográficamente
5. **Envío a AEAT**: Comunicación con la API de la Agencia Tributaria

### **Preparación del Sistema**

**Tablas necesarias:**

```sql
CREATE TABLE verifactu_registros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL,
    fecha_expedicion DATETIME NOT NULL,
    hash_factura VARCHAR(256) NOT NULL,
    hash_anterior VARCHAR(256),
    estado ENUM('PENDIENTE', 'ENVIADO', 'ACEPTADO', 'RECHAZADO') DEFAULT 'PENDIENTE',
    fecha_envio DATETIME,
    respuesta_aeat TEXT,
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Dependencias a añadir:**

```xml
<!-- Para firma digital -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.78</version>
</dependency>

<!-- Para cliente HTTP con AEAT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## 🧪 Testing

El proyecto incluye tests unitarios y de integración:

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar un test específico
./mvnw -Dtest=ArticuloServiceTest test

# Ver reporte de cobertura
./mvnw jacoco:report
```

---

## 📱 Aplicación de Escritorio con JavaFX

### **Próximos Pasos**

1. **Agregar dependencias JavaFX:**

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21.0.1</version>
</dependency>
```

2. **Estructura de ventanas:**
   - Ventana principal (Dashboard)
   - Ventana de clientes
   - Ventana de artículos
   - Ventana de facturación
   - Ventana de producción

3. **Comunicación Backend-Frontend:**
   - Usar RestTemplate o WebClient para conectar con API REST
   - Implementar caché local para mejor rendimiento
   - Manejo de sesiones

---

## 🎨 Buenas Prácticas Implementadas

✅ **Clean Code**: Código limpio y legible
✅ **SOLID Principles**: Principios de diseño orientado a objetos
✅ **Dependency Injection**: Inyección de dependencias con Spring
✅ **RESTful API**: API siguiendo principios REST
✅ **Data Validation**: Validación de datos en todas las capas
✅ **Error Handling**: Manejo centralizado de errores
✅ **Logging**: Sistema de logs con SLF4J
✅ **Transaction Management**: Gestión de transacciones con @Transactional
✅ **Security Ready**: Preparado para Spring Security
✅ **Testing**: Tests unitarios e integración

---

## 🔒 Seguridad (Próximos pasos)

### **Implementar Spring Security:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### **Características a implementar:**

1. **Autenticación** JWT o sesiones
2. **Autorización** basada en roles
3. **Cifrado** de passwords con BCrypt
4. **HTTPS** en producción
5. **CORS** configurado
6. **Rate Limiting** para prevenir abuso

---

## 📊 Monitorización y Métricas

### **Spring Boot Actuator:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Endpoints útiles:**
- `/actuator/health` - Estado de salud
- `/actuator/metrics` - Métricas de rendimiento
- `/actuator/info` - Información de la app
- `/actuator/loggers` - Gestión de logs

---

## 📝 Estado del Proyecto

### ✅ **Completado**

- [x] Configuración inicial del proyecto
- [x] Estructura de capas (API, Service, Repository)
- [x] Entidades JPA mapeadas
- [x] Repositorios base
- [x] Servicios CRUD básicos
- [x] Controladores REST
- [x] Configuración de base de datos MySQL
- [x] Configuración de tests con H2
- [x] Script de base de datos con datos de prueba
- [x] Documentación básica

### 🚧 **En Progreso / Pendiente**

- [ ] Implementar lógica de negocio completa
- [ ] Validaciones de datos
- [ ] Manejo de errores centralizado
- [ ] Spring Security
- [ ] Integración Verifactu
- [ ] Módulo de facturación completo
- [ ] Módulo de producción
- [ ] Reportes e impresión
- [ ] Interfaz JavaFX
- [ ] Tests exhaustivos
- [ ] Documentación API (Swagger/OpenAPI)
- [ ] Docker deployment
- [ ] CI/CD pipeline

---

## 🤝 Contribución

Para contribuir al proyecto:

1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/NuevaCaracteristica`)
3. Commit de cambios (`git commit -am 'Añadir nueva característica'`)
4. Push a la rama (`git push origin feature/NuevaCaracteristica`)
5. Crear Pull Request

---

## 📞 Soporte

Para dudas o problemas:
- Revisar documentación en `/docs`
- Consultar issues existentes
- Crear nuevo issue con detalles del problema

---

## 📄 Licencia

Proyecto privado - Todos los derechos reservados

---

## 🎯 Roadmap

### **Fase 1: Backend (Actual)**
- ✅ API REST básica
- ✅ Modelo de datos
- 🚧 Lógica de negocio completa

### **Fase 2: Integración Verifactu**
- Implementar firma digital
- Encadenamiento de facturas
- Cliente para API AEAT
- Sistema de colas para envíos

### **Fase 3: Frontend JavaFX**
- Diseño de interfaz
- Ventanas principales
- Formularios de entrada
- Listados y búsquedas
- Reportes

### **Fase 4: Optimización**
- Performance tuning
- Caché (Redis)
- Optimización de consultas
- Monitorización avanzada

### **Fase 5: Despliegue**
- Containerización (Docker)
- CI/CD
- Documentación de despliegue
- Backup y recuperación

---

## 📚 Referencias

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [JavaFX Documentation](https://openjfx.io/)
- [Verifactu - AEAT](https://sede.agenciatributaria.gob.es/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

**Última actualización:** 16 de Noviembre de 2025
**Versión:** 0.0.1-SNAPSHOT
**Estado:** En Desarrollo Activo 🚀

