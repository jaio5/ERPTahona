# ERP Panadería Tahona – Especificación y Plan de Proyecto

Este documento define el alcance funcional y técnico, la arquitectura, tecnologías, estructura del repositorio y el plan de ejecución para construir un ERP de panadería de nivel profesional, cumpliendo normativa española (incluida Veri*factu*) y orientado a una aplicación de escritorio en JavaFX.

---

## 1. Objetivo y Alcance

- Construir un ERP integral para una panadería con módulos de Ventas, Compras, Almacén, Producción (órdenes de fabricación/escandallo), Tesorería, TPV (opcional), Contabilidad básica y Reportes.
- Cumplimiento fiscal/contable en España: IVA/REQ, numeración y custodia de facturas, Firma/huella y envío Veri*factu*, posibilidad de Facturae (B2G) y preparación para e-factura B2B (Ley Crea y Crece).
- Aplicación de escritorio moderna en JavaFX para Windows (primario), con empaquetado nativo (MSI/EXE) y actualizaciones.
- Reutilizar la base de datos aportada (MySQL) y evolucionarla con migraciones controladas (Flyway), preservando datos existentes.

---

## 2. Requisitos Clave (Funcionales y No Funcionales)

### 2.1 Funcionales
- Maestros: Artículos, Familias, Clientes, Proveedores, Tipos de IVA/REQ, Zonas/Almacenes.
- Ventas: Presupuestos, Pedidos, Albaranes, Facturas, abonos; tarifas y precios por artículo; gestión de REQ; direcciones de envío; condiciones de pago.
- Compras: Pedidos, Albaranes, Facturas proveedor; valoración de compras.
- Almacén/Stock: movimientos, regularizaciones, traspasos, inventarios, mínimos/máximos por almacén.
- Producción (panadería): escandallos/recetas (composición), órdenes de fabricación, consumos, mermas, costes estándar/medio.
- Tesorería: cobros/pagos, remesas (SEPA pain.008 opcional), arqueos de caja, conciliación sencilla.
- Contabilidad básica: plan de cuentas (PGC), asientos básicos origen de ventas/compras, balances e informes simples.
- Fiscalidad España: IVA (general/reducido/superreducido), Recargo de Equivalencia (REQ), libros de IVA, modelos base (exportación a CSV/Excel para presentación).
- Facturación electrónica:
  - Veri*factu*: generación de la huella encadenada, registro inalterable, envío telemático (según modalidad elegida), control de numeración y no alteración.
  - Facturae (B2G) v3.2.2 opcional y preparación e-factura B2B (Ley Crea y Crece).
- Informes: ventas por periodo/artículo/cliente, márgenes, rotaciones, trazabilidad básica.
- Seguridad y auditoría: perfiles y permisos, bitácora de acciones y cambios relevantes.

### 2.2 No Funcionales
- Desktop first (JavaFX), rendimiento fluido en equipos modestos, base de datos MySQL 8.
- Calidad: pruebas unitarias/integración, estándares de código, CI.
- Mantenibilidad: arquitectura por capas y migraciones con Flyway.
- Entregables instalables (jpackage), soporte para auto-update.

---

## 3. Arquitectura

### 3.1 Vista General
- Aplicación de **escritorio JavaFX** que accede a la base de datos MySQL mediante **JPA/Hibernate**.
- Servicios de dominio (lógica) y capa de repositorios (JPA). UI desacoplada (MVVM/MVP) con inyección de dependencias.
- Opción futura: despliegue cliente-servidor con un microservicio REST (Spring Boot) manteniendo el cliente JavaFX (migración suave).

### 3.2 Capas
- Presentación: JavaFX (FXML), MVVM/MVP (Afterburner.FX o FXWeaver si se usa Spring). Controles: ControlsFX, Ikonli para iconografía.
- Aplicación/Dominio: Servicios de caso de uso, validación, reglas fiscales (IVA/REQ), motores de numeración, generación de huella Veri*factu*.
- Infraestructura: JPA/Hibernate 6, HikariCP, Flyway, MapStruct (mapeos DTO), JasperReports (informes), Apache POI (Excel), PDFBox/iText.

### 3.3 Persistencia
- MySQL 8.0 con collation/charset `utf8mb4_unicode_ci`.
- Migraciones con **Flyway**: baseline sobre el estado actual (script `tahona_mysql_mejorado.sql`) y evolución con `V2__...sql`, `V3__...sql`.
- Esquema definitivo: `tahona`.

