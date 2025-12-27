package alicanteweb.erp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de backup automático de base de datos
 * FASE 3: Seguridad Avanzada
 */
@Service
@Slf4j
public class BackupService {

    @Value("${backup.path:./backups}")
    private String backupPath;

    @Value("${backup.retention.days:30}")
    private int retentionDays;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final AuditoriaService auditoriaService;

    public BackupService(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /**
     * Backup automático diario a las 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void backupDiarioAutomatico() {
        log.info("Iniciando backup automático diario");
        try {
            realizarBackup("automatico_diario");
        } catch (Exception e) {
            log.error("Error en backup automático diario", e);
            auditoriaService.registrarError(null, "Backup", null,
                    "Error en backup automático: " + e.getMessage());
        }
    }

    /**
     * Backup manual
     */
    public String realizarBackupManual() throws IOException {
        log.info("Iniciando backup manual");
        String nombreArchivo = realizarBackup("manual");
        auditoriaService.registrarAccion(null, "BACKUP_MANUAL", "Backup", null,
                "Backup manual realizado: " + nombreArchivo);
        return nombreArchivo;
    }

    /**
     * Realiza el backup de la base de datos
     */
    private String realizarBackup(String tipo) throws IOException {
        // Crear directorio de backups si no existe
        Path backupDir = Paths.get(backupPath);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
            log.info("Directorio de backups creado: {}", backupPath);
        }

        // Extraer nombre de la base de datos de la URL
        String dbName = extractDatabaseName(datasourceUrl);

        // Nombre del archivo de backup
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String nombreArchivo = String.format("%s_%s_%s.sql", dbName, tipo, timestamp);
        Path archivoBackup = backupDir.resolve(nombreArchivo);

        // Comando mysqldump
        List<String> comando = new ArrayList<>();
        comando.add("mysqldump");
        comando.add("-u" + dbUsername);
        comando.add("-p" + dbPassword);
        comando.add("--single-transaction");
        comando.add("--routines");
        comando.add("--triggers");
        comando.add("--add-drop-table");
        comando.add(dbName);

        // Ejecutar mysqldump
        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectOutput(archivoBackup.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                long size = Files.size(archivoBackup);
                log.info("Backup completado exitosamente: {} ({} bytes)", nombreArchivo, size);

                // Comprimir el backup
                comprimirBackup(archivoBackup);

                // Limpiar backups antiguos
                limpiarBackupsAntiguos();

                return nombreArchivo;
            } else {
                throw new IOException("mysqldump falló con código: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Backup interrumpido", e);
        }
    }

