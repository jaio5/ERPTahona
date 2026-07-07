package alicanteweb.erp.config;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RolRepository;
import alicanteweb.erp.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Profile({"prod", "production"})
public class ProdDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(ProdDataInitializer.class);

    /** Hash placeholder que V9__disable_insecure_bootstrap_admin.sql asigna al admin de bootstrap. */
    private static final String PLACEHOLDER_BOOTSTRAP_HASH =
            "{bcrypt}$2a$10$7EqJtq98hPqEX7fNZaFWoOhiYr0YHG6MlCezr2XbZYi1s4r8Wz.VS";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.password}")
    private String adminDefaultPassword;

    public ProdDataInitializer(UsuarioRepository usuarioRepository,
                               RolRepository rolRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void asegurarAdminInicial() {
        Usuario admin = usuarioRepository.findByUsername("admin")
                .orElseGet(this::crearAdmin);

        if (esPasswordInicialInsegura(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(adminDefaultPassword));
            admin.setRequiereCambioPassword(true);
            admin.setEnabled(true);
            admin.setBloqueado(false);
            admin.setIntentosFallidos(0);
            usuarioRepository.save(admin);
            log.warn("Usuario admin inicial protegido. Debe cambiarse la password en el primer acceso.");
        }
    }

    private Usuario crearAdmin() {
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setEmail("admin@local");
        admin.setNombre("Administrador");
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        admin.setBloqueado(false);
        admin.setIntentosFallidos(0);
        admin.setFechaCreacion(LocalDateTime.now());
        admin.setRequiereCambioPassword(true);
        rolRepository.findByNombre("ADMIN").ifPresent(admin::setRol);
        return admin;
    }

    private boolean esPasswordInicialInsegura(String password) {
        if (password == null || password.isBlank()) {
            return true;
        }
        if ("admin".equals(password)) {
            return true;
        }
        if (PLACEHOLDER_BOOTSTRAP_HASH.equals(password)) {
            return true;
        }
        return !password.startsWith("{") && !password.startsWith("$2");
    }
}