### 3.4 Cumplimiento Veri*factu*
- Numeración inalterable y correlativa por serie.
- Generación de huella encadenada (hash de factura + hash previo + metadatos) y sellado con certificado.
- Registro inmutable (tabla de evidencias) y bitácora de eventos.
- Envío telemático según modalidad (inmediato/diferido) a la AEAT; manejo de colas y reintentos offline.
- Certificados (FNMT u otros) en almacén seguro (Windows Key Store o PKCS#12) y firma/cifrado TLS.

> Nota: Documentar la modalidad elegida según RD 1007/2023 e incorporar pruebas de integración en entorno de pruebas AEAT cuando publiquen sandbox/entorno.

---

## 4. Stack Tecnológico

- Java 21 LTS
- JavaFX 21 (FXML, ControlsFX, Ikonli)
- JPA/Hibernate 6 + HikariCP
- Flyway (migraciones)
- MapStruct (DTO ↔ entidad)
- SLF4J + Logback (logging), auditoría a tabla
- JasperReports para informes/PDF
- Testcontainers (MySQL) + JUnit 5 + Mockito + TestFX (UI)
- Maven (build), Spotless + Checkstyle/PMD (estilo), SonarLint (local)
- jlink + jpackage (runtime e instaladores Windows)

Opcionales:
- Spring Boot 3 para DI y futura API REST (si se elige), FXWeaver para integrar JavaFX+Spring.
- Liquibase (alternativa a Flyway), MapDB/SQLite (caché local offline), S3/MinIO (backups).

---

## 5. Estructura del Repositorio

```
ERP/
├─ docs/
│  ├─ PROYECTO_ERP_PANADERIA.md   # Este documento
│  ├─ INSTRUCCIONES_SQL.md        # Cómo cargar la BD
│  └─ CORRECCIONES_APLICADAS.md   # Cambios y fixes SQL
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  ├─ erp/
│  │  │  │  ├─ app/                 # bootstrap JavaFX
│  │  │  │  ├─ config/              # DI, propiedades, certificados
│  │  │  │  ├─ domain/              # entidades de dominio/servicios
│  │  │  │  ├─ infrastructure/      # repositorios, JPA, Flyway
│  │  │  │  └─ ui/                   # vistas/controladores (FXML)
│  │  │  └─ entities/               # entidades JPA (compatibles con BD)
│  │  ├─ resources/
│  │  │  ├─ application.properties  # conexión MySQL, flyway
│  │  │  ├─ db/migration/           # scripts Flyway V1__baseline.sql...
│  │  │  └─ fxml/                   # vistas FXML
│  └─ test/
│     ├─ java/
│     └─ resources/
├─ tahona_mysql_mejorado.sql       # script base (import inicial)
├─ verificar_datos_tahona.sql      # verificación post-carga
├─ pom.xml
└─ README.md                       # resumen y acceso a docs
```

> Nota: Las entidades JPA actuales (p.ej. `entities.Agente`) deberán revisarse para tener **@Id** y concordancia exacta con tabla/columnas.

---

## 6. Modelo de Datos (mapa a tablas existentes)

- Artículos (`Articulos`): precios PVP1..PVP5, familia, IVA/REQ, flags servicio/uso interno, proveedor por defecto, mínimos/máximos.
- Familias (`Familias`), Tipos de IVA (`TiposdeIVA`), Provincias (`Provincias`).
- Clientes (`Clientes`) y direcciones de envío (`DireccionesEnvio`).
- Documentos: Pedidos/Albaranes/Facturas de venta/compra + desgloses.
- Stock: `StockMovimiento`, `StockRegularizacione`.
- Contabilidad: `Cuentas`, `Subcuenta`, `Asiento`, `Apunte`, `BalancesConImporte`, `balancescuentas`.
- Producción: `OrdenesProduccion`, `OrdenesProduccionDesglose`, `Composicion` (escandallos).

Evoluciones con Flyway: normalizar claves primarias si faltan, foreign keys, índices, vistas de apoyo.

---

## 7. Cumplimiento Legal y Fiscal (España)

- IVA/REQ: correcto cálculo y desglose, libros de IVA de soporte y exportación de modelos.
- Veri*factu* (RD 1007/2023):
  - Serie y numeración inalterable.
  - Huella encadenada por factura (hash + hash anterior + sellado con certificado).
  - Registro inmutable y logging de eventos; bloqueo de modificaciones post-emitido (solo rectificativas).
  - Envío telemático con colas y reintentos, evidencia de entrega (ID/acuse).
- Facturae (opcional): generación XML 3.2.2 + firma XAdES, FACe para B2G.
- E-factura B2B (Ley Crea y Crece): preparación para conectores cuando se publique el nodo/REDI definitivo.
- Protección de datos (RGPD/LOPDGDD): perfiles, minimización, cifrado en tránsito, retención/depuración.

---

## 8. Diseño de UI (JavaFX)

- Patrón MVVM/MVP, pantallas: Dashboard, Maestros, Ventas/Compras (ABM + documentos), Almacén/Producción, Tesorería, Informes, Configuración.
- Componentes: tablas con filtrado/sorting, formularios validados, asistentes (wizard) de facturación y remesas.
- Accesibilidad y atajos de teclado, temas (light/dark), iconografía (Ikonli), notificaciones (ControlsFX).

---

## 9. Plan de Migraciones (Flyway)

1. Baseline (V1__baseline.sql): reflejar el estado actual de la BD (basado en `tahona_mysql_mejorado.sql`).
2. V2…: añadir claves primarias si faltan (p. ej., en `Agentes`, usar `CodigoAgente` como PK), FKs y constraints.
3. V3…: índices de rendimiento (búsquedas por código/fecha/cliente).
4. V4…: tablas de auditoría y evidencias Veri*factu*.
5. V5…: vistas/materializadas para informes.

> Cada cambio estructural o de datos maestros: nueva migración Flyway versionada.

---

## 10. Seguridad, Auditoría y Backups

- Usuarios/roles del sistema; trazabilidad con tabla de auditoría (quién, cuándo, qué campos cambian).
- Logs firmados/verificados para facturación.
- Backups: dump MySQL diario + retención; exportación CSV/PDF periódica de libros.

---

## 11. Despliegue, Empaquetado y Actualizaciones

- JDK 21 + JavaFX runtime con **jlink** para runtime reducido.
- Instalador Windows con **jpackage** (MSI/EXE), acceso directo y registro en Inicio.
- Config: `application.properties` en directorio de datos del usuario o cifrado de credenciales.
- Auto-update: comprobación de versión y descarga (opcional) desde servidor de actualizaciones.

---

## 12. Calidad y Pruebas

- Pruebas unitarias: JUnit 5 + Mockito.
- Integración: Testcontainers (MySQL) con Flyway.
- UI: TestFX para flujos críticos.
- Analizadores: Spotless, Checkstyle/PMD, SonarLint.
- CI (GitHub Actions/GitLab CI): build, test, empaquetado artefactos.

---

## 13. Guía de Desarrollo

### 13.1 Requisitos locales
- MySQL 8, Java 21, Maven 3.9+, Node (opcional para herramientas).

### 13.2 Configuración
`src/main/resources/application.properties` (ejemplo):

```
spring.datasource.url=jdbc:mysql://localhost:3306/tahona?useUnicode=true&characterEncoding=utf8&serverTimezone=Europe/Madrid
spring.datasource.username=usuario
spring.datasource.password=segura
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

> Si no se usa Spring, cargar estas propiedades vía librería de configuración (Typesafe Config o similar) e inicializar Hikari/Hibernate manualmente.

### 13.3 Convenciones de Código
- Paquetes `erp.domain`, `erp.infrastructure`, `erp.ui`.
- Entidades JPA con `@Id` obligatorio (p.ej., `Agente.codigoAgente` como `@Id`). Columnas alineadas a la BD.
- Servicios sin lógica de presentación; control de transacciones en capa de aplicación.

### 13.4 Scripts SQL
- `tahona_mysql_mejorado.sql`: carga inicial.
- `verificar_datos_tahona.sql`: verificación post-carga.
- `src/main/resources/db/migration`: migraciones Flyway.

---

## 14. Roadmap (Hitos)

1) Fase 0 – Acondicionamiento (1-2 semanas)
- Carga BD, baseline Flyway, ajuste entidades JPA (añadir @Id, validaciones), arranque JavaFX básico.

2) Fase 1 – Maestros e Inventario (3-4 semanas)
- CRUD Artículos/Clientes/Proveedores, Familias/IVA/Almacenes, stock y regularizaciones, informes básicos.

3) Fase 2 – Ventas/Compras (4-6 semanas)
- Ciclo completo de documentos, precios/tarifas, integración contable básica.

4) Fase 3 – Producción panadería (3-4 semanas)
- Escandallos, órdenes de fabricación, consumos y costes.

5) Fase 4 – Veri*factu* y e-factura (4-6 semanas)
- Huella encadenada, evidencias, envío telemático, Facturae opcional.

6) Fase 5 – TPV y Tesorería (3-4 semanas)
- Punto de venta simplificado, arqueos, remesas.

7) Fase 6 – Calidad, empaquetado y actualización (2-3 semanas)
- Pruebas, jpackage, actualizador, documentación final.

---

## 15. Riesgos y Mitigaciones
- Falta de PK/constraints en tablas heredadas → normalizar con migraciones Flyway graduales.
- Cambios normativos (Veri*factu*, e-factura B2B) → diseñar módulos de integración desacoplados.
- Rendimiento en equipos modestos → jlink, índices BD, carga diferida, paginado en tablas.

---

## 16. Anexos

### 16.1 Ajuste de la entidad `Agente`

La tabla `Agentes` no declaraba PK explícita en el SQL original. Recomendación: usar `CodigoAgente` como clave primaria.

```java
@Entity
@Table(name = "Agentes", schema = "tahona")
public class Agente {
    @Id
    @Column(name = "CodigoAgente", length = 500)
    private String codigoAgente;
    // ... resto de columnas
}
```

> El esquema definitivo es `tahona`.

### 16.2 Certificados y Veri*factu*
- Certificado PFX/P12 cargado con password (Windows CAPI o fichero seguro).
- Almacén de claves y rotación.
- Pruebas de firma/huella en preproducción.

---

## 17. Conclusión

Este plan permite construir un ERP profesional y cumplidor para panadería en España, reutilizando la base de datos existente, con arquitectura limpia, JavaFX como UI de escritorio, y un camino claro hacia Veri*factu* y e-factura. A partir de este documento, se pueden abrir issues/tareas por hito y comenzar la ejecución iterativa.
