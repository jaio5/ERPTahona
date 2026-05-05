package alicanteweb.erp.service;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que verifica y repara el estado del usuario admin al iniciar.
 * Solo activo en perfiles dev/test para no interferir en producción.
 */
@Service
@Slf4j
@Profile({"dev", "test", "default"})
public class DesbloqueoAutomaticoService {

    /** Contraseña por defecto del admin en dev. Configurable en application-dev.properties */
    @Value("${admin.default.password:admin}")
    private String adminDefaultPassword;

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DesbloqueoAutomaticoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void verificarAdminAlIniciar() {
        log.info("Verificando estado del usuario admin...");

        try {
            usuarioRepository.findByUsername("admin").ifPresentOrElse(
                this::repararAdminSiNecesario,
                () -> log.error("Usuario admin NO ENCONTRADO. Verifica que la BD esté correctamente inicializada.")
            );
        } catch (Exception e) {
            log.error("Error al verificar usuario admin", e);
        }
    }

    /**
     * Comprueba si una cadena es un hash de contraseña reconocido:
     * <ul>
     *   <li>BCrypt: empieza por {@code $2a$}, {@code $2b$} o {@code $2y$}</li>
     *   <li>PBKDF2 con prefijo Spring: empieza por {@code {pbkdf2}}</li>
     *   <li>PBKDF2 personalizado (CustomPbkdf2PasswordEncoder): formato base64$base64</li>
     * </ul>
     */
    private boolean esHashValido(String password) {
        if (password == null || password.isEmpty()) return false;
        // BCrypt
        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) return true;
        // DelegatingPasswordEncoder con prefijo: {pbkdf2}, {bcrypt}, etc.
        if (password.startsWith("{") && password.contains("}")) return true;
        // CustomPbkdf2PasswordEncoder: base64$base64 (salt$hash sin prefijo)
        if (password.contains("$")) {
            String[] partes = password.split("\\$", 2);
            if (partes.length == 2 && !partes[0].isEmpty() && !partes[1].isEmpty()) {
                try {
                    java.util.Base64.getDecoder().decode(partes[0]);
                    java.util.Base64.getDecoder().decode(partes[1]);
                    return true;
                } catch (IllegalArgumentException ignored) { /* no es Base64 válido */ }
            }
        }
        return false;
    }

    private void repararAdminSiNecesario(Usuario admin) {
        boolean cambios = false;

        if (Boolean.FALSE.equals(admin.getActivo())) {
            admin.setActivo(true);
            cambios = true;
            log.info("Admin: usuario activado");
        }

        if (Boolean.TRUE.equals(admin.getBloqueado())) {
            admin.setBloqueado(false);
            admin.setFechaBloqueo(null);
            cambios = true;
            log.info("Admin: usuario desbloqueado");
        }

        if (admin.getIntentosFallidos() != null && admin.getIntentosFallidos() > 0) {
            admin.setIntentosFallidos(0);
            cambios = true;
            log.info("Admin: intentos fallidos reseteados");
        }

        String password = admin.getPassword();
        boolean passwordInvalido = password == null || password.isEmpty() || !esHashValido(password);

        if (passwordInvalido) {
            admin.setPassword(passwordEncoder.encode(adminDefaultPassword));
            cambios = true;
            log.warn("Admin: password no era un hash reconocido — regenerado con contraseña por defecto");
        }

        if (cambios) {
            usuarioRepository.save(admin);
            log.info("Admin reparado y listo (usuario: admin)");
        } else {
            log.debug("Admin ya está en estado correcto, sin cambios");
        }
    }
}
