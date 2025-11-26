# Estructura propuesta y guía de desarrollo para ERP Tahona

Este documento explica la estructura del proyecto que vamos a mantener y sirve como guía para un estudiante de DAM que comienza con Spring Boot, JPA y JavaFX (FXML).

1) Objetivo
- Aplicación de escritorio (JavaFX + FXML) que usa Spring Boot y JPA para acceder a la base de datos.
- Verifactur: módulo encargado de registrar/transmitir facturas a AEAT (de momento veremos solo registro y previsualización).

2) Estructura de carpetas relevante
- src/main/java/alicanteweb/erp
  - SpringBootApp.java  -> Clase principal de Spring Boot (annotada con @SpringBootApplication)
  - ErpLauncher.java    -> Lanzador JavaFX que arranca Spring Boot (explicado en clase)
  - model/              -> Entidades JPA (Factura, Cliente, Articulo, Almacen, etc.)
  - repository/         -> Repositorios JPA (interfaces que extienden JpaRepository)
  - service/            -> Servicios de negocio (lógica, transacciones)
  - ui/                 -> Controladores FXML anotados con @Component (no usar @Autowired)
  - verifactur/         -> Código relacionado con la integración AEAT y modelos asociados

- src/main/resources/ui
  - fxml files (main_panel.fxml, facturas.fxml, clientes.fxml, articulos.fxml...)
- src/main/resources/application.properties (y profiles application-dev.properties,...)

3) Patrón de integración JavaFX + Spring
- `ErpLauncher` arranca Spring Boot en `init()` y, en `start()`, carga los FXML usando
  `loader.setControllerFactory(springContext::getBean)`. Esto permite que los controladores
  FXML sean beans de Spring y reciban dependencias por constructor.
- En los controladores FXML:
  - Marcar con `@Component` para que Spring los detecte.
  - No usar `@Autowired`. Preferir inyección por constructor.
  - Definir campos `@FXML` para enlazar con elementos del FXML.

4) JPA y repositorios
- Todas las entidades deben estar anotadas con `@Entity` y tener un `@Id`.
- Crear repositorios que extiendan `JpaRepository<T, ID>`.
- Los servicios usan los repositorios inyectados por constructor y aplican anotaciones
  como `@Transactional` cuando sea necesario.

5) Verifactur (concepto y flujo)
- Propósito: registrar envío de facturas a la AEAT y almacenar el estado de cada envío.
- Componentes:
  - Entidad `VerifacturRecord` (id, facturaId, estado, mensajeRespuesta, fechaEnvio, etc.)
  - Repositorio `VerifacturRecordRepository` (JpaRepository)
  - Servicio `VerifacturService` que:
    - Valida la factura y genera representación (XML/JSON) requerida por la AEAT
    - Realiza la llamada a la AEAT (simulada/real) y guarda el resultado en `VerifacturRecord`
    - Expone métodos para obtener el registro y su estado
  - UI (FXML) `VerifacturController` que muestra la tabla de envíos y permite reintentar
    envíos manualmente o ver la respuesta.

6) Conexión entre FXML y servicios (ejemplo práctico)
- FXML `facturas.fxml` define un botón "Emitir" con `onAction="#onEmitirFactura"`.
- Controlador `FacturaFxController` (bean Spring) tiene método `onEmitirFactura()`.
- Dentro de `onEmitirFactura()`:
  - Se crea/actualiza la entidad `Factura` y se guarda con `facturaService.save(...)`.
  - Se llama a `verifacturService.enviar(factura)` que devuelve un `VerifacturRecord`.
  - Se actualiza la UI (tabla de verifactur) y se muestra previsualización si procede.

7) Buenas prácticas y consejos
- Usar inyección por constructor en todos los beans (evitar @Autowired).
- Mantener la lógica de negocio en `service/` y la lógica de presentación en `ui/`.
- Evitar mezclar JPA y operaciones con UI en el hilo de JavaFX; si una operación es lenta,
  ejecutarla en un hilo/coroutine y actualizar la UI con Platform.runLater.
- Para previsualizar facturas en la UI usar `WebView` y generar HTML o generar PDF
  y mostrar un viewer.

8) Ajustes para ejecutar la aplicación (resumen rápido)
- Añadir plugin JavaFX en `pom.xml` o usar `--module-path` y `--add-modules` en las VM options
  para que la JVM encuentre los módulos JavaFX si usas una JDK modular.
- Ejecutar con Java 17 (ya configurado en tu entorno), y comprobar que las dependencias
  de `org.openjfx` están en el repositorio local.

9) Cómo comentar el código para aprender
- Cada clase debe incluir un comentario al principio que explique su responsabilidad.
- Métodos clave (start, init, save, enviar) deben tener javadoc o comentarios
  que expliquen entradas/salidas y efectos secundarios.

10) Próximos pasos sugeridos
- Revisar las entidades existentes en `model/` y asegurarse de que tienen relaciones bien
  mapeadas (@OneToMany, @ManyToOne) y `fetch` adecuados.
- Implementar los repositorios y servicios para las entidades que faltan.
- Crear/ajustar los FXML y controladores con bindings (Properties) para que la UI sea reactiva.

---

Si quieres, ahora puedo:
- Añadir comentarios similares a otras clases clave del proyecto (por ejemplo `FacturaService`,
  `FacturaFxController`, `VerifacturService`) para que las entiendas.
- Generar ejemplos de código de cómo llamar a la AEAT (simulado) y registrar la respuesta.

Dime qué prefieres que haga a continuación: comentar clases concretas o añadir ejemplos prácticos
(en código) para ver la previsualización e integración con la base de datos.
