package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitUpdateServiceTest {

    @Test
    void disabled_noEjecutaGit() {
        FakeGitRunner runner = new FakeGitRunner();
        GitUpdateService service = new GitUpdateService(false, "origin", "produccion", 5, "", runner);

        GitUpdateService.UpdateStatus status = service.checkForUpdates();

        assertFalse(status.isEnabled());
        assertFalse(status.isAvailable());
        assertTrue(runner.commands.isEmpty());
    }

    @Test
    void checkForUpdates_detectaCommitsRemotos() {
        FakeGitRunner runner = new FakeGitRunner()
            .on("rev-parse --is-inside-work-tree", "true\n")
            .on("branch --show-current", "produccion\n")
            .on("fetch --quiet origin produccion:refs/remotes/origin/produccion", "")
            .on("rev-parse HEAD", "local\n")
            .on("rev-parse origin/produccion", "remote\n")
            .on("rev-list --count HEAD..origin/produccion", "2\n");
        GitUpdateService service = new GitUpdateService(true, "origin", "produccion", 5, "", runner);

        GitUpdateService.UpdateStatus status = service.checkForUpdates();

        assertTrue(status.isAvailable());
        assertEquals(2, status.getCommitsBehind());
        assertEquals("produccion", status.getBranch());
    }

    @Test
    void checkForUpdates_noDisponibleSiNoEstaEnRamaConfigurada() {
        FakeGitRunner runner = new FakeGitRunner()
            .on("rev-parse --is-inside-work-tree", "true\n")
            .on("branch --show-current", "desarrollo\n");
        GitUpdateService service = new GitUpdateService(true, "origin", "produccion", 5, "", runner);

        GitUpdateService.UpdateStatus status = service.checkForUpdates();

        assertFalse(status.isAvailable());
        assertTrue(status.getMessage().contains("desarrollo"));
        assertFalse(runner.commands.contains("fetch --quiet origin produccion:refs/remotes/origin/produccion"));
    }

    @Test
    void update_hacePullFastForwardSiHayActualizacionYLimpio() {
        FakeGitRunner runner = new FakeGitRunner()
            .on("rev-parse --is-inside-work-tree", "true\n")
            .on("branch --show-current", "produccion\n")
            .on("fetch --quiet origin produccion:refs/remotes/origin/produccion", "")
            .on("rev-parse HEAD", "local\n")
            .on("rev-parse origin/produccion", "remote\n")
            .on("rev-list --count HEAD..origin/produccion", "1\n")
            .on("status --porcelain", "")
            .on("pull --ff-only origin produccion", "Fast-forward\n");
        GitUpdateService service = new GitUpdateService(true, "origin", "produccion", 5, "", runner);

        GitUpdateService.UpdateResult result = service.update();

        assertTrue(result.isUpdated());
        assertTrue(runner.commands.contains("pull --ff-only origin produccion"));
    }

    @Test
    void update_noHacePullSiHayCambiosLocales() {
        FakeGitRunner runner = new FakeGitRunner()
            .on("rev-parse --is-inside-work-tree", "true\n")
            .on("branch --show-current", "produccion\n")
            .on("fetch --quiet origin produccion:refs/remotes/origin/produccion", "")
            .on("rev-parse HEAD", "local\n")
            .on("rev-parse origin/produccion", "remote\n")
            .on("rev-list --count HEAD..origin/produccion", "1\n")
            .on("status --porcelain", " M file.txt\n");
        GitUpdateService service = new GitUpdateService(true, "origin", "produccion", 5, "", runner);

        GitUpdateService.UpdateResult result = service.update();

        assertFalse(result.isUpdated());
        assertTrue(result.getMessage().contains("cambios locales"));
        assertFalse(runner.commands.contains("pull --ff-only origin produccion"));
    }

    @Test
    void update_creaBackupAntesDelPullSiEstaConfigurado() throws Exception {
        FakeGitRunner runner = new FakeGitRunner()
            .on("rev-parse --is-inside-work-tree", "true\n")
            .on("branch --show-current", "produccion\n")
            .on("fetch --quiet origin produccion:refs/remotes/origin/produccion", "")
            .on("rev-parse HEAD", "local\n")
            .on("rev-parse origin/produccion", "remote\n")
            .on("rev-list --count HEAD..origin/produccion", "1\n")
            .on("status --porcelain", "")
            .on("pull --ff-only origin produccion", "Fast-forward\n");
        BackupService backupService = mock(BackupService.class);
        when(backupService.verificarDisponibilidad()).thenReturn(true);
        when(backupService.realizarBackup()).thenReturn("backups/backup_previo.sql");
        GitUpdateService service = new GitUpdateService(true, "origin", "produccion", 5, "",
            true, backupService, runner);

        GitUpdateService.UpdateResult result = service.update();

        assertTrue(result.isUpdated());
        assertTrue(result.getMessage().contains("backup_previo.sql"));
        verify(backupService).realizarBackup();
        assertTrue(runner.commands.contains("pull --ff-only origin produccion"));
    }

    @Test
    void update_noHacePullSiNoPuedeCrearBackupPrevio() {
        FakeGitRunner runner = new FakeGitRunner()
            .on("rev-parse --is-inside-work-tree", "true\n")
            .on("branch --show-current", "produccion\n")
            .on("fetch --quiet origin produccion:refs/remotes/origin/produccion", "")
            .on("rev-parse HEAD", "local\n")
            .on("rev-parse origin/produccion", "remote\n")
            .on("rev-list --count HEAD..origin/produccion", "1\n")
            .on("status --porcelain", "");
        BackupService backupService = mock(BackupService.class);
        when(backupService.verificarDisponibilidad()).thenReturn(false);
        GitUpdateService service = new GitUpdateService(true, "origin", "produccion", 5, "",
            true, backupService, runner);

        GitUpdateService.UpdateResult result = service.update();

        assertFalse(result.isUpdated());
        assertTrue(result.getMessage().contains("backup previo"));
        assertFalse(runner.commands.contains("pull --ff-only origin produccion"));
    }

    private static class FakeGitRunner implements GitUpdateService.GitCommandRunner {
        private final java.util.Map<String, String> outputs = new java.util.HashMap<>();
        private final List<String> commands = new ArrayList<>();

        FakeGitRunner on(String command, String output) {
            outputs.put(command, output);
            return this;
        }

        @Override
        public GitUpdateService.CommandResult run(File workDir, Duration timeout, String... args) {
            String command = String.join(" ", Arrays.asList(args));
            commands.add(command);
            if (!outputs.containsKey(command)) {
                throw new IllegalStateException("Comando no simulado: " + command);
            }
            return new GitUpdateService.CommandResult(outputs.get(command), "");
        }
    }
}
