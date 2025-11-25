// java
package alicanteweb.erp;

import javafx.application.Application;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class ErpLauncher {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        // Iniciar Spring antes de lanzar JavaFX para que el contexto esté disponible en init().
        context = new SpringApplicationBuilder(SpringBootApp.class).run(args);
        Application.launch(ErpFxApplication.class, args);
    }

    public static ConfigurableApplicationContext getContext() {
        return context;
    }
}
