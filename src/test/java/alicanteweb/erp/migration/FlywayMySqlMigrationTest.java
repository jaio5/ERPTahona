package alicanteweb.erp.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMySqlMigrationTest {

    static MySQLContainer<?> mysql;
    static String jdbcUrl;
    static String username;
    static String password;

    @BeforeAll
    static void prepararMySql() {
        String externalUrl = System.getenv("MIGRATION_TEST_JDBC_URL");
        if (externalUrl != null && !externalUrl.isBlank()) {
            jdbcUrl = externalUrl;
            username = System.getenv().getOrDefault("MIGRATION_TEST_DB_USER", "erp");
            password = System.getenv().getOrDefault("MIGRATION_TEST_DB_PASSWORD", "erp");
            return;
        }

        try {
            mysql = new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("erp_test")
                    .withUsername("erp")
                    .withPassword("erp");
            mysql.start();
            jdbcUrl = mysql.getJdbcUrl();
            username = mysql.getUsername();
            password = mysql.getPassword();
        } catch (RuntimeException ex) {
            Assumptions.abort("Docker/Testcontainers no disponible: " + ex.getMessage());
        }
    }

    @AfterAll
    static void detenerMySql() {
        if (mysql != null && mysql.isRunning()) {
            mysql.stop();
        }
    }

    @Test
    void aplicaTodasLasMigracionesSobreBaseVacia() throws Exception {
        Flyway flyway = flyway(null);
        flyway.clean();

        var result = flyway.migrate();

        assertTrue(result.success);
        flyway.validate();
        String ultimaVersionDisponible = java.util.Arrays.stream(flyway.info().all())
                .map(info -> info.getVersion())
                .filter(java.util.Objects::nonNull)
                .max(MigrationVersion::compareTo)
                .orElseThrow()
                .toString();
        assertEquals(ultimaVersionDisponible, versionActual());
    }

    @Test
    void actualizaDesdeV21EInicializaSecuenciaConAlbaranesExistentes() throws Exception {
        Flyway hastaV21 = flyway("21");
        hastaV21.clean();
        hastaV21.migrate();

        try (Connection connection = connection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                INSERT INTO albaranes_venta (numero, fecha, total, estado)
                VALUES ('ALB-2026-000042', '2026-06-18', 10.00, 'PENDIENTE')
                """);
            // Simula una instalación histórica donde las tablas base existían
            // pero V0 todavía no formaba parte del historial de Flyway.
            statement.executeUpdate(
                    "DELETE FROM flyway_schema_history WHERE version = '0'");
        }

        Flyway completa = flyway(null);
        completa.migrate();
        completa.validate();

        try (Connection connection = connection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                 SELECT ultimo_numero
                 FROM albaran_series
                 WHERE serie = 'ALB' AND ejercicio = 2026
                 """)) {
            assertTrue(result.next());
            assertEquals(42L, result.getLong(1));
        }
    }

    @Test
    void reservaNumerosUnicosBajoConcurrencia() throws Exception {
        Flyway flyway = flyway(null);
        flyway.clean();
        flyway.migrate();

        int reservas = 20;
        var executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Long>> tareas = java.util.stream.IntStream.range(0, reservas)
                    .mapToObj(ignored -> (Callable<Long>) () -> {
                        try (Connection connection = connection()) {
                            connection.setAutoCommit(false);
                            int filasAfectadas;
                            try (PreparedStatement reserve = connection.prepareStatement("""
                                INSERT INTO albaran_series (serie, ejercicio, ultimo_numero)
                                VALUES ('ALB', 2028, 1)
                                ON DUPLICATE KEY UPDATE
                                  ultimo_numero = LAST_INSERT_ID(ultimo_numero + 1)
                                """)) {
                                filasAfectadas = reserve.executeUpdate();
                            }
                            long numero = 1L;
                            if (filasAfectadas != 1) {
                                try (Statement statement = connection.createStatement();
                                     ResultSet result = statement.executeQuery("SELECT LAST_INSERT_ID()")) {
                                    assertTrue(result.next());
                                    numero = result.getLong(1);
                                }
                            }
                            connection.commit();
                            return numero;
                        }
                    })
                    .toList();

            Set<Long> numeros = new HashSet<>();
            for (var future : executor.invokeAll(tareas)) {
                numeros.add(future.get());
            }

            assertEquals(reservas, numeros.size());
            assertTrue(numeros.contains(1L));
            assertTrue(numeros.contains((long) reservas));
        } finally {
            executor.shutdownNow();
        }
    }

    private Flyway flyway(String target) {
        var configuration = Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .cleanDisabled(false)
                .ignoreMigrationPatterns("*:ignored");
        if (target != null) {
            configuration.target(MigrationVersion.fromVersion(target));
        }
        return configuration.load();
    }

    private String versionActual() throws Exception {
        try (Connection connection = connection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("""
                 SELECT version
                 FROM flyway_schema_history
                 WHERE success = 1
                 ORDER BY installed_rank DESC
                 LIMIT 1
                 """)) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private Connection connection() throws Exception {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
