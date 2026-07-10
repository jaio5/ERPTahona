package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** El soporte de @Scheduled se habilita una sola vez en {@link alicanteweb.erp.config.SchedulingConfig}. */
@Component
public class AccountUnlockScheduler {

    // Tiempos incrementales: 5min, 15min, 30min, 1h, 2h, 4h, 8h, 24h
    private static final long[] UNLOCK_MINUTES = {5, 15, 30, 60, 120, 240, 480, 1440};

    private final UsuarioRepository usuarioRepository;

    public AccountUnlockScheduler(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void unlockAccounts() {
        List<Usuario> bloqueados = usuarioRepository.findByBloqueadoTrue();
        LocalDateTime now = LocalDateTime.now();
        for (Usuario u : bloqueados) {
            if (u.getFechaBloqueo() == null) continue;
            int idx = Math.max(0, Math.min(
                (u.getContadorBloqueos() != null ? u.getContadorBloqueos() : 1) - 1,
                UNLOCK_MINUTES.length - 1
            ));
            long unlockMinutes = UNLOCK_MINUTES[idx];
            if (u.getFechaBloqueo().plusMinutes(unlockMinutes).isBefore(now)) {
                u.setBloqueado(false);
                u.setIntentosFallidos(0);
                usuarioRepository.save(u);
            }
        }
    }
}
