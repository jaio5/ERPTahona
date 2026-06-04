package alicanteweb.erp.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    private final FacturacionEventoService facturacionEventoService;

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
            log.error("❌ Error en backup automático", e);
        }
    }

    /**
     * Realiza un backup manual de la base de datos
     */
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
            tempCredFile = createDefaultsFile(backupPath, dbUsername, dbPassword);

            // Comando mysqldump
            List<String> comando = new ArrayList<>();

            // Detectar sistema operativo
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                comando.add("cmd.exe");
                comando.add("/c");
            }

            // --defaults-extra-file must be provided before other options
            comando.add("mysqldump");
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

            Process process = pb.start();
            int exitCode = process.waitFor();

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
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Restaura la base de datos desde un archivo de backup
     */
    public void restaurarBackup(String rutaArchivo) throws IOException, InterruptedException {
        log.info("🔄 Restaurando backup desde: {}", rutaArchivo);

        String restoringUser = System.getProperty("user.name");
        log.info("🔄 Restaurando backup desde: {} (usuario={}, hora={})", rutaArchivo, restoringUser, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        File backupFile = new File(rutaArchivo);
        if (!backupFile.exists()) {
            throw new IOException("Archivo de backup no encontrado: " + rutaArchivo);
        }

        // Usar fichero temporal con credenciales y --defaults-extra-file
        Path tempCredFile = null;
        try {
            String dbName = resolverNombreBaseDatos();
            tempCredFile = createDefaultsFile(backupFile.toPath().getParent(), dbUsername, dbPassword);

            List<String> comando = new ArrayList<>();
            comando.add("mysql");
            comando.add("--defaults-extra-file=" + tempCredFile.toString());
            comando.add(dbName);

            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            pb.redirectInput(backupFile);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("✅ Backup restaurado exitosamente");
                registrarEventoBackup("RESTAURACION_BACKUP", rutaArchivo, java.util.Map.of(
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
            comando.add("mysqldump");
            comando.add("--version");

            ProcessBuilder pb = new ProcessBuilder(comando);
            Process process = pb.start();
            int exitCode = process.waitFor();

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
     * Elimina un backup por ruta completa
     */
    public boolean deleteBackup(String rutaCompleta) {
        try {
            File file = new File(rutaCompleta);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("Backup eliminado: {}", rutaCompleta);
                    registrarEventoBackup("ELIMINACION_BACKUP_MANUAL", rutaCompleta, java.util.Map.of(
                        "ruta", rutaCompleta
                    ));
                } else {
                    log.warn("No se pudo eliminar backup: {}", rutaCompleta);
                }
                return deleted;
            } else {
                log.warn("Archivo de backup no existe: {}", rutaCompleta);
                return false;
            }
        } catch (Exception e) {
            log.error("Error eliminando backup {}", rutaCompleta, e);
            return false;
        }
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
     */
    private Path createDefaultsFile(Path dir, String user, String pass) throws IOException {
        if (dir == null) dir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path tempFile = Files.createTempFile(dir, "mycnf", ".cnf");
        String content = "[client]\n" + "user=" + user + "\n" + "password=" + pass + "\n";
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
