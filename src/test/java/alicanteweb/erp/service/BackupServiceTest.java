package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupServiceTest {

    @Mock
    FacturacionEventoService facturacionEventoService;

    @Mock
    EmailService emailService;

    @InjectMocks
    BackupService service;

    @Test
    void listarBackups_retornaVacio_cuandoDirectorioNoExiste() throws IOException {
        ReflectionTestUtils.setField(service, "backupDirectory", "/ruta/que/no/existe_12345");

        List<BackupService.BackupInfo> result = service.listarBackups();

        assertTrue(result.isEmpty());
    }

    @Test
    void listarBackups_retornaArchivosSQL_delDirectorio(@TempDir Path tempDir) throws IOException {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        Files.writeString(tempDir.resolve("backup_tahona_20260101_020000.sql"), "-- SQL content");
        Files.writeString(tempDir.resolve("backup_tahona_20260102_020000.sql"), "-- SQL content 2");
        Files.writeString(tempDir.resolve("otro_archivo.txt"), "no es backup");

        List<BackupService.BackupInfo> result = service.listarBackups();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.nombre().endsWith(".sql")));
    }

    @Test
    void listarBackups_ordenaDescendentePorFecha(@TempDir Path tempDir) throws IOException, InterruptedException {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        Path old = tempDir.resolve("backup_tahona_20260101_020000.sql");
        Path recent = tempDir.resolve("backup_tahona_20260619_020000.sql");
        Files.writeString(old, "-- old");
        Thread.sleep(50);
        Files.writeString(recent, "-- recent");

        List<BackupService.BackupInfo> result = service.listarBackups();

        assertEquals(2, result.size());
        assertTrue(result.get(0).fecha().isAfter(result.get(1).fecha())
                || result.get(0).fecha().isEqual(result.get(1).fecha()));
    }

    @Test
    void backupAutomatico_noHaceNada_cuandoEstaDeshabilitado() {
        ReflectionTestUtils.setField(service, "backupEnabled", false);

        assertDoesNotThrow(() -> service.backupAutomatico());
        verifyNoInteractions(facturacionEventoService);
    }

    @Test
    void deleteBackup_retornaFalso_cuandoArchivoNoExiste(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        assertFalse(service.deleteBackup("backup_inexistente.sql"));
    }

    @Test
    void deleteBackup_eliminaArchivoExistente(@TempDir Path tempDir) throws IOException {
        Path backup = tempDir.resolve("backup_tahona_20260619_020000.sql");
        Files.writeString(backup, "-- SQL");
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        boolean result = service.deleteBackup(backup.toString());

        assertTrue(result);
        assertFalse(Files.exists(backup));
    }

    // ── Guard anti-path-traversal (validarRutaBackup) ────────────────────────

    @Test
    void validarRutaBackup_rechazaRutaConTraversal(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        assertThrows(IllegalArgumentException.class,
                () -> service.validarRutaBackup("../../etc/backup_malicioso.sql"));
        assertThrows(IllegalArgumentException.class,
                () -> service.validarRutaBackup("..\\..\\Windows\\backup_malicioso.sql"));
        assertThrows(IllegalArgumentException.class,
                () -> service.validarRutaBackup("subdir/../../backup_fuera.sql"));
    }

    @Test
    void validarRutaBackup_rechazaRutaAbsolutaFueraDelDirectorio(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.resolve("backups").toString());

        Path fuera = tempDir.resolve("backup_fuera.sql");
        assertThrows(IllegalArgumentException.class,
                () -> service.validarRutaBackup(fuera.toString()));
    }

    @Test
    void validarRutaBackup_rechazaNombresQueNoSonBackupSql(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        assertThrows(IllegalArgumentException.class, () -> service.validarRutaBackup("credenciales.cnf"));
        assertThrows(IllegalArgumentException.class, () -> service.validarRutaBackup("cualquiera.sql"));
        assertThrows(IllegalArgumentException.class, () -> service.validarRutaBackup("backup_.sql.sh"));
        assertThrows(IllegalArgumentException.class, () -> service.validarRutaBackup(""));
        assertThrows(IllegalArgumentException.class, () -> service.validarRutaBackup(null));
    }

    @Test
    void validarRutaBackup_aceptaNombreValido_yRutaAbsolutaDentroDelDirectorio(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        Path porNombre = service.validarRutaBackup("backup_tahona_20260704_020000.sql");
        assertEquals(tempDir.toAbsolutePath().normalize(), porNombre.getParent());

        Path absolutaDentro = tempDir.resolve("backup_tahona_20260704_020000.sql");
        assertEquals(porNombre, service.validarRutaBackup(absolutaDentro.toString()));
    }

    @Test
    void restaurarBackup_rechazaTraversal_sinTocarElSistema(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());

        assertThrows(IllegalArgumentException.class,
                () -> service.restaurarBackup("../backup_fuera.sql"));
        verifyNoInteractions(facturacionEventoService);
    }

    @Test
    void deleteBackup_rechazaTraversal(@TempDir Path tempDir) throws IOException {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());
        // Fichero real fuera del directorio de backups: el guard debe impedir borrarlo
        Path victima = tempDir.getParent().resolve("backup_victima.sql");
        Files.writeString(victima, "-- fuera del directorio");
        try {
            assertThrows(IllegalArgumentException.class,
                    () -> service.deleteBackup("../" + victima.getFileName()));
            assertTrue(Files.exists(victima));
        } finally {
            Files.deleteIfExists(victima);
        }
    }

    @Test
    void backupInfo_getTamanoMB_calculaCorrectamente() {
        BackupService.BackupInfo info = new BackupService.BackupInfo(
                "backup.sql", "/backups/backup.sql", 5 * 1024 * 1024L, LocalDateTime.now());

        assertEquals(5.0, info.getTamanoMB(), 0.001);
    }

    @Test
    void backupInfo_getTamanoMB_pequeno_muestraDecimales() {
        BackupService.BackupInfo info = new BackupService.BackupInfo(
                "backup.sql", "/backups/backup.sql", 512 * 1024L, LocalDateTime.now());
        // 512 KB = 0.5 MB (antes daba 0 por división entera)
        assertEquals(0.5, info.getTamanoMB(), 0.001);
    }

    @Test
    void backupInfo_getFechaFormateada_formatoCorrecto() {
        LocalDateTime fecha = LocalDateTime.of(2026, 6, 19, 2, 0, 0);
        BackupService.BackupInfo info = new BackupService.BackupInfo(
                "backup.sql", "/backups/backup.sql", 1024L, fecha);

        assertEquals("19/06/2026 02:00:00", info.getFechaFormateada());
    }

    @Test
    void limpiarBackupsAntiguos_noEliminaBackupsRecientes(@TempDir Path tempDir) throws IOException {
        ReflectionTestUtils.setField(service, "backupDirectory", tempDir.toString());
        ReflectionTestUtils.setField(service, "retentionDays", 30);

        Files.writeString(tempDir.resolve("backup_tahona_20260619_020000.sql"), "-- recent");

        assertDoesNotThrow(() -> service.limpiarBackupsAntiguos());
        assertEquals(1, service.listarBackups().size());
    }
}