    /**
     * Comprime el archivo de backup
     */
    private void comprimirBackup(Path archivo) throws IOException {
        Path archivoGz = Paths.get(archivo.toString() + ".gz");

        ProcessBuilder pb = new ProcessBuilder("gzip", "-f", archivo.toString());
        Process process = pb.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Backup comprimido: {}", archivoGz.getFileName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Compresión interrumpida", e);
        }
    }

    /**
     * Limpia backups antiguos según política de retención
     */
    private void limpiarBackupsAntiguos() throws IOException {
        Path backupDir = Paths.get(backupPath);
        long limiteMs = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);

        List<Path> backupsAntiguos = Files.list(backupDir)
                .filter(p -> p.toString().endsWith(".sql.gz") || p.toString().endsWith(".sql"))
                .filter(p -> {
                    try {
                        return Files.getLastModifiedTime(p).toMillis() < limiteMs;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        for (Path backup : backupsAntiguos) {
            Files.delete(backup);
            log.info("Backup antiguo eliminado: {}", backup.getFileName());
        }

        if (!backupsAntiguos.isEmpty()) {
            log.info("Limpiados {} backups antiguos (retención: {} días)",
                    backupsAntiguos.size(), retentionDays);
        }
    }

    /**
     * Restaura un backup específico
     */
    public void restaurarBackup(String nombreArchivo) throws IOException {
        Path archivoBackup = Paths.get(backupPath, nombreArchivo);

        if (!Files.exists(archivoBackup)) {
            throw new IOException("Archivo de backup no encontrado: " + nombreArchivo);
        }

        String dbName = extractDatabaseName(datasourceUrl);

        // Si está comprimido, descomprimir primero
        if (nombreArchivo.endsWith(".gz")) {
            ProcessBuilder pb = new ProcessBuilder("gunzip", "-c", archivoBackup.toString());
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process gunzip = pb.start();

            // Restaurar desde el pipe
            ProcessBuilder mysqlPb = new ProcessBuilder("mysql", "-u" + dbUsername,
                    "-p" + dbPassword, dbName);
            mysqlPb.redirectInput(ProcessBuilder.Redirect.PIPE);
            Process mysql = mysqlPb.start();

            // Conectar pipes
            gunzip.getInputStream().transferTo(mysql.getOutputStream());
            mysql.getOutputStream().close();

            try {
                int exitCode = mysql.waitFor();
                if (exitCode == 0) {
                    log.info("Backup restaurado exitosamente: {}", nombreArchivo);
                    auditoriaService.registrarAccion(null, "RESTAURAR_BACKUP", "Backup",
                            nombreArchivo, "Backup restaurado exitosamente");
                } else {
                    throw new IOException("Restauración falló con código: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Restauración interrumpida", e);
            }
        } else {
            // Restaurar backup sin comprimir
            ProcessBuilder pb = new ProcessBuilder("mysql", "-u" + dbUsername,
                    "-p" + dbPassword, dbName);
            pb.redirectInput(archivoBackup.toFile());

            Process process = pb.start();
            try {
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("Backup restaurado exitosamente: {}", nombreArchivo);
                    auditoriaService.registrarAccion(null, "RESTAURAR_BACKUP", "Backup",
                            nombreArchivo, "Backup restaurado exitosamente");
                } else {
                    throw new IOException("Restauración falló con código: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Restauración interrumpida", e);
            }
        }
    }

    /**
     * Lista todos los backups disponibles
     */
    public List<BackupInfo> listarBackups() throws IOException {
        Path backupDir = Paths.get(backupPath);

        if (!Files.exists(backupDir)) {
            return new ArrayList<>();
        }

        return Files.list(backupDir)
                .filter(p -> p.toString().endsWith(".sql.gz") || p.toString().endsWith(".sql"))
                .map(p -> {
                    try {
                        return new BackupInfo(
                                p.getFileName().toString(),
                                Files.size(p),
                                Files.getLastModifiedTime(p).toMillis()
                        );
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(info -> info != null)
                .sorted((a, b) -> Long.compare(b.getFechaMs(), a.getFechaMs()))
                .collect(Collectors.toList());
    }

    /**
     * Extrae el nombre de la base de datos de la URL
     */
    private String extractDatabaseName(String url) {
        // jdbc:mysql://localhost:3306/erp_alicante?...
        int lastSlash = url.lastIndexOf('/');
        int questionMark = url.indexOf('?', lastSlash);

        if (questionMark > 0) {
            return url.substring(lastSlash + 1, questionMark);
        } else {
            return url.substring(lastSlash + 1);
        }
    }

    /**
     * Clase interna para información de backup
     */
    public static class BackupInfo {
        private String nombre;
        private long tamanoBytes;
        private long fechaMs;

        public BackupInfo(String nombre, long tamanoBytes, long fechaMs) {
            this.nombre = nombre;
            this.tamanoBytes = tamanoBytes;
            this.fechaMs = fechaMs;
        }

        public String getNombre() { return nombre; }
        public long getTamanoBytes() { return tamanoBytes; }
        public long getFechaMs() { return fechaMs; }

        public String getTamanoFormateado() {
            if (tamanoBytes < 1024) return tamanoBytes + " B";
            if (tamanoBytes < 1024 * 1024) return (tamanoBytes / 1024) + " KB";
            return (tamanoBytes / (1024 * 1024)) + " MB";
        }
    }
}

