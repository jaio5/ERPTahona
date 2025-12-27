package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la configuración de la empresa
 */
@Service
public class EmpresaConfigService {

    private static final Logger log = LoggerFactory.getLogger(EmpresaConfigService.class);

    private final EmpresaConfigRepository empresaConfigRepository;

    public EmpresaConfigService(EmpresaConfigRepository empresaConfigRepository) {
        this.empresaConfigRepository = empresaConfigRepository;
    }

    /**
     * Obtiene la configuración activa de la empresa
     */
    public Optional<EmpresaConfig> getConfiguracionActiva() {
        return empresaConfigRepository.findActive();
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
}


