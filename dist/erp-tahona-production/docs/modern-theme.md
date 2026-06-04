# Plan de trabajo: Reparación y mejora del proyecto ERPTahona

Este documento contiene el plan detallado y priorizado para que la aplicación arranque en modo dev, se corrijan errores de compilación y FXML, se limpie el `pom.xml`, se arregle Verifactu y se apliquen mejoras de código limpio.

---

## Resumen rápido

Objetivos principales (prioridad alta):
- Hacer que la aplicación arranque con `javafx:run` en perfil `dev` usando MySQL local.
- Corregir errores de compilación críticos (ej. `UsuarioController`) y unresolved `fx:id` en FXML.
- Asegurar que los controladores y FXML estén correctamente enlazados.

Objetivos secundarios (prioridad media):
- Limpiar `pom.xml` y eliminar dependencias no utilizadas.
- Reparar la integración `verifactu` (certificado y envío a AEAT en sandbox).
- Mejorar CSS/UX para evitar desbordamientos en formularios.

Objetivos de baja prioridad:
- Limpieza general de código (sin usar `@SuppressWarnings`), agregar tests mínimos y preparar perfiles `prod`/`dev` con placeholders.

---

## Checklist inicial (acciones concretas que voy a ejecutar)

- [ ] Crear rama de trabajo: `fix/dev-setup`.
- [ ] Verificar/ajustar `src/main/resources/application-dev.properties` con MySQL local.
- [ ] Ejecutar `mvn -DskipTests clean compile` y recopilar errores.
- [ ] Corregir errores de compilación prioritarios (controladores y firmas de métodos).
- [ ] Revisar FXML críticos y mapear `fx:id` ↔ `@FXML` fields/methods.
- [ ] Ejecutar `mvn javafx:run -Dspring.profiles.active=dev` y validar arranque.
- [ ] Limpiar `pom.xml` (dependency:analyze) y fijar versiones de plugins.
- [ ] Diagnosticar y reparar `verifactu` (keystore y endpoint sandbox).
- [ ] Ajustes CSS/Layouts para evitar overflow (añadir `ScrollPane` y constraints donde haga falta).
- [ ] Revisión final, añadir tests mínimos y documentar variables sensibles en README.

---

## Plan paso a paso detallado

### 1) Arranque en modo dev con MySQL local (PRIORIDAD: Alta)

- Archivos a revisar:
  - `src/main/resources/application-dev.properties`
  - `src/main/resources/application.properties`
  - `pom.xml` (presencia de `mysql-connector-java` y `javafx-maven-plugin`)
  - Código de DataSource si existe (buscar `HikariDataSource`, `spring.datasource`)

- Riesgos:
  - Ejecutar accidentalmente contra la base de datos de producción.
  - Falta de driver JDBC o configuración incompatible.

- Estimación: 1.5–3 horas.

