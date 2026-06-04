package alicanteweb.erp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Herramienta de diagnóstico de readiness para producción.
 * Uso: java -cp ERP.jar alicanteweb.erp.tools.ProductionReadinessCheck
 */
public class ProductionReadinessCheck {

    private static final Logger log = LoggerFactory.getLogger(ProductionReadinessCheck.class);

    private final List<CheckResult> resultados = new ArrayList<>();
    private final ConfigurableApplicationContext ctx;

    public ProductionReadinessCheck(ConfigurableApplicationContext ctx) {
        this.ctx = ctx;
    }

    public static void main(String[] args) {
        System.out.println("=== ERP TAHONA - VERIFICACIÓN DE PRODUCCIÓN ===");
        System.out.println("Fecha: " + LocalDate.now());
        System.out.println("Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println();
    }

    public void ejecutarTodasLasVerificaciones(DataSource dataSource) {
        verificarJava();
        verificarMemoria();
        verificarPerfilActivo();
        verificarBaseDatos(dataSource);
        verificarCertificadoVerifactu();
        verificarVariablesEntorno();
        verificarDirectorios();
        verificarMigraciones(dataSource);
        verificarModulos();
        imprimirResumen();
    }

    private void verificarJava() {
        String version = System.getProperty("java.version");
        boolean ok = version.startsWith("17") || version.startsWith("21") || version.startsWith("23");
        add("Versión Java", ok, "Java " + version,
            ok ? "OK" : "Requiere JDK 17-23. Actual: " + version);
    }

    private void verificarMemoria() {
        Runtime rt = Runtime.getRuntime();
        long maxMb = rt.maxMemory() / (1024 * 1024);
        boolean ok = maxMb >= 512;
        add("Memoria máxima JVM", ok, maxMb + " MB",
            ok ? "OK" : "BAJA: recomienda al menos 512 MB (-Xmx512m)");
    }

    private void verificarPerfilActivo() {
        String[] profiles = ctx.getEnvironment().getActiveProfiles();
        boolean prod = false;
        for (String p : profiles) {
            if ("prod".equals(p) || "production".equals(p)) { prod = true; break; }
        }
        String perfil = profiles.length > 0 ? String.join(", ", profiles) : "default";
        add("Perfil Spring activo", prod, perfil,
            prod ? "OK" : "ATENCIÓN: perfil no es 'prod'. En producción debe usar --spring.profiles.active=prod");
    }

    private void verificarBaseDatos(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL();
            String dbProduct = conn.getMetaData().getDatabaseProductName();
            boolean esMySQL = url.contains("mysql");
            boolean esRemoto = url.contains("//") && !url.contains("localhost") && !url.contains("127.0.0.1");

            add("Conexión BD", true, dbProduct,
                "Conectado a " + url);
            add("Motor BD", esMySQL, dbProduct,
                esMySQL ? "OK" : "ATENCIÓN: solo MySQL está soportado en producción");

            if (!esMySQL) {
                add("BD producción", false, dbProduct,
                    "ERROR: H2 solo para desarrollo/tests. Producción requiere MySQL");
            }
            if (!esRemoto && esMySQL) {
                add("BD local", true, url,
                    "INFO: BD MySQL en localhost (aceptable para entorno pequeño)");
            }

        } catch (Exception e) {
            add("Conexión BD", false, "ERROR",
                "No se pudo conectar: " + e.getMessage());
        }
    }

    private void verificarCertificadoVerifactu() {
        String ksPath = ctx.getEnvironment().getProperty("verifactu.keystore.path", "");
        if (ksPath.isEmpty()) {
            ksPath = System.getenv("VERIFACTU_KEYSTORE_PATH");
        }

        if (ksPath == null || ksPath.isEmpty()) {
            add("Certificado VeriFactu", false, "No configurado",
                "AVISO: Sin certificado no se pueden emitir facturas VeriFactu. Obligatorio desde julio 2025.");
            return;
        }

        Path path = Paths.get(ksPath);
        if (Files.exists(path) && Files.isReadable(path)) {
            add("Certificado VeriFactu", true, ksPath,
                "OK: Certificado presente y legible");
        } else {
            add("Certificado VeriFactu", false, ksPath,
                "ERROR: Archivo no encontrado o sin permisos: " + ksPath);
        }
    }

