package alicanteweb.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EntityScan(basePackages = "alicanteweb.erp.entities")
@EnableJpaRepositories(basePackages = "alicanteweb.erp.repository")
public class ErpApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ErpApplication.class, args);

        Environment env = ctx.getEnvironment();
        boolean launchUi = Boolean.parseBoolean(env.getProperty("erp.launch-ui-on-start", "false"));
        if (launchUi) {
            // Lanzar el cliente JavaFX en un proceso separado (Windows)
            new Thread(() -> {
                try {
                    String projectDir = System.getProperty("user.dir");
                    String mvnw = projectDir + File.separator + "mvnw.cmd";
                    String modulePom = projectDir + File.separator + "javafx-client" + File.separator + "pom.xml";

                    List<String> command = new ArrayList<>();
                    command.add("cmd.exe");
                    command.add("/c");
                    // start abre una nueva ventana y no bloquea la consola actual
                    command.add("start");
                    command.add("ERP-JavaFX");
                    command.add(mvnw);
                    command.add("-f");
                    command.add(modulePom);
                    command.add("javafx:run");

                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.directory(new File(projectDir));
                    pb.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }

}
