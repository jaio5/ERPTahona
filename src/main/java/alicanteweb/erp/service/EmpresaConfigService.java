package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para gestionar la configuración de la empresa
 */
@Service
public class EmpresaConfigService {

    private static final Logger log = LoggerFactory.getLogger(EmpresaConfigService.class);

    private final EmpresaConfigRepository empresaConfigRepository;
    private final FacturacionEventoService facturacionEventoService;

    public EmpresaConfigService(EmpresaConfigRepository empresaConfigRepository,
                                FacturacionEventoService facturacionEventoService) {
        this.empresaConfigRepository = empresaConfigRepository;
        this.facturacionEventoService = facturacionEventoService;
    }

    /**
     * Obtiene la configuración activa de la empresa
     */
    public Optional<EmpresaConfig> getConfiguracionActiva() {
        // Llamamos a repo que devuelve la primera por activo. Añadimos diagnóstico si hay múltiples.
        List<EmpresaConfig> activas = empresaConfigRepository.findByActivoTrue();
        if (activas == null || activas.isEmpty()) {
            return Optional.empty();
        }
        if (activas.size() > 1) {
            log.warn("Se detectaron {} configuraciones activas en la BD. Usando la primera encontrada (id={}). Revisa la tabla empresa_config.", activas.size(), activas.get(0).getId());
        }
        return Optional.ofNullable(activas.get(0));
    }

    /**
     * Obtiene la configuración activa o lanza excepción si no existe
     */
    public EmpresaConfig getConfiguracionActivaOrThrow() {
        return getConfiguracionActiva()
            .orElseThrow(() -> new IllegalStateException("No hay configuración de empresa activa. Configure los datos de su empresa primero."));
    }

    /**
     * Guarda o actualiza la configuración de empresa
     */
    @Transactional
    public EmpresaConfig save(EmpresaConfig config) {
        EmpresaConfig anterior = config.getId() != null
            ? empresaConfigRepository.findById(config.getId()).orElse(null)
            : null;

        // Si se marca como activa, desactivar las demás
        if (Boolean.TRUE.equals(config.getActivo())) {
            List<EmpresaConfig> todas = empresaConfigRepository.findAll();
            for (EmpresaConfig c : todas) {
                if (!c.getId().equals(config.getId())) {
                    c.setActivo(false);
                    empresaConfigRepository.save(c);
                }
            }
        }

        EmpresaConfig saved = empresaConfigRepository.save(config);
        registrarEventoConfiguracion(anterior, saved);
        log.info("Configuración de empresa guardada: {}", saved.getNombreEmpresa());
        return saved;
    }

    /**
     * Obtiene todas las configuraciones
     */
    public List<EmpresaConfig> findAll() {
        return empresaConfigRepository.findAll();
    }

    /**
     * Verifica si existe configuración activa
     */
    public boolean existeConfiguracionActiva() {
        return empresaConfigRepository.existsActive();
    }

    /**
     * Obtiene configuración por ID
     */
    public Optional<EmpresaConfig> findById(Long id) {
        return empresaConfigRepository.findById(id);
    }

    /**
     * Activa una configuración específica
     */
    @Transactional
    public void activar(Long id) {
        Optional<EmpresaConfig> optConfig = empresaConfigRepository.findById(id);
        if (optConfig.isPresent()) {
            // Desactivar todas
            List<EmpresaConfig> todas = empresaConfigRepository.findAll();
            todas.forEach(c -> {
                c.setActivo(false);
                empresaConfigRepository.save(c);
            });

            // Activar la seleccionada
            EmpresaConfig config = optConfig.get();
            config.setActivo(true);
            empresaConfigRepository.save(config);
            log.info("Configuración {} activada", config.getNombreEmpresa());
        }
    }

    /**
     * Elimina una configuración
     */
    @Transactional
    public void deleteById(Long id) {
        Optional<EmpresaConfig> opt = empresaConfigRepository.findById(id);
        if (opt.isPresent() && Boolean.TRUE.equals(opt.get().getActivo())) {
            throw new IllegalStateException("No se puede eliminar la configuración activa. Primero active otra configuración.");
        }
        empresaConfigRepository.deleteById(id);
        log.info("Configuración {} eliminada", id);
    }
    private void registrarEventoConfiguracion(EmpresaConfig anterior, EmpresaConfig saved) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("empresaId", saved.getId());
            metadata.put("nif", saved.getCif());
            metadata.put("modalidadSif", saved.getSifModalidad() != null ? saved.getSifModalidad().name() : null);
            metadata.put("origenSistema", "EmpresaConfigService");
            metadata.put("versionSistema", saved.getVerifactuVersionSistema());
            metadata.put("declaracionResponsableEmitida", saved.getDeclaracionResponsableEmitida());
            if (anterior != null) {
                metadata.put("modalidadAnterior", anterior.getSifModalidad() != null ? anterior.getSifModalidad().name() : null);
                metadata.put("versionAnterior", anterior.getVerifactuVersionSistema());
            }
            facturacionEventoService.registrarEvento(
                FacturacionEventoService.AMBITO_GLOBAL,
                anterior == null ? "CONFIG_EMPRESA_ALTA" : "CONFIG_EMPRESA_MODIFICADA",
                saved.getId() != null ? saved.getId().toString() : saved.getCif(),
                metadata
            );
        } catch (Exception e) {
            log.warn("No se pudo registrar evento fiscal de configuracion de empresa: {}", e.getMessage());
        }
    }
}
