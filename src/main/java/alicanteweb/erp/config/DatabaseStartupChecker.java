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

        requireProdValue(environment, "spring.datasource.url", "SPRING_DATASOURCE_URL");
        requireProdValue(environment, "spring.datasource.username", "SPRING_DATASOURCE_USERNAME");
        requireProdValue(environment, "spring.datasource.password", "SPRING_DATASOURCE_PASSWORD");
        requireProdValue(environment, "cifrado.aes.key", "CIFRADO_AES_KEY");
        requireProdValue(environment, "security.pbkdf2.secret", "SECURITY_PBKDF2_SECRET");
        requireProdValue(environment, "admin.default.password", "ADMIN_DEFAULT_PASSWORD");
        requireMinLength(environment, "security.pbkdf2.secret", 32);
        requireMinLength(environment, "admin.default.password", 12);

        requireProdValue(environment, "verifactu.keystore.path", "VERIFACTU_CERT_PATH");
        requireProdValue(environment, "verifactu.keystore.password", "VERIFACTU_CERT_PASSWORD");
        requireProdValue(environment, "verifactu.key.alias", "VERIFACTU_KEY_ALIAS");
        requireProdValue(environment, "verifactu.key.password", "VERIFACTU_KEY_PASSWORD");

        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto", "");
        if (!"validate".equalsIgnoreCase(ddlAuto)) {
            fail("Production profile requires spring.jpa.hibernate.ddl-auto=validate. Current value: " + ddlAuto);
        }

        String fallbackH2 = firstNonBlank(
                environment.getProperty("erp.fallback-h2.enabled"),
                System.getenv("ERP_FALLBACK_H2_ENABLED"),
                "false"
        );
        if (Boolean.parseBoolean(fallbackH2)) {
            fail("Production profile cannot run with ERP_FALLBACK_H2_ENABLED=true.");
        }

        boolean aeatEnabled = Boolean.parseBoolean(environment.getProperty("verifactu.aeat.enabled", "false"));
        if (aeatEnabled) {
            requireProdValue(environment, "verifactu.aeat.endpoint", "VERIFACTU_AEAT_ENDPOINT");
        }

        String url = environment.getProperty("spring.datasource.url", "");
        String password = environment.getProperty("spring.datasource.password", "");

        if (url == null || !url.toLowerCase().startsWith("jdbc:mysql:")) {
            fail("Production profile requires a MySQL JDBC URL. Current value: " + url);
        }

        if (url != null && url.toLowerCase().contains("mysql")) {
            if (password == null || password.isBlank()) {
                fail("[SECURITY] Production profile active but 'spring.datasource.password' is not set.");
            }
        }
    }

    private void requireProdValue(Environment environment, String propertyName, String envName) {
        String value = environment.getProperty(propertyName);
        if (isUnsafeValue(value)) {
            fail("[SECURITY] Production profile requires a real value for '" + propertyName
                    + "' via " + envName + ". Placeholders/defaults are not allowed.");
        }
    }

    private boolean isUnsafeValue(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.contains("change-me")
                || normalized.contains("changeme")
                || normalized.contains("mysql-host")
                || normalized.contains("<")
                || normalized.equals("base64-32-byte-key")
                || normalized.equals("admin")
                || normalized.equals("password")
                || normalized.equals("root");
    }

    private void requireMinLength(Environment environment, String propertyName, int minLength) {
        String value = environment.getProperty(propertyName);
        if (value == null || value.length() < minLength) {
            fail("[SECURITY] Production profile requires '" + propertyName
                    + "' to have at least " + minLength + " characters.");
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private void fail(String message) {
        log.error(message);
        throw new IllegalStateException(message);
    }
}
