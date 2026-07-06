package alicanteweb.erp.config;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import alicanteweb.erp.repository.RolRepository;
import alicanteweb.erp.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("dev")
public class DevDataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmpresaConfigRepository empresaConfigRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${admin.default.password:admin}")
    private String adminDefaultPassword;

    public DevDataInitializer(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder,
                              EmpresaConfigRepository empresaConfigRepository,
                              JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.empresaConfigRepository = empresaConfigRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // Reparar columna xml_generado si Flyway no la ha creado como LONGTEXT
        try {
            jdbcTemplate.execute("ALTER TABLE verifactu_evidence MODIFY COLUMN xml_generado LONGTEXT");
            log.info("Dev schema fix: xml_generado → LONGTEXT");
        } catch (Exception e) {
            log.debug("Dev schema fix skipped (xml_generado ya es LONGTEXT o tabla no existe): {}", e.getMessage());
        }
        try {
            asegurarAdminDev();
        } catch (Exception e) {
            log.error("No se pudo inicializar usuario dev: {}", e.getMessage(), e);
        }

        // Inicializar configuración de empresa mínima para dev
        try {
            if (!empresaConfigRepository.existsActive()) {
                EmpresaConfig ec = new EmpresaConfig();
                ec.setActivo(true);
                ec.setNombreEmpresa("GRUPO BABO - DEV");
                ec.setNombreComercial("GRUPO BABO");
                ec.setCif("00000000A");
                ec.setCiudad("Ciudad");
                ec.setDireccion("C/ Ejemplo 1");
                ec.setFechaCreacion(LocalDateTime.now());
                // Configuración Verifactu por defecto en dev: deshabilitado
                ec.setVerifactuHabilitado(false);
                ec.setVerifactuNifEmisor("NIF000000A");

                empresaConfigRepository.save(ec);
                log.info("Empresa de desarrollo creada: {}", ec.getNombreEmpresa());
            } else {
                log.info("Empresa ya configurada, no se creará una nueva");
            }
        } catch (Exception e) {
            log.error("No se pudo inicializar empresa dev: {}", e.getMessage(), e);
        }
    }

    /**
     * Garantiza un usuario admin usable en desarrollo.
     *
     * <p>Las migraciones V8/V9 endurecen el admin de bootstrap sembrado por V1:
     * lo dejan con un hash placeholder, la cuenta deshabilitada y cambio de
     * contraseña forzado. Ese diseño está pensado para que {@code ProdDataInitializer}
     * fije la contraseña real en producción. En dev no existe ese paso equivalente,
     * así que aquí se restablece el admin a la contraseña de desarrollo
     * ({@code admin.default.password}, por defecto {@code "admin"}) para que un
     * esquema recién regenerado sea accesible sin tener que adivinar el hash de
     * arranque. Si el admin ya está operativo (habilitado y sin cambio forzado) no
     * se toca, para no pisar una contraseña que el desarrollador haya cambiado.
     */
    private void asegurarAdminDev() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@local.dev");
            admin.setNombre("Administrador");
            admin.setRole("ADMIN");
            admin.setFechaCreacion(LocalDateTime.now());
            rolRepository.findByNombre("ADMIN").ifPresent(admin::setRol);
        } else if (adminUsable(admin)) {
            log.info("Usuario 'admin' ya operativo en dev; no se modifica");
            return;
        }
        admin.setPassword(passwordEncoder.encode(adminDefaultPassword));
        admin.setEnabled(true);
        admin.setBloqueado(false);
        admin.setIntentosFallidos(0);
        admin.setRequiereCambioPassword(false);
        usuarioRepository.save(admin);
        log.info("Admin de desarrollo listo: usuario 'admin' con la contraseña de admin.default.password");
    }

    private boolean adminUsable(Usuario admin) {
        return Boolean.TRUE.equals(admin.getEnabled())
                && !Boolean.TRUE.equals(admin.getRequiereCambioPassword());
    }
}
