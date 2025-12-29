package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que desbloquea automáticamente al usuario admin al iniciar la aplicación
 * TEMPORAL - Para resolver problema de usuario bloqueado
 */
@Service
@Slf4j
public class DesbloqueoAutomaticoService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DesbloqueoAutomaticoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void desbloquearAdminAlIniciar() {
        try {
            log.info("═══════════════════════════════════════════════════════");
            log.info("  DESBLOQUEO AUTOMÁTICO DE USUARIO ADMIN");
            log.info("═══════════════════════════════════════════════════════");

            var usuarioOpt = usuarioRepository.findByUsername("admin");

            if (usuarioOpt.isPresent()) {
                Usuario admin = usuarioOpt.get();

                log.info("Usuario admin encontrado - Estado actual:");
                log.info("  - Activo: {}", admin.getActivo());
                log.info("  - Bloqueado: {}", admin.getBloqueado());
                log.info("  - Intentos fallidos: {}", admin.getIntentosFallidos());

                // Desbloquear siempre
                boolean cambios = false;

                if (Boolean.FALSE.equals(admin.getActivo())) {
                    admin.setActivo(true);
                    cambios = true;
                    log.info("  ✓ Usuario activado");
                }

                if (Boolean.TRUE.equals(admin.getBloqueado())) {
                    admin.setBloqueado(false);
                    cambios = true;
                    log.info("  ✓ Usuario desbloqueado");
                }

                if (admin.getIntentosFallidos() != null && admin.getIntentosFallidos() > 0) {
                    admin.setIntentosFallidos(0);
                    cambios = true;
                    log.info("  ✓ Intentos fallidos reseteados");
                }

                // Verificar password
                String currentPassword = admin.getPassword();
                if (currentPassword == null || currentPassword.isEmpty() ||
                    (!currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2y$"))) {
                    // Password no es BCrypt, regenerar
                    String newPassword = passwordEncoder.encode("admin");
                    admin.setPassword(newPassword);
                    cambios = true;
                    log.info("  ✓ Password regenerado (no era BCrypt válido)");
                }

                if (cambios) {
                    usuarioRepository.save(admin);
                    log.info("═══════════════════════════════════════════════════════");
                    log.info("  ✅ USUARIO ADMIN DESBLOQUEADO Y LISTO");
                    log.info("═══════════════════════════════════════════════════════");
                    log.info("  Credenciales:");
                    log.info("    Usuario: admin");
                    log.info("    Contraseña: admin");
                    log.info("═══════════════════════════════════════════════════════");
                } else {
                    log.info("═══════════════════════════════════════════════════════");
                    log.info("  ✅ Usuario admin ya está desbloqueado");
                    log.info("═══════════════════════════════════════════════════════");
                }

            } else {
                log.error("✗ Usuario admin NO ENCONTRADO en la base de datos");
                log.error("  Verifica que la base de datos esté correctamente configurada");
            }

        } catch (Exception e) {
            log.error("✗ Error al desbloquear usuario admin automáticamente", e);
        }
    }
}

