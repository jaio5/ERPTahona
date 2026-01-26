package alicanteweb.erp.config;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private final UsuarioService usuarioService;

    public DevDataInitializer(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        try {
            String adminUser = "admin";
            if (usuarioService.buscarPorUsername(adminUser).isEmpty()) {
                Usuario u = new Usuario();
                u.setUsername(adminUser);
                u.setEmail("admin@local.dev");
                u.setNombre("Administrador");
                u.setRole("ADMIN");

                // La contraseña por defecto en dev será 'admin' (cambiar en entorno real)
                usuarioService.crearUsuario(u, "admin");
                log.info("Usuario de desarrollo creado: {} / admin", adminUser);
            } else {
                log.info("Usuario 'admin' ya existe, no se creará uno nuevo");
            }
        } catch (Exception e) {
            log.error("No se pudo inicializar usuario dev: {}", e.getMessage(), e);
        }
    }
}
