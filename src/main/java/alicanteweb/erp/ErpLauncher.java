// java
package alicanteweb.erp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Lanzador combinado Spring Boot + JavaFX.
 *
 * Comentarios para un estudiante de DAM:
 * - Esta clase extiende `javafx.application.Application` y por tanto es el punto
 *   de entrada de la parte gráfica (JavaFX).
 * - Al mismo tiempo arranca un contexto de Spring Boot para poder usar beans,
 *   inyección de dependencias, JPA, repositorios, etc. dentro de los controladores
 *   JavaFX.
 *
 * Flujo de arranque (resumen):
 * 1. JavaFX llama a `init()` antes de crear la UI. Aquí arrancamos Spring Boot.
 * 2. JavaFX llama a `start(Stage)` donde cargamos el FXML y mostramos la ventana.
 * 3. Al cerrar la aplicación `stop()` se encarga de cerrar el contexto de Spring
 *    y terminar JavaFX.
 *
 * Objetivo pedagógico:
 * - Que entiendas cómo combinar un framework de servidor (Spring Boot) con una UI
 *   de escritorio (JavaFX), y cómo los controladores FXML pueden recibir servicios
 *   gestionados por Spring (repositorios JPA, servicios de negocio, etc.).
 */
public class ErpLauncher extends Application {
    // Guardamos el contexto de Spring para poder pedir beans desde los controladores
    // JavaFX (esto será útil cuando el FXMLLoader pida un controller, lo resolveremos
    // a través de springContext::getBean).
    private static ConfigurableApplicationContext springContext;

    /**
     * init() se ejecuta antes de start() en el hilo de JavaFX.
     * Aquí arrancamos Spring Boot para tener disponible el contexto y los beans
     * cuando vayamos a cargar los controladores FXML.
     *
     * Nota: no hacemos operaciones gráficas intensivas aquí; sólo arrancamos
     * el contexto de Spring.
     */
    @Override
    public void init() {
        // SpringApplicationBuilder arranca la aplicación Spring Boot usando la
        // clase principal `SpringBootApp` (debe existir en el proyecto).
        // El resultado es un `ConfigurableApplicationContext` que contiene
        // los beans (servicios, repositorios, controladores anotados como
        // @Component/@Service/@Controller, etc.).
        springContext = new SpringApplicationBuilder(SpringBootApp.class).run();
    }

    /**
     * start() es llamado por JavaFX cuando la aplicación gráfica debe inicializarse.
     * Aquí cargamos el FXML principal y mostramos el Stage.
     *
     * Explicación clave para integración con Spring:
     * - `FXMLLoader` crea instancias de los controladores (normalmente con `new`).
     * - Queremos que Spring sea el responsable de crear y gestionar esos controladores
     *   para que puedan recibir dependencias (servicios JPA, servicios de verifactur,
     *   etc.). Para eso usamos `loader.setControllerFactory(springContext::getBean)`.
     * - Con `setControllerFactory` le decimos al FXMLLoader: "cuando necesites un
     *   controlador, pídelo a Spring (springContext.getBean(Class))".
     * - En los controladores FXML, en lugar de usar `@Autowired`, se recomienda
     *   inyección por constructor. Spring inyectará los servicios automáticamente.
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Cargamos el fichero FXML que contiene la definición de la UI principal.
        // Asegúrate de que `src/main/resources/ui/main_panel.fxml` existe y tiene
        // el fx:controller correcto o que el controlador se registra como bean en Spring.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_panel.fxml"));

        // Esta línea es la que permite que los controladores FXML sean beans de Spring.
        // Cuando el FXMLLoader necesite crear el controlador, llamará a
        // springContext.getBean(tipoDelControlador).
        // Ventajas:
        //  - Los controladores pueden recibir repositorios/servicios por constructor.
        //  - Podemos aplicar transacciones, AOP y otras funcionalidades de Spring.
        loader.setControllerFactory(springContext::getBean);

        // Cargar la jerarquía de nodos definida en el FXML.
        Parent root = loader.load();

        // Crear la escena y mostrar la ventana principal.
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("ERP Tahona");
        stage.show();
    }

    /**
     * stop() se llama cuando la aplicación cierra. Hay que cerrar el contexto de
     * Spring para liberar recursos (conexiones, hilos, etc.) y terminar JavaFX.
     */
    @Override
    public void stop() {
        if (springContext != null) springContext.close();
        Platform.exit();
    }

    /**
     * Método main para lanzar JavaFX. No necesitamos llamar a Spring Boot aquí;
     * JavaFX invocará init() y start() automáticamente.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