    private void verificarVariablesEntorno() {
        String[] vars = {
            "DB_URL", "DB_USERNAME", "DB_PASSWORD",
            "AES_SECRET_KEY", "PBKDF2_SECRET"
        };
        for (String var : vars) {
            String val = System.getenv(var);
            boolean ok = val != null && !val.isBlank();
            add("Env: " + var, ok, ok ? "configurada" : "NO configurada",
                ok ? "OK" : "Falta variable de entorno: " + var + " (requerida en prod)");
        }
    }

    private void verificarDirectorios() {
        String[] dirs = {"impresiones", "backups"};
        for (String dir : dirs) {
            File d = new File(dir);
            if (d.exists() && d.isDirectory() && d.canWrite()) {
                add("Directorio: " + dir, true, d.getAbsolutePath(), "OK");
            } else {
                boolean created = d.mkdirs();
                add("Directorio: " + dir, created, d.getAbsolutePath(),
                    created ? "Creado correctamente" : "ERROR: No se pudo crear/escribir");
            }
        }
    }

    private void verificarMigraciones(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM flyway_schema_history");
            if (rs.next()) {
                int count = rs.getInt(1);
                add("Migraciones Flyway", count >= 9, count + " migraciones",
                    count >= 9 ? "OK (" + count + " aplicadas)" : "ATENCIÓN: esperadas >= 9, hay " + count);
            }
            rs.close();
        } catch (Exception e) {
            add("Migraciones Flyway", false, "ERROR",
                "No se pudo verificar tabla flyway_schema_history: " + e.getMessage());
        }
    }

    private void verificarModulos() {
        String[] beans = {
            "facturaService", "clienteService", "articuloService", "proveedorService",
            "recetaService", "ordenProduccionService", "loteService",
            "vehiculoService", "hojaRutaService", "devolucionService",
            "contabilidadService", "backupService", "verifactuService"
        };
        for (String bean : beans) {
            try {
                ctx.getBean(bean);
                add("Bean: " + bean, true, "Presente", "OK");
            } catch (Exception e) {
                add("Bean: " + bean, false, "Falta", "ERROR: no encontrado - " + e.getMessage());
            }
        }
    }

    private void add(String item, boolean ok, String valor, String mensaje) {
        resultados.add(new CheckResult(item, ok, valor, mensaje));
    }

    private void imprimirResumen() {
        long ok = resultados.stream().filter(CheckResult::ok).count();
        long errores = resultados.stream().filter(r -> !r.ok()).count();

        System.out.println();
        System.out.println("=== RESULTADOS DE VERIFICACIÓN ===");

        String estado = "INFO";
        for (CheckResult r : resultados) {
            String icono = r.ok() ? "✓" : "✗";
            if (!r.ok() && r.mensaje().startsWith("ERROR")) estado = "ERROR";
            System.out.printf("  %s %-40s [%s] %s%n", icono, r.item(), estado, r.mensaje());
            if (r.valor() != null) {
                System.out.printf("     → %s%n", r.valor());
            }
        }

        System.out.println();
        System.out.println("─────────────────────────────────────────────");
        System.out.printf("  Total: %d | OK: %d | Fallos: %d%n",
            resultados.size(), ok, errores);
        System.out.println("─────────────────────────────────────────────");

        if (errores > 0) {
            System.out.println();
            System.out.println("CONCLUSIÓN: NO APTA PARA PRODUCCIÓN");
            System.out.println("  Corrija los " + errores + " fallos marcados con ✗ antes de desplegar.");
        } else {
            System.out.println();
            System.out.println("CONCLUSIÓN: APTA PARA PRODUCCIÓN");
            System.out.println("  Todas las verificaciones superadas. Puede desplegar.");
        }
    }

    private record CheckResult(String item, boolean ok, String valor, String mensaje) {}
}
