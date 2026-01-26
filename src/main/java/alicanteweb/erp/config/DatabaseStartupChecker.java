package alicanteweb.erp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class DatabaseStartupChecker implements EnvironmentPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(DatabaseStartupChecker.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String[] profiles = environment.getActiveProfiles();
        boolean prodActive = false;
        if (profiles != null) {
            for (String p : profiles) {
                if ("prod".equalsIgnoreCase(p) || "production".equalsIgnoreCase(p)) {
                    prodActive = true;
                    break;
                }
            }
        }

        if (!prodActive) {
            // not production - do nothing
            log.debug("DatabaseStartupChecker: production profile not active ({}). Skipping DB password enforcement.", (profiles==null?"none":String.join(",", profiles)));
            return;
        }

        String url = environment.getProperty("spring.datasource.url", "");
        String password = environment.getProperty("spring.datasource.password", "");

        if (url != null && url.toLowerCase().contains("mysql")) {
            if (password == null || password.isBlank()) {
                String msg = "[SECURITY] Production profile active but 'spring.datasource.password' (DB_PASSWORD) is not set.\n"
                        + "Provide DB_PASSWORD via environment variable or JVM property. Example (PowerShell): $env:DB_PASSWORD='<pwd>'; .\\mvnw.cmd -Dspring.profiles.active=prod javafx:run";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
        }
    }
}
