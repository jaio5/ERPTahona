Estructura del proyecto ERP (Java 17, Spring Boot, JPA, JavaFX con FXML)

Objetivo
- Aplicación de escritorio JavaFX que usa Spring Boot y JPA para acceso a la base de datos.
- Evitar @Autowired: inyección por constructor y uso del ApplicationContext en los controladores FXML.

Estructura recomendada (ya presente en el proyecto)

- src/main/java/
  - alicanteweb.erp
    - SpringBootApp.java                # clase @SpringBootApplication (configuración principal)
    - ErpLauncher.java                 # arranca Spring y lanza JavaFX
    - ErpFxApplication.java            # extiende javafx.application.Application e integra Spring
    - ErpApplication.java              # alternativa que arranca Spring dentro de JavaFX (opcional)

  - alicanteweb.erp.entities/         # JPA @Entity
    - Articulo.java
    - Cliente.java
    - Factura.java
    - FacturaLinea.java
    - Almacene.java
    - AlbaranesVenta.java
    - ...

  - alicanteweb.erp.repository/       # interfaces JPA (extienden JpaRepository)
    - ArticuloRepository.java
    - ClienteRepository.java
    - FacturaRepository.java
    - FacturaLineaRepository.java
    - AlmaceneRepository.java
    - AlbaranesVentaRepository.java
    - ...

  - alicanteweb.erp.service/          # servicios (componentes sin @Autowired)
    - ArticuloService.java
    - ClienteService.java
    - FacturaService.java
    - FacturaLineaService.java
    - AlmaceneService.java
    - AlbaranesVentaService.java

  - alicanteweb.erp.controller/       # controladores JavaFX (anotados con @Controller o @Component opcionalmente)
    - ui/                             # controllers de la UI principal
    - ArticuloController.java
    - ClienteController.java
    - FacturaController.java
    - FacturaLineaController.java
    - AlmaceneController.java
    - AlbaranesVentaController.java

- src/main/resources/
  - application.properties            # configuración (datasource, JPA, perfiles)
  - ui/                               # archivos FXML
    - main_panel.fxml
    - facturas.fxml
    - factura-lineas.fxml
    - articulos.fxml
    - ...
  - certs/
  - css/

Buenas prácticas para este proyecto
- Java 17: en pom.xml asegurarse de <java.version>17</java.version>.
- JPA: usar Spring Data JPA (JpaRepository) para repositorios.
- Spring Boot: crear una clase @SpringBootApplication (ya añadida: SpringBootApp).
- JavaFX + Spring: cargar FXML con FXMLLoader y setControllerFactory(context::getBean) para inyectar beans por constructor.
- No usar @Autowired: usar inyección por constructor; marcar servicios/repositorios como @Service/@Repository o interfaces JpaRepository.
- Manejo de errores: para aplicación de escritorio, usar excepciones específicas y diálogos; no dependes de controlador global web.

Cómo arrancar la aplicación localmente (desarrollo)
1. Tener JDK 17 instalado y JAVA_HOME configurado.
2. Tener Maven instalado (o usar el wrapper mvnw con permisos de ejecución).
3. Desde la raíz del proyecto:
   - Con Maven: mvn javafx:run -DskipTests
   - Con wrapper: ./mvnw -DskipTests package && java -jar target/*.jar

Cómo ejecutar rápido sin MySQL (H2 en memoria)

Si no quieres configurar MySQL local, existe un perfil `dev-h2` para desarrollo que usa H2 en memoria.

PowerShell – ejecutar con H2 (usa el wrapper incluido):

```powershell
# Limpia e inicia la aplicación usando JavaFX plugin
.\mvnw clean javafx:run -Dspring.profiles.active=dev-h2

# O empaqueta y ejecuta el jar (parámetros para H2)
.\mvnw -DskipTests clean package
java -jar .\target\ERP-0.0.1.jar --spring.profiles.active=dev-h2
```

Notas:
- Asegúrate de tener JDK 17 y `JAVA_HOME` configurado. Si no tienes Maven instalado, usa `mvnw` o `mvnw.cmd` (ö el wrapper de Windows) que está incluido en el proyecto.
- Si prefieres usar MySQL, revisa `src/main/resources/application-dev.properties` y actualiza la URL/credenciales.

Checklist para completar el frontend y errores detectados
- [x] Añadir SpringBootApp para arrancar Spring Boot.
- [x] Cambiar ErpLauncher para iniciar Spring antes de JavaFX y exponer el contexto.
- [x] ErpFxApplication ya preparado para cargar FXML con setControllerFactory.
- [ ] Asegurarse que todos los controladores FXML están definidos como beans (marcados @Controller o @Component) y usan inyección por constructor.
- [ ] Reemplazar cualquier @Autowired por inyección por constructor en todo el proyecto.
- [ ] Probar mvn package y ejecutar la app JavaFX.

Próximos pasos sugeridos
- Ejecutar una búsqueda para eliminar todos los @Autowired (ya solicitada anteriormente).
- Verificar que todos los controladores FXML tienen un bean en Spring y que los fxml usan fx:controller si no se usa controllerFactory.
- Implementar o revisar services/repositories faltantes y adaptarlos a JPA.

Si quieres, puedo:
- Ejecutar la búsqueda y eliminar todos los @Autowired automáticamente.
- Marcar y corregir los controladores FXML que no se registren como beans.
- Generar plantillas para servicios, repositorios y controladores faltantes.

---
Archivo generado automáticamente por la herramienta de soporte del proyecto.
