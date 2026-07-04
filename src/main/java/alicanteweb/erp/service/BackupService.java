package alicanteweb.erp.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Servicio para realizar backups automáticos de la base de datos
 */
@Service
@RequiredArgsConstructor
public class BackupService {
    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${backup.directory:backups}")
    private String backupDirectory;

    @Value("${backup.retention.days:30}")
    private int retentionDays;

    @Value("${backup.enabled:true}")
    private boolean backupEnabled;

    @Value("${backup.mysqldump-path:}")
    private String mysqldumpPath;

    @Value("${backup.mysql-path:}")
    private String mysqlPath;

    private final FacturacionEventoService facturacionEventoService;
    private final EmailService emailService;

    // Límites de ejecución: un mysqldump/mysql colgado no debe retener el hilo indefinidamente
    private static final Duration TIMEOUT_BACKUP = Duration.ofMinutes(15);
    private static final Duration TIMEOUT_RESTAURACION = Duration.ofMinutes(30);
    private static final Duration TIMEOUT_VERSION = Duration.ofSeconds(15);

    /**
     * Backup automático diario a las 2:00 AM
     */
    @Scheduled(cron = "${backup.cron:0 0 2 * * ?}")
    public void backupAutomatico() {
        if (!backupEnabled) {
            log.info("Backup automático deshabilitado");
            return;
        }

        log.info("🔄 Iniciando backup automático...");

        // Comprobar disponibilidad de mysqldump antes de intentar el backup
        if (!verificarDisponibilidad()) {
            log.warn("⚠️ mysqldump no está disponible; se omite el backup automático");
            return;
        }

        try {
            String archivoBackup = realizarBackup();
            log.info("✅ Backup completado exitosamente: {}", archivoBackup);
            registrarEventoBackup("BACKUP_AUTOMATICO", archivoBackup, java.util.Map.of(
                "baseDatos", resolverNombreBaseDatos()
            ));

            // Limpiar backups antiguos
            limpiarBackupsAntiguos();

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("❌ Error en backup automático", e);
            registrarEventoBackup("BACKUP_FALLIDO", "backup-automatico", java.util.Map.of(
                "error", String.valueOf(e.getMessage())
            ));
            // Un backup que falla en silencio no protege nada: avisar al administrador
            emailService.enviarAvisoAdministrador("Fallo en el backup automático",
                    "El backup automático de la base de datos ha fallado.\n\nError: " + e.getMessage()
                    + "\n\nRevisa los logs de la aplicación y el estado de mysqldump.");
        }
    }

    /**
     * Realiza un backup manual de la base de datos
     */
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
    public String realizarBackup() throws IOException, InterruptedException {
        log.info("📦 Creando backup de la base de datos...");

        // Crear directorio de backups si no existe
        Path backupPath = Paths.get(backupDirectory);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
            log.info("Directorio de backups creado: {}", backupPath);
        }

        // Generar nombre del archivo
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String dbName = resolverNombreBaseDatos();
        String nombreArchivo = String.format("backup_%s_%s.sql", dbName, timestamp);
        String rutaCompleta = backupPath.resolve(nombreArchivo).toString();

        // Crear fichero temporal con credenciales para no pasarlas en la línea de comandos
        Path tempCredFile = null;
        try {
            tempCredFile = createDefaultsFile(dbUsername, dbPassword);

            // Comando mysqldump
            List<String> comando = new ArrayList<>();

            // Detectar sistema operativo
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                comando.add("cmd.exe");
                comando.add("/c");
            }

            // --defaults-extra-file must be provided before other options
            comando.add(resolverEjecutableMysqldump());
            comando.add("--defaults-extra-file=" + tempCredFile.toString());

            comando.add("--single-transaction");
            comando.add("--routines");
            comando.add("--triggers");
            comando.add("--add-drop-table");
            comando.add(dbName);
            comando.add("--result-file=" + rutaCompleta);

            log.debug("Ejecutando comando mysqldump para DB {} usando defaults-file {}", dbName, tempCredFile);

