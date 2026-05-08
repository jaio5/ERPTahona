package alicanteweb.erp.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GitUpdateService {

    private final boolean enabled;
    private final String remote;
    private final String branch;
    private final Duration timeout;
    private final File workDir;

    public GitUpdateService(@Value("${app.update.enabled:true}") boolean enabled,
                            @Value("${app.update.remote:origin}") String remote,
                            @Value("${app.update.branch:produccion}") String branch,
                            @Value("${app.update.timeout-seconds:60}") int timeoutSeconds,
                            @Value("${app.update.workdir:}") String configuredWorkDir) {
        this.enabled = enabled;
        this.remote = remote;
        this.branch = branch;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.workDir = configuredWorkDir == null || configuredWorkDir.isBlank()
            ? new File(System.getProperty("user.dir"))
            : new File(configuredWorkDir);
    }

    public UpdateStatus checkForUpdates() {
        if (!enabled) {
            return UpdateStatus.disabled();
        }
        if (!isGitRepository()) {
            return UpdateStatus.unavailable("La aplicación no se está ejecutando desde un repositorio Git.");
        }

        String currentBranch = runGit("branch", "--show-current").stdout().trim();
        if (!branch.equals(currentBranch)) {
            return UpdateStatus.unavailable("La aplicación está en la rama '" + currentBranch + "', no en '" + branch + "'.");
        }

        runGit("fetch", "--quiet", remote, branch);

        String local = runGit("rev-parse", "HEAD").stdout().trim();
        String remoteRef = remote + "/" + branch;
        String remoteHead = runGit("rev-parse", remoteRef).stdout().trim();
        if (local.equals(remoteHead)) {
            return UpdateStatus.upToDate(currentBranch);
        }

        String behind = runGit("rev-list", "--count", "HEAD.." + remoteRef).stdout().trim();
        int commitsBehind = parseInt(behind);
        return commitsBehind > 0
            ? UpdateStatus.available(currentBranch, commitsBehind)
            : UpdateStatus.unavailable("La rama local y " + remoteRef + " han divergido. Actualiza manualmente.");
    }

    public UpdateResult update() {
        UpdateStatus status = checkForUpdates();
        if (!status.available()) {
            return UpdateResult.notUpdated(status.message());
        }

        CommandResult result = runGit("pull", "--ff-only", remote, branch);
        return UpdateResult.updated(result.stdout().isBlank() ? "Actualización aplicada." : result.stdout());
    }

    private boolean isGitRepository() {
        try {
            return runGit("rev-parse", "--is-inside-work-tree").stdout().trim().equals("true");
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private CommandResult runGit(String... args) {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(args));

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workDir);
        builder.redirectErrorStream(false);

        try {
            Process process = builder.start();
            boolean finished = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Tiempo agotado ejecutando: " + String.join(" ", command));
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Error ejecutando '" + String.join(" ", command) + "': "
                    + (stderr.isBlank() ? stdout : stderr).trim());
            }
            return new CommandResult(stdout, stderr);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo ejecutar Git. Comprueba que Git esté instalado y en el PATH.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Actualización interrumpida.", e);
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private record CommandResult(String stdout, String stderr) {
    }

    @Getter
    public static class UpdateStatus {
        private final boolean available;
        private final boolean enabled;
        private final String branch;
        private final int commitsBehind;
        private final String message;

        private UpdateStatus(boolean available, boolean enabled, String branch, int commitsBehind, String message) {
            this.available = available;
            this.enabled = enabled;
            this.branch = branch;
            this.commitsBehind = commitsBehind;
            this.message = message;
        }

        static UpdateStatus disabled() {
            return new UpdateStatus(false, false, null, 0, "Actualizaciones deshabilitadas.");
        }

        static UpdateStatus unavailable(String message) {
            return new UpdateStatus(false, true, null, 0, message);
        }

        static UpdateStatus upToDate(String branch) {
            return new UpdateStatus(false, true, branch, 0, "La aplicación ya está actualizada.");
        }

        static UpdateStatus available(String branch, int commitsBehind) {
            return new UpdateStatus(true, true, branch, commitsBehind,
                "Hay " + commitsBehind + " actualización(es) disponible(s).");
        }
    }

    @Getter
    public static class UpdateResult {
        private final boolean updated;
        private final String message;

        private UpdateResult(boolean updated, String message) {
            this.updated = updated;
            this.message = message;
        }

        static UpdateResult updated(String message) {
            return new UpdateResult(true, message);
        }

        static UpdateResult notUpdated(String message) {
            return new UpdateResult(false, message);
        }
    }
}
