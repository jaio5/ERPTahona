# 📚 DOCUMENTACIÓN COMPLETA - ERP PANADERÍA TAHONA

**Versión:** 0.0.1  
**Fecha:** Diciembre 2025  
**Desarrollador:** AlicanteWeb  
**Licencia:** Propietario

---

## 📋 ÍNDICE

1. [Descripción General](#descripción-general)
2. [Características Principales](#características-principales)
3. [Módulos Implementados](#módulos-implementados)
4. [Módulos Pendientes](#módulos-pendientes)
5. [Arquitectura Técnica](#arquitectura-técnica)
6. [Requisitos del Sistema](#requisitos-del-sistema)
7. [Instalación](#instalación)
8. [Configuración](#configuración)
9. [Manual de Usuario](#manual-de-usuario)
10. [Manual Técnico](#manual-técnico)
11. [Cumplimiento Legal](#cumplimiento-legal)
12. [Seguridad](#seguridad)
13. [Backup y Recuperación](#backup-y-recuperación)
14. [Troubleshooting](#troubleshooting)
15. [Roadmap](#roadmap)

---

## 🎯 DESCRIPCIÓN GENERAL

**ERP Panadería Tahona** es un sistema de gestión empresarial (ERP) completo diseñado específicamente para negocios del sector alimentario (panaderías, pastelerías, etc.) que cumple con toda la normativa fiscal y legal española vigente.

### Objetivo Principal
Proporcionar una solución integral para la gestión de ventas, facturación, inventario, clientes y cumplimiento fiscal, con especial énfasis en:
- Cumplimiento con la normativa española (AEAT)
- Sistema Verifactu para facturación electrónica
- RGPD (Protección de Datos)
- Auditoría completa de operaciones
- Interfaz intuitiva y moderna con JavaFX

---

## ✨ CARACTERÍSTICAS PRINCIPALES

### ✅ Implementadas

#### 1. **Gestión de Usuarios**
- Sistema de autenticación seguro con Spring Security
- Encriptación de contraseñas con BCrypt
- Roles y permisos (ADMIN, USUARIO, VENDEDOR, CONTABLE)
- Control de intentos fallidos y bloqueo automático
- Auditoría completa de accesos

#### 2. **Gestión de Clientes**
- Alta, baja y modificación de clientes
- Búsqueda avanzada con filtros
- Historial de operaciones
- Cumplimiento RGPD con consentimientos
- Exportación de datos

#### 3. **Gestión de Artículos**
- Catálogo completo de productos
- Códigos de barras
- Precios con IVA
- Control de stock básico
- Categorización

#### 4. **Gestión de Proveedores**
- Registro completo de proveedores
- Datos fiscales
- Historial de compras

#### 5. **Gestión de Albaranes**
- Creación de albaranes de venta
- Líneas de detalle
- Conversión a factura
- Impresión en PDF

#### 6. **Facturación Completa**
- Emisión de facturas conforme a ley
- Estados: BORRADOR, PENDIENTE_REVISION, APROBADA, ENVIADA_AEAT, REGISTRADA_AEAT, CANCELADA
- Numeración automática por año
- Cálculo automático de IVA y totales
- Vinculación con albaranes
- Impresión en PDF con código QR
- Sistema de revisión y aprobación

#### 7. **Integración Verifactu (AEAT)**
- Generación de evidencias electrónicas
- Firma digital de facturas
- Cadena de custodia (blockchain interno)
- Envío automático a AEAT (simulado)
- Cumplimiento con la normativa de facturación electrónica

#### 8. **RGPD**
- Registro de consentimientos
- Control de acceso a datos personales
- Gestión de solicitudes de ejercicio de derechos
- Auditoría de accesos a datos personales

#### 9. **Auditoría**
- Registro completo de todas las operaciones
- Trazabilidad de cambios
- Logs detallados con:
  - Usuario
  - Fecha y hora
  - Acción realizada
  - Valores anteriores y nuevos
  - Resultado (éxito/fallo)
  - IP y User-Agent

#### 10. **Impresión**
- Facturas en PDF
- Albaranes en PDF
- Formato profesional
- Código QR para verificación
- Soporte UTF-8 completo

#### 11. **Interfaz de Usuario (JavaFX)**
- Diseño moderno con CSS
- Tema oscuro profesional
- Navegación intuitiva
- Formularios validados
- Tablas con búsqueda y filtrado
- Notificaciones visuales

#### 12. **Seguridad**
- Encriptación de contraseñas
- Protección contra ataques (SQL injection, XSS)
- Sesiones seguras
- Cifrado de datos sensibles
- Backup automático

---

## 📦 MÓDULOS IMPLEMENTADOS

### 1. **Módulo de Autenticación**
**Ubicación:** `service/AutenticacionService.java`

**Funcionalidades:**
- Login con usuario y contraseña
- Validación de credenciales
- Control de intentos fallidos (máximo 5)
- Bloqueo automático tras intentos fallidos
- Desbloqueo automático del usuario admin al iniciar
- Registro en auditoría de todos los intentos

**Entidades:**
- `Usuario` - Datos del usuario
- `Rol` - Roles del sistema

### 2. **Módulo de Clientes**
**Ubicación:** `controller/ClienteController.java`, `service/ClienteService.java`

**Funcionalidades:**
- CRUD completo de clientes
- Búsqueda por código, nombre, CIF
- Filtrado por activo/inactivo
- Exportación de datos
- Validación de CIF
- Historial de compras

**Entidades:**
- `Cliente` - Datos del cliente
- `DireccionenvioNew` - Direcciones de envío

### 3. **Módulo de Artículos**
**Ubicación:** `controller/ArticuloController.java`, `service/ArticuloService.java`

**Funcionalidades:**
- CRUD completo de artículos
- Gestión de precios e IVA
- Códigos de barras
- Control de stock
- Búsqueda avanzada

**Entidades:**
- `Articulo` - Datos del artículo
- `Almacen` - Control de stock

### 4. **Módulo de Proveedores**
**Ubicación:** `controller/ProveedorController.java`, `service/ProveedorService.java`

**Funcionalidades:**
- CRUD completo de proveedores
- Datos fiscales
- Historial

**Entidades:**
- `Proveedor` - Datos del proveedor

### 5. **Módulo de Albaranes**
**Ubicación:** `controller/AlbaranController.java`, `service/AlbaranVentaService.java`

**Funcionalidades:**
- Creación de albaranes
- Líneas de detalle
- Conversión a factura
- Impresión PDF

**Entidades:**
- `AlbaranVenta` - Cabecera del albarán
- `AlbaranVentaLinea` - Líneas del albarán
- `AlbaranVentaFactura` - Relación albarán-factura

### 6. **Módulo de Facturación**
**Ubicación:** `controller/FacturaController.java`, `service/FacturaService.java`

**Funcionalidades:**
- Emisión de facturas
- Estados de factura con workflow
- Cálculo automático de totales
- Integración con Verifactu
- Impresión con QR
- Workflow de aprobación:
  1. BORRADOR → Creación inicial
  2. PENDIENTE_REVISION → Solicitud de revisión
  3. APROBADA → Aprobada para envío
  4. ENVIADA_AEAT → Enviada a AEAT
  5. REGISTRADA_AEAT → Confirmada por AEAT

**Entidades:**
- `Factura` - Cabecera de factura
- `FacturaLinea` - Líneas de factura
- `EstadoFactura` - Estados posibles
- `FacturaAlbaran` - Relación factura-albarán

### 7. **Módulo Verifactu**
**Ubicación:** `service/VerifactuService.java`, `service/VerifactuAEATService.java`

**Funcionalidades:**
- Generación de evidencias
- Firma digital
- Cadena de custodia
- Envío a AEAT (simulado)
- Verificación de integridad

**Entidades:**
- `VerifactuEvidence` - Evidencias digitales

### 8. **Módulo RGPD**
**Ubicación:** `service/RgpdConsentimientoService.java`, `service/RgpdAccesoDatosService.java`

**Funcionalidades:**
- Registro de consentimientos
- Control de accesos
- Gestión de solicitudes
- Exportación de datos

**Entidades:**
- `RgpdConsentimiento` - Consentimientos
- `RgpdAccesoDatos` - Accesos a datos
- `RgpdSolicitud` - Solicitudes RGPD

### 9. **Módulo de Auditoría**
**Ubicación:** `service/AuditoriaService.java`

**Funcionalidades:**
- Registro automático de operaciones
- Consulta de historial
- Exportación de logs

**Entidades:**
- `AuditoriaAccion` - Registro de acciones

### 10. **Módulo de Impresión**
**Ubicación:** `service/PrintService.java`, `service/QrCodeService.java`

**Funcionalidades:**
- Generación de PDFs
- Códigos QR
- Plantillas personalizables
- Soporte UTF-8

---

## 🚧 MÓDULOS PENDIENTES (PARA SER ERP COMPLETO)

### ❌ No Implementados - CRÍTICOS

#### 1. **Módulo de Contabilidad**
**Prioridad:** ALTA

**Funcionalidades necesarias:**
- Plan contable español (PGC 2007)
- Asientos contables automáticos
- Libro diario y mayor
- Balance de situación
- Cuenta de pérdidas y ganancias
- Conciliación bancaria
- Cierre de ejercicio

**Impacto:** Sin contabilidad integrada, no es un ERP completo. Se debe integrar o usar software externo.

#### 2. **Módulo de Tesorería**
**Prioridad:** ALTA

**Funcionalidades necesarias:**
- Control de caja
- Movimientos bancarios
- Pagos a proveedores
- Cobros de clientes
- Previsión de tesorería
- Conciliación bancaria
- Remesas SEPA

**Impacto:** Esencial para control financiero diario.

#### 3. **Módulo de Compras**
**Prioridad:** ALTA

**Funcionalidades necesarias:**
- Pedidos a proveedores
- Albaranes de compra
- Facturas de compra
- Control de recepciones
- Gestión de devoluciones
- Comparación de precios

**Impacto:** Actualmente solo hay gestión de ventas, falta el flujo de compras.

#### 4. **Módulo de Almacén (Avanzado)**
**Prioridad:** MEDIA-ALTA

**Funcionalidades necesarias:**
- Ubicaciones de almacén
- Control de lotes y caducidades (crítico en panadería)
- Trazabilidad completa
- Inventarios periódicos
- Movimientos entre almacenes
- Valoración de stocks (FIFO, PMP)
- Alertas de stock mínimo

**Impacto:** Actualmente hay control básico de stock, pero falta gestión avanzada.

#### 5. **Módulo de Producción**
**Prioridad:** MEDIA (específico para panadería)

**Funcionalidades necesarias:**
- Fichas técnicas de productos
- Gestión de recetas
- Cálculo de costes de producción
- Planificación de producción
- Control de mermas
- Órdenes de fabricación

**Impacto:** Específico del sector alimentario, permite calcular costes reales.

#### 6. **Módulo Fiscal Avanzado**
**Prioridad:** ALTA

**Funcionalidades necesarias:**
- Modelo 303 (IVA trimestral)
- Modelo 347 (Operaciones con terceros)
- Modelo 349 (Operaciones intracomunitarias)
- Modelo 390 (Resumen anual IVA)
- Libros de registro IVA
- SII (Suministro Inmediato de Información)
- Exportación a formato AEAT

**Impacto:** Crítico para cumplimiento fiscal completo.

#### 7. **Módulo de Pedidos de Clientes**
**Prioridad:** MEDIA

**Funcionalidades necesarias:**
- Gestión de pedidos pendientes
- Reserva de stock
- Seguimiento de entregas
- Presupuestos
- Conversión presupuesto → pedido → albarán → factura

**Impacto:** Mejora flujo comercial.

#### 8. **Módulo de TPV (Terminal Punto de Venta)**
**Prioridad:** MEDIA-ALTA

**Funcionalidades necesarias:**
- Venta rápida en mostrador
- Cobro con múltiples formas de pago
- Apertura y cierre de caja
- Tickets de venta
- Integración con cajón portamonedas
- Lectura de códigos de barras
- Pantalla de cliente

**Impacto:** Esencial para venta al público directa.

#### 9. **Módulo de Recursos Humanos**
**Prioridad:** BAJA-MEDIA

**Funcionalidades necesarias:**
- Ficha de empleados
- Control de horarios
- Nóminas
- Vacaciones y ausencias
- Contratos

**Impacto:** Depende del tamaño de la empresa.

#### 10. **Módulo de CRM (Customer Relationship Management)**
**Prioridad:** BAJA-MEDIA

**Funcionalidades necesarias:**
- Historial de contactos
- Oportunidades de venta
- Campañas de marketing
- Segmentación de clientes
- Programas de fidelización

**Impacto:** Mejora relación con clientes.

#### 11. **Módulo de Informes y BI**
**Prioridad:** MEDIA

**Funcionalidades necesarias:**
- Dashboard con KPIs
- Ventas por período
- Productos más vendidos
- Márgenes por producto
- Análisis ABC
- Gráficas interactivas
- Exportación Excel/PDF

**Impacto:** Mejora toma de decisiones.

#### 12. **Módulo Multi-empresa**
**Prioridad:** BAJA

**Funcionalidades necesarias:**
- Gestión de múltiples empresas
- Consolidación de datos
- Informes multi-empresa

**Impacto:** Solo si se gestiona más de una empresa.

---

## 🏗️ ARQUITECTURA TÉCNICA

### Stack Tecnológico

#### Backend
- **Spring Boot 3.5.7** - Framework principal
- **Spring Data JPA** - Capa de persistencia
- **Hibernate 6.6.33** - ORM
- **Spring Security** - Autenticación y autorización
- **BCrypt** - Encriptación de contraseñas

#### Frontend
- **JavaFX 21** - Framework de interfaz gráfica
- **FXML** - Definición de vistas
- **CSS** - Estilos personalizados

#### Base de Datos
- **MySQL 8.0** - Base de datos relacional
- **HikariCP** - Pool de conexiones

#### Librerías Adicionales
- **iText 5.5.13** - Generación de PDFs
- **ZXing 3.5.3** - Generación de códigos QR
- **BouncyCastle 1.70** - Criptografía
- **SLF4J + Logback** - Sistema de logs

### Arquitectura de Capas

```
┌─────────────────────────────────────────┐
│         CAPA DE PRESENTACIÓN            │
│  (JavaFX Controllers + FXML Views)      │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         CAPA DE SERVICIO                │
│  (Business Logic + Validaciones)        │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         CAPA DE DATOS                   │
│  (JPA Repositories + Entities)          │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         BASE DE DATOS                   │
│         (MySQL 8.0)                     │
└─────────────────────────────────────────┘
```

### Patrones de Diseño Utilizados

1. **MVC (Model-View-Controller)** - Separación de responsabilidades
2. **Repository Pattern** - Abstracción de acceso a datos
3. **Service Layer Pattern** - Lógica de negocio
4. **DTO (Data Transfer Object)** - Transferencia de datos
5. **Dependency Injection** - Inyección de dependencias con Spring
6. **Singleton** - Servicios de Spring
7. **Factory** - Creación de entidades complejas

---

## 💻 REQUISITOS DEL SISTEMA

### Requisitos Mínimos

#### Hardware
- **Procesador:** Intel Core i3 o equivalente
- **RAM:** 4 GB
- **Disco Duro:** 1 GB de espacio libre
- **Pantalla:** 1280x720 píxeles

#### Software
- **Sistema Operativo:** Windows 10/11, Linux, macOS
- **Java:** JDK 17 o superior
- **MySQL:** 8.0 o superior
- **Maven:** 3.8 o superior

### Requisitos Recomendados

#### Hardware
- **Procesador:** Intel Core i5 o superior
- **RAM:** 8 GB o más
- **Disco Duro:** SSD con 10 GB de espacio libre
- **Pantalla:** 1920x1080 píxeles

#### Software
- **Sistema Operativo:** Windows 11, Linux Ubuntu 22.04+
- **Java:** JDK 21
- **MySQL:** 8.0.35+
- **Maven:** 3.9+

---

## 🚀 INSTALACIÓN

### 1. Instalación de Prerrequisitos

#### Instalar Java JDK 17+
```bash
# Windows: Descargar desde https://adoptium.net/
# Linux:
sudo apt update
sudo apt install openjdk-17-jdk

# Verificar instalación:
java -version
```

#### Instalar MySQL 8.0
```bash
# Windows: Descargar desde https://dev.mysql.com/downloads/installer/
# Linux:
sudo apt install mysql-server

# Configurar MySQL:
sudo mysql_secure_installation
```

#### Instalar Maven
```bash
# Windows: Descargar desde https://maven.apache.org/download.cgi
# Linux:
sudo apt install maven

# Verificar instalación:
mvn -version
```

### 2. Configuración de Base de Datos

```sql
-- Conectar a MySQL como root
mysql -u root -p

-- Crear base de datos
CREATE DATABASE tahona CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario
CREATE USER 'tahona_user'@'localhost' IDENTIFIED BY 'TU_CONTRASEÑA_SEGURA';
GRANT ALL PRIVILEGES ON tahona.* TO 'tahona_user'@'localhost';
FLUSH PRIVILEGES;

-- Importar estructura
USE tahona;
SOURCE /ruta/a/basesdedatos/definitivo/tahonaerp.sql;
```

### 3. Configuración de la Aplicación

Editar `src/main/resources/application.properties`:

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?useSSL=false&serverTimezone=Europe/Madrid
spring.datasource.username=tahona_user
spring.datasource.password=TU_CONTRASEÑA_SEGURA

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Logs
logging.level.alicanteweb.erp=INFO
```

### 4. Compilar y Ejecutar

```bash
# Navegar al directorio del proyecto
cd D:\Programación\ERP

# Compilar
mvn clean compile

# Ejecutar
mvn javafx:run
```

### 5. Credenciales Iniciales

**Usuario:** `admin`  
**Contraseña:** `admin`

⚠️ **IMPORTANTE:** Cambiar la contraseña del administrador después del primer inicio.

---

## ⚙️ CONFIGURACIÓN

### Configuración de la Empresa

1. Iniciar sesión con usuario administrador
2. Ir a **Configuración → Empresa**
3. Completar datos fiscales:
   - Nombre fiscal
   - CIF
   - Dirección
   - Datos de contacto
   - Configuración AEAT (para Verifactu)

### Configuración de Verifactu

Para habilitar Verifactu (opcional):

1. Obtener certificado digital de la AEAT
2. Colocar el archivo `.p12` en `src/main/resources/certs/`
3. Configurar en la aplicación:
   - Ruta del certificado
   - Contraseña del certificado
   - URL de la AEAT

### Configuración de Impresión

Los PDFs se generan automáticamente en el directorio temporal del sistema.

Para personalizar plantillas:
- Editar `service/PrintService.java`
- Modificar fuentes, colores y diseño

---

## 📖 MANUAL DE USUARIO

### 1. Inicio de Sesión

1. Abrir la aplicación
2. Introducir usuario y contraseña
3. Pulsar "Iniciar Sesión"

**Notas:**
- Tras 5 intentos fallidos, el usuario se bloquea
- El usuario admin se desbloquea automáticamente al reiniciar la aplicación
- Contactar con el administrador para desbloquear otros usuarios

### 2. Gestión de Clientes

#### Crear Cliente
1. Ir a **Gestión → Clientes**
2. Pulsar botón **"Nuevo Cliente"**
3. Rellenar formulario:
   - Código (único)
   - Nombre o razón social
   - CIF/NIF
   - Dirección completa
   - Datos de contacto
4. Pulsar **"Guardar"**

#### Buscar Cliente
1. Ir a **Gestión → Clientes**
2. Escribir en el campo de búsqueda (código, nombre o CIF)
3. La tabla se filtrará automáticamente

#### Editar Cliente
1. Seleccionar cliente en la tabla
2. Pulsar **"Editar"**
3. Modificar datos
4. Pulsar **"Guardar"**

#### Dar de Baja Cliente
1. Seleccionar cliente en la tabla
2. Pulsar **"Dar de Baja"**
3. Confirmar acción
4. El cliente pasa a estado inactivo (no se elimina)

### 3. Gestión de Artículos

#### Crear Artículo
1. Ir a **Gestión → Artículos**
2. Pulsar **"Nuevo Artículo"**
3. Rellenar:
   - Código
   - Nombre
   - Precio de coste
   - Precio de venta (sin IVA)
   - % IVA
   - Código de barras (opcional)
4. Pulsar **"Guardar"**

### 4. Crear Albarán

1. Ir a **Ventas → Albaranes**
2. Pulsar **"Nuevo Albarán"**
3. Seleccionar cliente
4. Añadir líneas:
   - Buscar artículo
   - Indicar cantidad
   - Precio se calcula automáticamente
5. Pulsar **"Guardar"**

### 5. Emitir Factura

#### Método 1: Desde Albarán
1. Seleccionar albarán en la lista
2. Pulsar **"Convertir a Factura"**
3. Revisar datos
4. Pulsar **"Emitir"**

#### Método 2: Factura Nueva
1. Ir a **Ventas → Facturas**
2. Pulsar **"Nueva Factura"**
3. Rellenar:
   - Cliente
   - Fecha
   - Líneas de detalle
4. Estado inicial: **BORRADOR**

### 6. Workflow de Factura

```
BORRADOR → PENDIENTE_REVISION → APROBADA → ENVIADA_AEAT → REGISTRADA_AEAT
```

**Pasos:**

1. **BORRADOR**: Crear factura
   - Se puede editar y eliminar
   
2. **Solicitar Revisión**:
   - Pulsar **"Enviar a Revisión"**
   - Estado cambia a PENDIENTE_REVISION
   - Ya no se puede editar
   
3. **Aprobar** (solo usuarios con permisos):
   - Seleccionar factura PENDIENTE_REVISION
   - Pulsar **"Aprobar"**
   - Estado cambia a APROBADA
   
4. **Enviar a AEAT**:
   - Seleccionar factura APROBADA
   - Pulsar **"Enviar a AEAT"**
   - Se genera evidencia Verifactu
   - Estado cambia a ENVIADA_AEAT
   
5. **Confirmación AEAT** (automático):
   - Estado final: REGISTRADA_AEAT

### 7. Imprimir Factura

1. Seleccionar factura
2. Pulsar **"Imprimir"**
3. Se genera PDF con:
   - Datos fiscales completos
   - Líneas de detalle
   - Totales e IVA
   - Código QR de verificación
4. Se abre automáticamente

### 8. Anular Factura

⚠️ **IMPORTANTE:** Solo se pueden eliminar facturas en estado BORRADOR.

Facturas en otros estados:
- No se pueden eliminar (normativa fiscal)
- Se debe emitir factura rectificativa

### 9. Devolver a Borrador

Si una factura en PENDIENTE_REVISION tiene errores:
1. Seleccionar factura
2. Pulsar **"Devolver a Borrador"**
3. Realizar correcciones
4. Volver a enviar a revisión

---

## 🔧 MANUAL TÉCNICO

### Estructura de Directorios

```
ERP/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── alicanteweb/erp/
│   │   │       ├── config/          # Configuración Spring
│   │   │       ├── controller/      # Controladores JavaFX
│   │   │       ├── entities/        # Entidades JPA
│   │   │       ├── exception/       # Excepciones personalizadas
│   │   │       ├── repository/      # Repositorios JPA
│   │   │       ├── service/         # Lógica de negocio
│   │   │       ├── util/            # Utilidades
│   │   │       └── ErpLauncher.java # Punto de entrada
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── certs/               # Certificados digitales
│   │       ├── css/                 # Estilos CSS
│   │       ├── images/              # Imágenes
│   │       └── ui/                  # Archivos FXML
│   └── test/                        # Tests
├── basesdedatos/                    # Scripts SQL
│   └── definitivo/
│       └── tahonaerp.sql           # Estructura base de datos
├── documentacion/                   # Documentación adicional
├── scripts/                         # Scripts de utilidad
└── pom.xml                         # Configuración Maven
```

### Entidades Principales

#### Usuario
```java
@Entity
@Table(name = "users")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password; // BCrypt
    
    @Enumerated(EnumType.STRING)
    private Rol role;
    
    private Boolean enabled;
    private Boolean bloqueado;
    private Integer intentosFallidos;
    
    // ... getters y setters
}
```

#### Cliente
```java
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String codigo;
    
    private String nombre;
    private String cif;
    private String direccion;
    private String poblacion;
    private String provincia;
    private String codigoPostal;
    private Boolean activo;
    
    // ... getters y setters
}
```

#### Factura
```java
@Entity
@Table(name = "facturas")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String numero;
    
    @ManyToOne
    private Cliente cliente;
    
    private LocalDate fecha;
    
    @Enumerated(EnumType.STRING)
    private EstadoFactura estado;
    
    private BigDecimal baseImponible;
    private BigDecimal iva;
    private BigDecimal total;
    
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL)
    private List<FacturaLinea> lineas;
    
    // ... getters y setters
}
```

### Servicios Clave

#### FacturaService
Gestiona todo el ciclo de vida de las facturas:
- Crear, editar, eliminar (solo borradores)
- Cambios de estado
- Cálculo de totales
- Integración con Verifactu

#### VerifactuService
Gestiona la integración con AEAT:
- Genera evidencias digitales
- Firma facturas
- Mantiene cadena de custodia
- Simula envío a AEAT

#### AuditoriaService
Registra todas las operaciones:
- Login/logout
- CRUD de entidades
- Cambios de estado
- Accesos a datos

### Base de Datos

#### Tablas Principales

**users** - Usuarios del sistema
- id, username, password, role, enabled, bloqueado

**clientes** - Clientes
- id, codigo, nombre, cif, direccion, poblacion, provincia, codigo_postal, activo

**articulos** - Artículos
- id, codigo, nombre, precio_coste, precio_venta, iva, codigo_barras

**proveedores** - Proveedores
- id, codigo, nombre, cif, direccion

**facturas** - Facturas
- id, numero, fecha, cliente_id, estado, base_imponible, iva, total

**factura_linea** - Líneas de factura
- id, factura_id, articulo_id, descripcion, cantidad, precio, iva, total

**albaran_venta** - Albaranes de venta
- id, numero, fecha, cliente_id

**albaran_venta_linea** - Líneas de albarán
- id, albaran_id, articulo_id, cantidad, precio

**verifactu_evidence** - Evidencias Verifactu
- id, factura_id, hash, firma, fecha_firma

**auditoria_acciones** - Auditoría
- id, usuario_id, fecha, tipo_accion, entidad_tipo, entidad_id, resultado

**rgpd_consentimiento** - Consentimientos RGPD
- id, cliente_id, tipo, fecha, aceptado

### Configuración de Spring

#### SecurityConfig
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
```

#### JpaConfig
```java
@Configuration
@EnableJpaRepositories(basePackages = "alicanteweb.erp.repository")
public class JpaConfig {
    // Configuración JPA
}
```

### Testing

Ubicación: `src/test/java/`

Para ejecutar tests:
```bash
mvn test
```

---

## ⚖️ CUMPLIMIENTO LEGAL

### Normativa Española Aplicable

#### 1. Ley de Facturación (RD 1619/2012)
✅ **Cumplimiento:**
- Numeración correlativa por año
- Datos obligatorios en factura
- Conservación de facturas (mínimo 4 años)
- Series de facturación

#### 2. Ley Antifraude (Ley 11/2021)
✅ **Cumplimiento:**
- Sistema Verifactu implementado
- Cadena de custodia de facturas
- Firma digital
- Trazabilidad completa

#### 3. RGPD (Reglamento UE 2016/679)
✅ **Cumplimiento:**
- Registro de consentimientos
- Derecho de acceso, rectificación, supresión
- Portabilidad de datos
- Auditoría de accesos
- Cifrado de datos sensibles

#### 4. LOPD (Ley Orgánica 3/2018)
✅ **Cumplimiento:**
- Medidas de seguridad
- Registro de actividades de tratamiento
- Auditoría

⚠️ **Pendiente:**
- Modelo 303 (IVA)
- Modelo 347
- SII (para empresas obligadas)
- Libros registro

### Conservación de Documentos

**Facturas:** Mínimo 4 años (6 años recomendado)
**Albaranes:** Mínimo 4 años
**Auditoría:** Mínimo 6 años

---

## 🔒 SEGURIDAD

### Medidas Implementadas

1. **Autenticación**
   - Contraseñas encriptadas con BCrypt (cost=10)
   - Control de intentos fallidos
   - Bloqueo automático

2. **Autorización**
   - Roles y permisos
   - Control de acceso por funcionalidad

3. **Auditoría**
   - Registro completo de acciones
   - Trazabilidad

4. **Datos**
   - Cifrado en reposo (opcional)
   - Comunicación segura con BD
   - Backup automático

5. **RGPD**
   - Consentimientos
   - Derecho al olvido
   - Portabilidad

### Recomendaciones

1. **Contraseñas**
   - Mínimo 8 caracteres
   - Mayúsculas, minúsculas, números
   - Cambiar periódicamente

2. **Base de Datos**
   - Usuario específico (no root)
   - Contraseña fuerte
   - Acceso solo desde localhost
   - Firewall configurado

3. **Backups**
   - Diarios automáticos
   - Almacenamiento externo
   - Encriptación

4. **Red**
   - Firewall activo
   - Antivirus actualizado
   - Red segura

---

## 💾 BACKUP Y RECUPERACIÓN

### Backup de Base de Datos

#### Manual
```bash
mysqldump -u tahona_user -p tahona > backup_tahona_$(date +%Y%m%d).sql
```

#### Automático (Script Windows)
```batch
@echo off
set FECHA=%date:~6,4%%date:~3,2%%date:~0,2%
mysqldump -u tahona_user -pCONTRASEÑA tahona > D:\Backups\tahona_%FECHA%.sql
```

#### Automático (Script Linux)
```bash
#!/bin/bash
DATE=$(date +%Y%m%d)
mysqldump -u tahona_user -pCONTRASEÑA tahona > /backups/tahona_$DATE.sql
```

### Restauración

```bash
mysql -u tahona_user -p tahona < backup_tahona_YYYYMMDD.sql
```

### Programar Backup Automático

#### Windows (Programador de Tareas)
1. Crear script `.bat`
2. Programador de tareas → Nueva tarea
3. Ejecutar diariamente a las 2:00 AM

#### Linux (Cron)
```bash
crontab -e
# Añadir:
0 2 * * * /ruta/backup_script.sh
```

---

## 🐛 TROUBLESHOOTING

### Problema: No arranca la aplicación

**Error:** `Failed to execute goal javafx:run`

**Solución:**
1. Verificar Java 17+: `java -version`
2. Verificar Maven: `mvn -version`
3. Limpiar proyecto: `mvn clean`
4. Compilar: `mvn compile`
5. Ejecutar: `mvn javafx:run`

### Problema: Error de conexión a base de datos

**Error:** `Cannot create PoolableConnectionFactory`

**Solución:**
1. Verificar que MySQL está activo: `sudo systemctl status mysql`
2. Verificar credenciales en `application.properties`
3. Verificar que la base de datos existe:
```sql
mysql -u root -p
SHOW DATABASES;
```

### Problema: No funciona el login

**Error:** Credenciales inválidas

**Solución 1: Resetear contraseña admin**
```sql
mysql -u root -p
USE tahona;
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', bloqueado = 0, intentos_fallidos = 0 WHERE username = 'admin';
```
Contraseña: `admin`

**Solución 2: Desbloquear usuario**
```sql
UPDATE users SET bloqueado = 0, intentos_fallidos = 0 WHERE username = 'admin';
```

### Problema: Error de codificación UTF-8

**Solución:**
1. Verificar charset base de datos:
```sql
SHOW VARIABLES LIKE 'character_set%';
```

2. Si no es utf8mb4:
```sql
ALTER DATABASE tahona CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Problema: Facturas marcadas como rollback-only

**Error:** `Transaction marked as rollback-only`

**Solución:**
1. Revisar logs para identificar error original
2. Reiniciar aplicación
3. Verificar integridad de datos
4. Si persiste, contactar soporte técnico

### Problema: No se generan PDFs

**Solución:**
1. Verificar que iText está en dependencias
2. Verificar permisos de escritura en directorio temporal
3. Revisar logs

---

## 🗺️ ROADMAP

### Versión 0.1.0 (Próxima - Q1 2026)

**Prioridad Alta:**
- [ ] Módulo de Contabilidad básico
- [ ] Módulo de Tesorería (caja y bancos)
- [ ] Módulo de Compras
- [ ] Gestión avanzada de Almacén (lotes y caducidades)
- [ ] Modelo 303 (IVA)

**Prioridad Media:**
- [ ] TPV (Terminal Punto de Venta)
- [ ] Informes y Dashboard
- [ ] Módulo de Pedidos de clientes
- [ ] Mejoras en Verifactu (envío real a AEAT)

**Prioridad Baja:**
- [ ] Módulo CRM
- [ ] App móvil para consultas

### Versión 1.0.0 (Q2 2026)

**Objetivos:**
- ERP completo y funcional
- Todos los módulos fiscales
- Certificación AEAT
- Documentación completa
- Tests automatizados (>80% cobertura)

### Versión 2.0.0 (Q4 2026)

**Objetivos:**
- Versión Cloud (SaaS)
- API REST completa
- App móvil nativa
- Multi-idioma
- Integración con plataformas externas

---

## 📞 SOPORTE Y CONTACTO

**Desarrollador:** AlicanteWeb  
**Email:** soporte@alicanteweb.com (ficticio)  
**GitHub:** https://github.com/alicanteweb/erp (ficticio)  

**Horario de soporte:** Lunes a Viernes, 9:00 - 18:00 (CET)

---

## 📝 LICENCIA

Copyright © 2025 AlicanteWeb. Todos los derechos reservados.

Este software es propietario y confidencial. No está permitida su distribución, copia o modificación sin autorización expresa del titular.

---

## 📚 REFERENCIAS

- [Ley 11/2021 Antifraude](https://www.boe.es/buscar/act.php?id=BOE-A-2021-11473)
- [Real Decreto 1619/2012 Facturación](https://www.boe.es/buscar/act.php?id=BOE-A-2012-14696)
- [RGPD](https://eur-lex.europa.eu/eli/reg/2016/679/oj)
- [Verifactu AEAT](https://sede.agenciatributaria.gob.es/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JavaFX Documentation](https://openjfx.io/)

---

**Última actualización:** 29 de Diciembre de 2025  
**Versión documento:** 1.0