            // Ejecutar comando
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);

            int exitCode = ejecutarProceso(pb, TIMEOUT_BACKUP, "mysqldump");

            if (exitCode == 0) {
                File backupFile = new File(rutaCompleta);
                if (backupFile.exists() && backupFile.length() > 0) {
                    long sizeMB = backupFile.length() / (1024 * 1024);
                    log.info("✅ Backup creado exitosamente: {} ({} MB)", nombreArchivo, sizeMB);
                    registrarEventoBackup("BACKUP_GENERADO", rutaCompleta, java.util.Map.of(
                        "nombreArchivo", nombreArchivo,
                        "tamanoBytes", backupFile.length(),
                        "baseDatos", dbName
                    ));
                    return rutaCompleta;
                } else {
                    throw new IOException("El archivo de backup está vacío o no se creó");
                }
            } else {
                throw new IOException("Error en mysqldump. Código de salida: " + exitCode);
            }
        } finally {
            // Borrar fichero temporal de credenciales
            if (tempCredFile != null) {
                try {
                    Files.deleteIfExists(tempCredFile);
                } catch (IOException e) {
                    log.warn("No se pudo eliminar el fichero temporal de credenciales {}: {}", tempCredFile, e.getMessage());
                }
            }
        }
    }

    /**
     * Restaura la base de datos desde un archivo de backup.
     * Acepta el nombre del fichero o una ruta, siempre dentro de backup.directory
     * (ver {@link #validarRutaBackup(String)}).
     */
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
    public void restaurarBackup(String rutaArchivo) throws IOException, InterruptedException {
        Path rutaValidada = validarRutaBackup(rutaArchivo);

        String restoringUser = usuarioActual();
        log.info("🔄 Restaurando backup desde: {} (usuario={}, hora={})", rutaValidada, restoringUser,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        File backupFile = rutaValidada.toFile();
        if (!backupFile.exists()) {
            throw new IOException("Archivo de backup no encontrado: " + rutaValidada.getFileName());
        }

        // Usar fichero temporal con credenciales y --defaults-extra-file
        Path tempCredFile = null;
        try {
            String dbName = resolverNombreBaseDatos();
            tempCredFile = createDefaultsFile(dbUsername, dbPassword);

            List<String> comando = new ArrayList<>();
            comando.add(resolverEjecutableMysql());
            comando.add("--defaults-extra-file=" + tempCredFile.toString());
            comando.add(dbName);

            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            pb.redirectInput(backupFile);

            int exitCode = ejecutarProceso(pb, TIMEOUT_RESTAURACION, "mysql (restauración)");

            if (exitCode == 0) {
                log.info("✅ Backup restaurado exitosamente");
                registrarEventoBackup("RESTAURACION_BACKUP", rutaValidada.toString(), java.util.Map.of(
                    "usuario", restoringUser,
                    "baseDatos", dbName
                ));
            } else {
                throw new IOException("Error restaurando backup. Código de salida: " + exitCode);
            }
        } finally {
            if (tempCredFile != null) {
                try { Files.deleteIfExists(tempCredFile); } catch (Exception e) { log.debug("No se pudo eliminar archivo temporal de credenciales: {}", e.getMessage()); }
            }
        }
    }

    /**
     * Lista todos los backups disponibles
     */
    public List<BackupInfo> listarBackups() throws IOException {
        List<BackupInfo> backups = new ArrayList<>();

        Path backupPath = Paths.get(backupDirectory);
        if (!Files.exists(backupPath)) {
            return backups;
        }

        try (Stream<Path> paths = Files.list(backupPath)) {
            paths.filter(path -> path.toString().endsWith(".sql"))
                 .filter(path -> path.toString().contains("backup_"))
                 .forEach(path -> {
                     try {
                         File file = path.toFile();
                         backups.add(new BackupInfo(
                             file.getName(),
                             path.toString(),
                             file.length(),
                             LocalDateTime.ofInstant(
                                 java.nio.file.Files.getLastModifiedTime(path).toInstant(),
                                 java.time.ZoneId.systemDefault()
                             )
                         ));
                     } catch (IOException e) {
                         log.warn("Error al obtener información del backup: {}", path, e);
                     }
                 });
        } catch (IOException e) {
            log.error("Error listando backups", e);
        }

        // Ordenar por fecha descendente (más recientes primero)
        backups.sort(Comparator.comparing(BackupInfo::fecha).reversed());

        return backups;
    }

    /**
     * Elimina backups más antiguos que el período de retención
     */
    public void limpiarBackupsAntiguos() {
        log.info("🧹 Limpiando backups antiguos (> {} días)...", retentionDays);

        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(retentionDays);

        try {
            List<BackupInfo> backups = listarBackups();

            int eliminados = 0;
            for (BackupInfo backup : backups) {
                if (backup.fecha().isBefore(fechaLimite)) {
                    File file = new File(backup.rutaCompleta());
                    if (file.delete()) {
                        log.info("Backup eliminado: {}", backup.nombre());
                        registrarEventoBackup("ELIMINACION_BACKUP_RETENCION", backup.rutaCompleta(), java.util.Map.of(
                            "nombreArchivo", backup.nombre(),
                            "fechaBackup", backup.fecha().toString()
                        ));
                        eliminados++;
                    }
                }
            }

            if (eliminados > 0) {
                log.info("✅ {} backups antiguos eliminados", eliminados);
            } else {
                log.info("No hay backups antiguos para eliminar");
            }

        } catch (IOException e) {
            log.error("Error limpiando backups antiguos", e);
        }
    }

    /**
     * Ejecuta un proceso externo con límite de tiempo, consumiendo su salida en un hilo
     * aparte (si nadie lee el pipe y el proceso escribe mucho, se bloquea; y si el
     * proceso se cuelga sin escribir, un readLine en este hilo bloquearía el waitFor).
     */
    private int ejecutarProceso(ProcessBuilder pb, Duration timeout, String descripcion)
            throws IOException, InterruptedException {
        Process process = pb.start();
        StringBuilder salida = new StringBuilder();
        Thread lector = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    if (salida.length() < 8192) {
                        salida.append(linea).append('\n');
                    }
                }
            } catch (IOException ignored) {
                // El stream se cierra al morir el proceso; no hay nada que hacer
            }
        }, "backup-process-output");
        lector.setDaemon(true);
        lector.start();

        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new IOException(descripcion + " excedió el tiempo máximo de "
                    + timeout.toMinutes() + " min y fue cancelado");
        }
        lector.join(5_000);
        int exitCode = process.exitValue();
        if (exitCode != 0 && salida.length() > 0) {
            log.warn("Salida de {} (exit {}): {}", descripcion, exitCode, salida.toString().trim());
        }
        return exitCode;
    }

    /**
     * Devuelve la ruta al ejecutable mysqldump: usa backup.mysqldump-path si está configurado,
     * o simplemente "mysqldump" para resolverlo desde el PATH del sistema.
     */
    private String resolverEjecutableMysqldump() {
        if (mysqldumpPath != null && !mysqldumpPath.isBlank()) {
            return mysqldumpPath;
        }
        return "mysqldump";
    }

    /**
     * Devuelve la ruta al cliente mysql (restauración): usa backup.mysql-path si está
     * configurado, o "mysql" para resolverlo desde el PATH del sistema.
     */
    private String resolverEjecutableMysql() {
        if (mysqlPath != null && !mysqlPath.isBlank()) {
            return mysqlPath;
        }
        return "mysql";
    }

    /**
     * Verifica que mysqldump esté disponible
     */
    public boolean verificarDisponibilidad() {
        try {
            List<String> comando = new ArrayList<>();

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                comando.add("cmd.exe");
                comando.add("/c");
            }
            comando.add(resolverEjecutableMysqldump());
            comando.add("--version");

            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            int exitCode = ejecutarProceso(pb, TIMEOUT_VERSION, "mysqldump --version");

            boolean disponible = exitCode == 0;

            if (disponible) {
                log.info("✅ mysqldump disponible");
            } else {
                log.warn("⚠️ mysqldump no disponible");
            }

            return disponible;

        } catch (Exception e) {
            log.warn("⚠️ No se pudo verificar mysqldump: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un backup por nombre de fichero o ruta, siempre dentro de
     * backup.directory (ver {@link #validarRutaBackup(String)}).
     *
     * @throws IllegalArgumentException si la ruta escapa del directorio de backups
     *                                  o no es un fichero backup_*.sql
     */
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
    public boolean deleteBackup(String rutaCompleta) {
        Path ruta = validarRutaBackup(rutaCompleta);
        try {
            File file = ruta.toFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("Backup eliminado: {}", ruta);
                    registrarEventoBackup("ELIMINACION_BACKUP_MANUAL", ruta.toString(), java.util.Map.of(
                        "ruta", ruta.toString(),
                        "usuario", usuarioActual()
                    ));
                } else {
                    log.warn("No se pudo eliminar backup: {}", ruta);
                }
                return deleted;
            } else {
                log.warn("Archivo de backup no existe: {}", ruta);
                return false;
            }
        } catch (Exception e) {
            log.error("Error eliminando backup {}", ruta, e);
            return false;
        }
    }

    /**
     * Defensa ante path traversal: resuelve la entrada (nombre de fichero o ruta)
     * contra backup.directory, la normaliza y rechaza cualquier resultado que
     * escape del directorio de backups. Solo se admiten ficheros backup_*.sql,
     * el patrón con el que este servicio genera los backups.
     *
     * @throws IllegalArgumentException si la ruta escapa del directorio o el
     *                                  nombre no es un fichero de backup válido
     */
    Path validarRutaBackup(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new IllegalArgumentException("Nombre de backup vacío");
        }
        Path directorio = Paths.get(backupDirectory).toAbsolutePath().normalize();
        Path candidata = Paths.get(entrada);
        Path resuelta = (candidata.isAbsolute() ? candidata : directorio.resolve(candidata))
                .toAbsolutePath().normalize();

        if (!resuelta.startsWith(directorio) || resuelta.equals(directorio)) {
            log.warn("Rechazada ruta de backup fuera del directorio permitido: {}", entrada);
            throw new IllegalArgumentException("Ruta de backup fuera del directorio permitido");
        }
        String nombre = resuelta.getFileName().toString();
        if (!nombre.startsWith("backup_") || !nombre.endsWith(".sql")
                || !resuelta.getParent().equals(directorio)) {
            log.warn("Rechazado nombre de backup no válido: {}", entrada);
            throw new IllegalArgumentException("Solo se admiten ficheros backup_*.sql del directorio de backups");
        }
        return resuelta;
    }

    /** Usuario autenticado que ejecuta la operación (para auditoría). */
    private String usuarioActual() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        // Sin contexto de seguridad (tarea programada, arranque): usuario del proceso
        return System.getProperty("user.name");
    }

    /**
     * Información de un backup
     */
    public record BackupInfo(
        String nombre,
        String rutaCompleta,
        long tamanoBytes,
        LocalDateTime fecha
    ) {
        public long getTamanoMB() {
            return tamanoBytes / (1024 * 1024);
        }

        public String getFechaFormateada() {
            return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }
    }

    /**
     * Crea un archivo temporal con formato MySQL defaults-extra-file para pasar credenciales
     * Contenido:
     * [client]
     * user=usuario
     * password=pass
     *
     * Se crea SIEMPRE en java.io.tmpdir (efímero, dentro del contenedor), nunca en el
     * directorio de backups: ese directorio se persiste en un volumen y un fichero de
     * credenciales huérfano (crash entre creación y borrado) sobreviviría al contenedor.
     */
    private Path createDefaultsFile(String user, String pass) throws IOException {
        Path tempFile = Files.createTempFile("mycnf", ".cnf");
        // Incluir host/puerto de la URL JDBC: sin ellos mysqldump intenta el socket
        // local y falla cuando MySQL corre en otro host (p.ej. contenedor "db")
        String content = "[client]\n" + "user=" + user + "\n" + "password=" + pass + "\n"
                + "host=" + resolverHost() + "\n" + "port=" + resolverPuerto() + "\n"
                + "protocol=TCP\n";
        Files.writeString(tempFile, content, java.nio.charset.StandardCharsets.UTF_8);

        // Intentar establecer permisos 600 en sistemas POSIX
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(tempFile, perms);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows o FS que no soporta POSIX: continuar pero advertir
            log.warn("No se pudieron establecer permisos POSIX en {}. Asegúrate de proteger el fichero.", tempFile);
        }

        return tempFile;
    }

    /** Host de la URL JDBC (jdbc:mysql://HOST:PUERTO/db). */
    private String resolverHost() {
        String hostPuerto = extraerHostPuerto();
        if (hostPuerto == null) {
            return "localhost";
        }
        int puntos = hostPuerto.lastIndexOf(':');
        return puntos > 0 ? hostPuerto.substring(0, puntos) : hostPuerto;
    }

    /** Puerto de la URL JDBC, 3306 por defecto. */
    private String resolverPuerto() {
        String hostPuerto = extraerHostPuerto();
        if (hostPuerto != null) {
            int puntos = hostPuerto.lastIndexOf(':');
            if (puntos > 0 && puntos < hostPuerto.length() - 1) {
                return hostPuerto.substring(puntos + 1);
            }
        }
        return "3306";
    }

    private String extraerHostPuerto() {
        if (dbUrl == null || !dbUrl.contains("//")) {
            return null;
        }
        String resto = dbUrl.substring(dbUrl.indexOf("//") + 2);
        int slash = resto.indexOf('/');
        String hostPuerto = slash > 0 ? resto.substring(0, slash) : resto;
        return hostPuerto.isBlank() ? null : hostPuerto;
    }

    private String resolverNombreBaseDatos() {
        if (dbUrl == null || dbUrl.isBlank()) {
            return "tahona";
        }
        String sinParametros = dbUrl.split("\\?", 2)[0];
        int slash = sinParametros.lastIndexOf('/');
        if (slash >= 0 && slash < sinParametros.length() - 1) {
            return sinParametros.substring(slash + 1);
        }
        return "tahona";
    }

    private void registrarEventoBackup(String tipoEvento, String referencia, java.util.Map<String, Object> metadata) {
        facturacionEventoService.registrarEvento(
            FacturacionEventoService.AMBITO_BACKUP,
            tipoEvento,
            referencia,
            metadata
        );
    }
}
