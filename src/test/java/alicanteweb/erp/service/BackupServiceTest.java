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
    void deleteBackup_retornaFalso_cuandoArchivoNoExiste() {
        assertFalse(service.deleteBackup("/ruta/inexistente/backup.sql"));
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

    @Test
    void backupInfo_getTamanoMB_calculaCorrectamente() {
        BackupService.BackupInfo info = new BackupService.BackupInfo(
                "backup.sql", "/backups/backup.sql", 5 * 1024 * 1024L, LocalDateTime.now());

        assertEquals(5L, info.getTamanoMB());
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
