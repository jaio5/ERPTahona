package alicanteweb.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Guard de arranque VeriFactu: en producción, la remisión real (aeat.enabled=true)
 * no puede apuntar al entorno de pruebas de AEAT (prewww*): las facturas se
 * registrarían en la plataforma de pruebas sin validez fiscal.
 */
class DatabaseStartupCheckerVerifactuTest {

    private static final String ENDPOINT_PRUEBAS = "https://prewww2.aeat.es/wlpl/AVAC-FACT/ws/fe/SiiVerifactu";
    private static final String ENDPOINT_PRODUCCION = "https://www1.agenciatributaria.gob.es/wlpl/TIKE-CONT/ws/SistemaFacturacion";

    private final DatabaseStartupChecker checker = new DatabaseStartupChecker();

    /** Entorno prod mínimo que pasa el resto de validaciones del checker. */
    private MockEnvironment entornoProdValido() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.url", "jdbc:mysql://db:3306/tahona");
        env.setProperty("spring.datasource.username", "tahona");
        env.setProperty("spring.datasource.password", "clave-real-de-bd");
        env.setProperty("cifrado.aes.key", "8Yy4x1vVecFYidPZK3v0eYc8mThp0BSteJpDcvvzuQg=");
        env.setProperty("security.pbkdf2.secret", "secreto-pbkdf2-de-mas-de-32-caracteres!!");
        env.setProperty("admin.default.password", "AdminSegura#2026");
        env.setProperty("spring.jpa.hibernate.ddl-auto", "validate");
        // Certificado configurado: prerequisito para poder habilitar la remisión AEAT
        env.setProperty("verifactu.keystore.path", "certs/prod.p12");
        env.setProperty("verifactu.keystore.password", "clave-keystore");
        env.setProperty("verifactu.key.alias", "mi_certificado");
        env.setProperty("verifactu.key.password", "clave-keystore");
        return env;
    }

    @Test
    void placeholdersDelEnvExampleImpidenElArranqueEnProduccion() {
        // Contraseña de BD tal cual viene en .env.example
        MockEnvironment env1 = entornoProdValido();
        env1.setProperty("spring.datasource.password", "cambia-esta-app");
        assertThatThrownBy(() -> checker.postProcessEnvironment(env1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.password");

        // ADMIN_DEFAULT_PASSWORD tal cual viene en .env.example
        MockEnvironment env2 = entornoProdValido();
        env2.setProperty("admin.default.password", "CambiaEstaClave123");
        assertThatThrownBy(() -> checker.postProcessEnvironment(env2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("admin.default.password");
    }

    @Test
    void remisionRealConEndpointDePruebasImpideElArranque() {
        MockEnvironment env = entornoProdValido();
        env.setProperty("verifactu.aeat.enabled", "true");
        env.setProperty("verifactu.aeat.endpoint", ENDPOINT_PRUEBAS);

        assertThatThrownBy(() -> checker.postProcessEnvironment(env, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Endpoint de pruebas AEAT");
    }

    @Test
    void remisionRealConEndpointDeProduccionArranca() {
        MockEnvironment env = entornoProdValido();
        env.setProperty("verifactu.aeat.enabled", "true");
        env.setProperty("verifactu.aeat.endpoint", ENDPOINT_PRODUCCION);

        assertThatCode(() -> checker.postProcessEnvironment(env, null))
                .doesNotThrowAnyException();
    }

    @Test
    void remisionDeshabilitadaConEndpointDePruebasArranca() {
        MockEnvironment env = entornoProdValido();
        env.setProperty("verifactu.aeat.enabled", "false");
        env.setProperty("verifactu.aeat.endpoint", ENDPOINT_PRUEBAS);
        // QR de producción con endpoint de pruebas: solo un WARN, no bloquea
        env.setProperty("verifactu.qr.base-url", "https://www2.agenciatributaria.gob.es/wlpl/TIKE-CONT/ValidarQR");

        assertThatCode(() -> checker.postProcessEnvironment(env, null))
                .doesNotThrowAnyException();
    }

    @Test
    void fueraDeProduccionElCheckerNoInterviene() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        env.setProperty("verifactu.aeat.enabled", "true");
        env.setProperty("verifactu.aeat.endpoint", ENDPOINT_PRUEBAS);

        assertThatCode(() -> checker.postProcessEnvironment(env, null))
                .doesNotThrowAnyException();
    }

    @Test
    void conFlywayActivoUnDdlAutoQueMutaElEsquemaImpideElArranque() {
        for (String ddlPeligroso : new String[]{"update", "create", "create-drop"}) {
            MockEnvironment env = new MockEnvironment();
            env.setActiveProfiles("dev");
            env.setProperty("spring.flyway.enabled", "true");
            env.setProperty("spring.jpa.hibernate.ddl-auto", ddlPeligroso);

            assertThatThrownBy(() -> checker.postProcessEnvironment(env, null))
                    .as("ddl-auto=%s con Flyway activo debe bloquear el arranque", ddlPeligroso)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("drift");
        }
    }

    @Test
    void conFlywayActivoValidateYNoneArrancan() {
        for (String ddlSeguro : new String[]{"validate", "none"}) {
            MockEnvironment env = new MockEnvironment();
            env.setActiveProfiles("dev");
            env.setProperty("spring.flyway.enabled", "true");
            env.setProperty("spring.jpa.hibernate.ddl-auto", ddlSeguro);

            assertThatCode(() -> checker.postProcessEnvironment(env, null))
                    .as("ddl-auto=%s con Flyway activo debe permitir el arranque", ddlSeguro)
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void conFlywayDeshabilitadoSePermiteQueHibernateCreeElEsquema() {
        // Escenario de tests: H2 en memoria, Flyway off, Hibernate construye el esquema.
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("test");
        env.setProperty("spring.flyway.enabled", "false");
        env.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");

        assertThatCode(() -> checker.postProcessEnvironment(env, null))
                .doesNotThrowAnyException();
    }
}
