package alicanteweb.erp.config;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import alicanteweb.erp.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Profile("dev")
public class DevDataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final UsuarioService usuarioService;
    private final EmpresaConfigRepository empresaConfigRepository;
    private final JdbcTemplate jdbcTemplate;

    public DevDataInitializer(UsuarioService usuarioService, EmpresaConfigRepository empresaConfigRepository, JdbcTemplate jdbcTemplate) {
        this.usuarioService = usuarioService;
        this.empresaConfigRepository = empresaConfigRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        if (!initialized.compareAndSet(false, true)) return;

        // Reparar columna xml_generado si Flyway no la ha creado como LONGTEXT
        try {
            jdbcTemplate.execute("ALTER TABLE verifactu_evidence MODIFY COLUMN xml_generado LONGTEXT");
            log.info("Dev schema fix: xml_generado → LONGTEXT");
        } catch (Exception e) {
            log.debug("Dev schema fix skipped (xml_generado ya es LONGTEXT o tabla no existe): {}", e.getMessage());
        }
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
}
