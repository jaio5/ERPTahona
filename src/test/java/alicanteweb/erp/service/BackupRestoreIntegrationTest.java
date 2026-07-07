package alicanteweb.erp.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MySQLContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Ensayo real del ciclo backup → restore contra un MySQL efímero (Testcontainers),
 * usando los mismos binarios mysqldump/mysql que usa BackupService en ejecución.
 *
 * Se omite (no falla) cuando Docker o los binarios cliente de MySQL no están
 * disponibles, igual que FlywayMySqlMigrationTest.
 */
class BackupRestoreIntegrationTest {

    static MySQLContainer<?> mysql;

    @BeforeAll
    static void prepararMySql() {
        try {
            mysql = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("erp_backup_test")
                    .withUsername("erp")
                    .withPassword("erp");
            mysql.start();
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
    void backupInsertarRestaurar_elRegistroPosteriorAlBackupDesaparece(@TempDir Path backupDir) throws Exception {
        BackupService service = servicioConfigurado(backupDir);
        Assumptions.assumeTrue(service.verificarDisponibilidad(),
                "mysqldump no disponible en esta máquina; se omite el ensayo de restore");
        Assumptions.assumeTrue(clienteMysqlDisponible(),
                "cliente mysql no disponible en esta máquina; se omite el ensayo de restore");

        try (Connection conn = conexion(); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS ensayo_restore (id INT PRIMARY KEY, nombre VARCHAR(50))");
            st.executeUpdate("DELETE FROM ensayo_restore");
            st.executeUpdate("INSERT INTO ensayo_restore VALUES (1, 'antes-del-backup')");
        }

        String rutaBackup = service.realizarBackup();
        assertTrue(Files.exists(Paths.get(rutaBackup)), "el fichero de backup debe existir");
        assertTrue(Files.size(Paths.get(rutaBackup)) > 0, "el fichero de backup no debe estar vacío");
        // El fichero de credenciales temporal no debe quedar en el directorio persistido
        try (var ficheros = Files.list(backupDir)) {
            assertTrue(ficheros.allMatch(f -> f.getFileName().toString().endsWith(".sql")),
                    "en el directorio de backups solo debe haber ficheros .sql");
        }

        try (Connection conn = conexion(); Statement st = conn.createStatement()) {
            st.executeUpdate("INSERT INTO ensayo_restore VALUES (2, 'despues-del-backup')");
        }
        assertEquals(2, contarFilas());

        // Restaurar pasando solo el nombre del fichero, como hace el controlador web
        service.restaurarBackup(Paths.get(rutaBackup).getFileName().toString());

        assertEquals(1, contarFilas(), "la fila insertada tras el backup debe desaparecer al restaurar");
        try (Connection conn = conexion(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre FROM ensayo_restore WHERE id = 1")) {
            assertTrue(rs.next());
            assertEquals("antes-del-backup", rs.getString(1));
        }
    }

    private BackupService servicioConfigurado(Path backupDir) {
        BackupService service = new BackupService(
                mock(FacturacionEventoService.class), mock(EmailService.class));
        // Credenciales root del contenedor: garantizan permisos para dump y DROP/CREATE
        ReflectionTestUtils.setField(service, "dbUsername", "root");
        ReflectionTestUtils.setField(service, "dbPassword", mysql.getPassword());
        ReflectionTestUtils.setField(service, "dbUrl", mysql.getJdbcUrl());
        ReflectionTestUtils.setField(service, "backupDirectory", backupDir.toString());
        ReflectionTestUtils.setField(service, "mysqldumpPath", resolverBinario("BACKUP_MYSQLDUMP_PATH", "mysqldump"));
        ReflectionTestUtils.setField(service, "mysqlPath", resolverBinario("BACKUP_MYSQL_PATH", "mysql"));
        return service;
    }

    /**
     * Misma resolución que la configuración de la app: variable de entorno si existe,
     * la instalación estándar de Windows usada en dev, o el PATH del sistema.
     */
    private static String resolverBinario(String envVar, String nombre) {
        String env = System.getenv(envVar);
        if (env != null && !env.isBlank()) {
            return env;
        }
        Path windowsDefault = Paths.get("C:/Program Files/MySQL/MySQL Server 8.0/bin", nombre + ".exe");
        if (Files.exists(windowsDefault)) {
            return windowsDefault.toString();
        }
        return nombre;
    }

    private boolean clienteMysqlDisponible() {
        try {
            Process p = new ProcessBuilder(resolverBinario("BACKUP_MYSQL_PATH", "mysql"), "--version")
                    .redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            return p.waitFor() == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private int contarFilas() throws Exception {
        try (Connection conn = conexion(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM ensayo_restore")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Connection conexion() throws Exception {
        return DriverManager.getConnection(mysql.getJdbcUrl(), "root", mysql.getPassword());
    }
}
