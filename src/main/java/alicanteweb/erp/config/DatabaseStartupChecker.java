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
        // Anti-drift (todos los perfiles): si Flyway gestiona el esquema, Hibernate no
        // debe poder alterarlo. Se aplica antes que las comprobaciones de producción.
        enforceNoSchemaDrift(environment);

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

        // VeriFactu: sin VERIFACTU_CERT_PATH la firma queda deshabilitada (arranque permitido);
        // con certificado o remisión AEAT activa se exige la configuración completa
        String keystorePath = environment.getProperty("verifactu.keystore.path", "");
        boolean aeatEnabled = Boolean.parseBoolean(environment.getProperty("verifactu.aeat.enabled", "false"));
        if (keystorePath != null && !keystorePath.isBlank()) {
            requireProdValue(environment, "verifactu.keystore.path", "VERIFACTU_CERT_PATH");
            requireProdValue(environment, "verifactu.keystore.password", "VERIFACTU_CERT_PASSWORD");
            requireProdValue(environment, "verifactu.key.alias", "VERIFACTU_KEY_ALIAS");
            requireProdValue(environment, "verifactu.key.password", "VERIFACTU_KEY_PASSWORD");
        } else if (aeatEnabled) {
            fail("[SECURITY] verifactu.aeat.enabled=true requires a certificate: set VERIFACTU_CERT_PATH.");
        } else {
            log.warn("VERIFACTU_CERT_PATH vacio: firma y remision VeriFactu deshabilitadas hasta configurar el certificado.");
        }

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

        if (aeatEnabled) {
            requireProdValue(environment, "verifactu.aeat.endpoint", "VERIFACTU_AEAT_ENDPOINT");
        }

        // VeriFactu: en producción, la remisión real no puede apuntar al entorno de
        // pruebas de AEAT (prewww*). Las facturas se registrarían en la plataforma de
        // pruebas y no tendrían validez fiscal.
        String aeatEndpoint = environment.getProperty("verifactu.aeat.endpoint", "");
        boolean endpointDePruebas = aeatEndpoint.toLowerCase().contains("prewww");
        if (aeatEnabled && endpointDePruebas) {
            fail("[FISCAL] Endpoint de pruebas AEAT con remisión real habilitada: verifactu.aeat.enabled=true"
                    + " pero verifactu.aeat.endpoint apunta a '" + aeatEndpoint
                    + "'. Configura VERIFACTU_AEAT_ENDPOINT con la URL de producción o desactiva VERIFACTU_AEAT_ENABLED.");
        }
        String qrBaseUrl = environment.getProperty("verifactu.qr.base-url", "");
        if (endpointDePruebas && !qrBaseUrl.isBlank() && !qrBaseUrl.toLowerCase().contains("prewww")) {
            log.warn("[FISCAL] verifactu.qr.base-url apunta a producción ({}) mientras verifactu.aeat.endpoint"
                    + " es el de pruebas ({}): los QR de cotejo no encontrarán las facturas.", qrBaseUrl, aeatEndpoint);
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

    /**
     * Evita el drift de esquema: cuando Flyway gestiona las migraciones, Hibernate
     * no debe crear ni alterar tablas por su cuenta. Rechaza el arranque —en
     * cualquier perfil— si {@code spring.jpa.hibernate.ddl-auto} es un valor que
     * muta el esquema ({@code create}, {@code create-drop} o {@code update}) mientras
     * Flyway está habilitado. Con Flyway deshabilitado (p. ej. tests con H2) Hibernate
     * construye el esquema legítimamente y no se aplica la restricción.
     */
    private void enforceNoSchemaDrift(Environment environment) {
        boolean flywayEnabled = Boolean.parseBoolean(environment.getProperty("spring.flyway.enabled", "true"));
        if (!flywayEnabled) {
            return;
        }
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto", "");
        String normalized = ddlAuto == null ? "" : ddlAuto.trim().toLowerCase();
        if (normalized.equals("create") || normalized.equals("create-drop") || normalized.equals("update")) {
            fail("[SCHEMA] Con Flyway habilitado, spring.jpa.hibernate.ddl-auto='" + ddlAuto
                    + "' permitiria que Hibernate alterase el esquema (riesgo de drift)."
                    + " Usa 'validate' o 'none' y define los cambios de esquema como migraciones Flyway.");
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