- Cambios concretos a aplicar:
  1. Añadir/comprobar dependencia MySQL en `pom.xml` (ej: `mysql:mysql-connector-java:8.0.x`).
  2. Editar `application-dev.properties` con credenciales locales (ejemplo):
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/erp_dev?useSSL=false&serverTimezone=UTC
     spring.datasource.username=root
     spring.datasource.password=Iirne322*
     spring.jpa.hibernate.ddl-auto=update
     ```
  3. Ejecutar y probar:
     ```powershell
     mvn -DskipTests clean package
     mvn javafx:run -Dspring.profiles.active=dev
     ```
  4. Si `javafx-maven-plugin` no está configurado, usar `spring-boot:run` con un launcher JavaFX o añadir la configuración del plugin en `pom.xml`.

- Comprobaciones:
  - `mvn dependency:tree | findstr /i mysql`
  - Log de arranque y HikariPool iniciando conexiones.

---

### 2) Corregir errores de compilación críticos (PRIORIDAD: Alta)

- Archivos a revisar:
  - `src/main/java/alicanteweb/erp/controller/UsuarioController.java`
  - `src/main/java/alicanteweb/erp/controller/formcontroller/*.java`
  - Todos los controladores con errores reportados en la compilación.

- Riesgos:
  - Romper firmas usadas desde FXML o desde otras clases.

- Estimación: 4–12 horas (depende del número de errores).

- Cambios concretos a aplicar:
  1. Ejecutar: `mvn -DskipTests clean compile` y copiar la salida de errores.
  2. Para cada error típico:
     - `cannot find symbol method initCallback(this::cargarDatos)` ➜ revisar clase `UsuarioFormController` y añadir/ajustar método `initCallback(Consumer<T> callback)` o cambiar la forma de invocarlo en `UsuarioController`.
     - `cannot find symbol method initStage(Stage)` ➜ añadir `public void initStage(Stage stage)` en `BaseFormController` o usar la interfaz correcta.
     - Revisar coincidencia `fx:id`/`@FXML`: si FXML referencia `onAction="#metodo"` asegurar `@FXML public void metodo(ActionEvent e)` exista.
  3. Añadir `@FXML` a campos en controladores que correspondan a `fx:id`.
  4. Recompilar iterativamente hasta que `mvn compile` sea exitoso.

- Tests manuales:
  - Arrancar la aplicación y navegar a pantallas afectadas.

---

### 3) Limpiar `pom.xml` y dependencias no usadas (PRIORIDAD: Media)

- Archivos a revisar: `pom.xml`.
- Riesgos: eliminar dependencias transitivas necesarias.
- Estimación: 2–6 horas.
- Pasos:
  1. Ejecutar `mvn dependency:analyze` y `mvn dependency:tree`.
  2. Mover dependencias usadas solo en tests a `scope=test`.
  3. Consolidar versiones problemáticas en `dependencyManagement`.
  4. Fijar versiones de plugins y comprobar `Unresolved plugin` (ej. `org.owasp:dependency-check-maven` si aparece).
  5. Recompilar y validar ausencia de warnings relevantes.

---

### 4) Revisar y enlazar FXML con controladores (PRIORIDAD: Alta)

- Archivos a revisar:
  - `src/main/resources/ui/**/*.fxml`
  - `src/main/java/**/*Controller.java`

- Riesgos: renombrado incorrecto que rompa rutas de `FXMLLoader`.
- Estimación: 6–16 horas.

- Pasos:
  1. Inventario: `Get-ChildItem -Recurse -Filter *.fxml` y listar `fx:controller` y `fx:id`.
  2. Para cada FXML:
     - Asegurar que `fx:controller` apunta a la clase correcta.
     - Añadir `@FXML private <Type> field;` por cada `fx:id` no resuelto.
     - Confirmar handlers `onAction="#..."` existen con la firma correcta.
  3. Priorizar pantallas: `facturas`, `albaranes`, `clientes`, `dashboard`.
  4. Repetir `mvn compile` y luego `mvn javafx:run` para probar navegación.

---

### 5) Reparar `verifactu` (certificado y envío AEAT) (PRIORIDAD: Media-Alta)

- Archivos a revisar:
  - `src/main/resources/certs/mi_certificado.p12` (existencia/validez)
  - Clases en `src/main/java/.../verifactu*` o `VerifactuService`.
  - `application.properties` / `application-dev.properties` (propiedades `verifactu.*`).

- Riesgos: pruebas contra AEAT en producción; manejo de secretos.
- Estimación: 4–12 horas (dependiendo del formato del certificado y del cliente AEAT).

- Pasos:
  1. Verificar `mi_certificado.p12` con `keytool -list -v -keystore path -storetype PKCS12 -storepass <pass>` (local).
  2. Ajustar propiedades:
     ```properties
     verifactu.cert.path=classpath:certs/mi_certificado.p12
     verifactu.cert.password=changeit
     verifactu.aeat.endpoint=https://prewww2.aeat.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu
     verifactu.aeat.enabled=false
     ```
  3. Manejar errores de parsing del keystore (ej. `Tag number over 30 is not supported`) revisando formato del P12 o regenerando.
  4. Implementar modo sandbox y logs de diagnóstico evitando exponer secretos.

---

### 6) Evitar desbordamientos en formularios y mejorar CSS/UX (PRIORIDAD: Media)

- Archivos a revisar:
  - `src/main/resources/styles/*`, `src/main/resources/css/*`
  - Formularios FXML que se desbordan (ej. `albaran_form.fxml`, `factura_form.fxml`).

- Riesgos: cambios visuales que afecten la experiencia.
- Estimación: 3–8 horas.

- Soluciones concretas:
  1. En contenedores largos, envolver con `ScrollPane`.
  2. Usar `VBox`/`GridPane` con `hgrow/vgrow` y `Priority.ALWAYS` para elementos redimensionables.
  3. Evitar tamaños absolutos en CSS; usar `-fx-max-height`, `-fx-min-height` y porcentajes cuando proceda.

---

### 7) Limpieza de código y tests mínimos (sin `@SuppressWarnings`) (PRIORIDAD: Media)

- Archivos a revisar: todo el repo en busca de `@SuppressWarnings` y code smells.
- Riesgos: refactor que rompa comportamiento si no se prueba.
- Estimación: 3–10 horas.

- Pasos:
  1. Buscar `@SuppressWarnings` y sustituir por correcciones reales (tipos genéricos, Optional, eliminar código muerto).
  2. Añadir tests unitarios básicos para servicios críticos (UsuarioService, FacturaService) usando JUnit 5.
  3. Incluir `maven-surefire-plugin` y ejecutar `mvn test`.

---

### 8) Preparar la app para producción (perfiles, secrets) (PRIORIDAD: Alta)

- Archivos a revisar:
  - `application.properties`, `application-dev.properties`, `application-prod.properties`.
  - `README_RUN.md` para instrucción de despliegue.

- Riesgos: exponer contraseñas en VCS.
- Estimación: 2–4 horas.

- Pasos:
  1. Configurar placeholders con variables de entorno:
     ```properties
     spring.datasource.username=${DB_USER:root}
     spring.datasource.password=${DB_PASS:changeme}
     ```
  2. Documentar la creación de secrets y el uso de perfiles en `README_RUN.md`.
  3. Configurar perfil Maven `prod` si se desea un artefacto específico.

---

## Comandos útiles para validar durante el proceso

- Compilar sin tests:

```powershell
mvn -DskipTests clean package
```

- Ejecutar en modo dev (JavaFX):

```powershell
mvn javafx:run -Dspring.profiles.active=dev
```

- Ejecutar Spring Boot (si hace falta usar `spring-boot:run`):

```powershell
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

- Analizar dependencias:

```powershell
mvn dependency:analyze
mvn dependency:tree
```

- Ejecutar tests:

```powershell
mvn test
```

---

## Recomendación de priorización inmediata (primeras tareas a ejecutar ahora)

1. Crear rama `fix/dev-setup` y commitear cambios de `application-dev.properties` para apuntar a MySQL local (credenciales que me diste: `root` / `Iirne322*`) — solo para desarrollo.
2. Ejecutar `mvn -DskipTests clean compile` y capturar errores. Ordenaré los errores por gravedad y los corregiré empezando por los que impiden la compilación.
3. Corregir `UsuarioController` y cualquier `BaseFormController`/`FormController` relacionado que presente métodos faltantes (`initCallback`, `initStage`, etc.).
4. Revisar FXML críticos y mapear `fx:id` no resueltos.
5. Ejecutar `mvn javafx:run -Dspring.profiles.active=dev` y validar arranque.

---

Si confirmas, procedo a aplicar los cambios iniciales en una rama (`fix/dev-setup`) y empezar con la corrección de compilación. Si prefieres, puedo primero generar un inventario completo de FXML y controladores antes de tocar código.

