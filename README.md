# ERP Panadería Tahona

ERP completo para panadería desarrollado con JavaFX y Spring Boot, con cumplimiento normativo español (IVA/REQ, Veri*factu*, preparación e-factura B2B).

---

## 📋 Documentación

- **[Especificación del Proyecto](docs/PROYECTO_ERP_PANADERIA.md)** - Arquitectura, stack, roadmap y plan completo
- **[Instrucciones SQL](docs/INSTRUCCIONES_SQL.md)** - Cómo cargar la base de datos MySQL
- **[Correcciones Aplicadas](CORRECCIONES_APLICADAS.md)** - Cambios en el script SQL

---

## 🚀 Inicio Rápido

### Requisitos Previos

- Java 17 o superior
- MySQL 8.0
- Maven 3.9+ (incluido con Maven Wrapper)

### 1. Configurar la Base de Datos

```bash
# En MySQL Workbench o cliente MySQL:
# 1. File → Open SQL Script → tahona_mysql_mejorado.sql
# 2. Execute (⚡)
# 3. Verificar con: SELECT COUNT(*) FROM tahona.Articulos;
```

O desde línea de comandos:
```bash
mysql -u root -p < tahona_mysql_mejorado.sql
```

### 2. Configurar Credenciales

Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?useUnicode=true&characterEncoding=utf8&serverTimezone=Europe/Madrid
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD_AQUI
```

### 3. Ejecutar el Backend

```bash
# Windows (con Maven Wrapper incluido):
.\mvnw.cmd spring-boot:run

# Linux/Mac:
./mvnw spring-boot:run
```

El servidor arrancará en **http://localhost:8080**

---

## 🔌 API REST Disponible

### Endpoints

#### Health Check
```
GET /api/health
```
Respuesta:
```json
{
  "status": "UP",
  "service": "ERP"
}
```

#### Artículos

```
GET /api/articulos?limit=20
```
Lista artículos (paginado, máximo 100 por petición)

```
GET /api/articulos/{codigo}
```
Obtener artículo por código

#### Clientes

```
GET /api/clientes?limit=20
```
Lista clientes (paginado, máximo 100 por petición)

```
GET /api/clientes/{codigo}
```
Obtener cliente por código

---

## 🏗️ Arquitectura Backend

```
src/main/java/
├─ alicanteweb.erp/
│  ├─ ErpApplication.java         # Punto de entrada Spring Boot
│  ├─ api/                        # Controladores REST
│  │  ├─ HealthController.java
│  │  ├─ ArticuloController.java
│  │  └─ ClienteController.java
│  ├─ service/                    # Servicios de negocio
│  │  ├─ ArticuloService.java
│  │  └─ ClienteService.java
│  └─ repository/                 # Repositorios JPA
│     ├─ ArticuloRepository.java
│     └─ ClienteRepository.java
└─ entities/                      # Entidades JPA (schema: tahona)
   ├─ Articulo.java
   ├─ Cliente.java
   ├─ Familia.java
   └─ ... (80+ entidades)
```

---

## 🛠️ Stack Tecnológico

- **Backend**: Spring Boot 3.5.7, JPA/Hibernate 6
- **Base de Datos**: MySQL 8.0 (schema `tahona`)
- **Java**: 17 LTS
- **Build**: Maven 3.9+
- **Próximamente**: JavaFX 21 (UI de escritorio)

---

## 📊 Estado del Proyecto

### ✅ Completado

- [x] Base de datos MySQL con schema `tahona` (80+ tablas, ~2,667 registros)
- [x] Script SQL mejorado con ENGINE=InnoDB, UTF-8, tipos optimizados
- [x] Entidades JPA mapeadas (Articulo, Cliente y 80+ más)
- [x] Repositorios JPA con Spring Data
- [x] Servicios REST para Artículos y Clientes
- [x] Health check endpoint

### 🚧 En Desarrollo

- [ ] Mapeo JPA completo de entidades relacionales
- [ ] DTOs y validación
- [ ] Servicios REST para Familias, Tipos de IVA, Provincias
- [ ] Servicios REST para Ventas/Compras
- [ ] Módulo JavaFX (UI de escritorio)

### 📅 Próximos Hitos

Ver [PROYECTO_ERP_PANADERIA.md](docs/PROYECTO_ERP_PANADERIA.md) para roadmap completo.

---

## 🧪 Pruebas

```bash
# Ejecutar tests
.\mvnw.cmd test

# Ejecutar con cobertura
.\mvnw.cmd verify
```

---

## 📦 Compilación

```bash
# Compilar sin tests
.\mvnw.cmd clean package -DskipTests

# El JAR se genera en:
target/ERP-0.0.1-SNAPSHOT.jar

# Ejecutar el JAR
java -jar target/ERP-0.0.1-SNAPSHOT.jar
```

---

## 🔍 Verificación de Datos

Después de cargar la BD, ejecutar:

```bash
# En MySQL Workbench:
# File → Open SQL Script → verificar_datos_tahona.sql
# Execute
```

Debe mostrar:
- Artículos: ~150+
- Clientes: ~50+
- Cuentas: ~450+
- Familias: 10
- Provincias: 50

---

## 💡 Tips de Desarrollo

### Logs de Hibernate

En `application.properties`:
```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### Hot Reload (Spring Boot DevTools)

Añadir al `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 📞 Contacto y Contribución

Este proyecto está en desarrollo activo. Para contribuir:
1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Añadir nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

---

## 📄 Licencia

[Especificar licencia aquí]

---

**Última actualización**: 2025-11-16  
**Versión**: 0.0.1-SNAPSHOT

