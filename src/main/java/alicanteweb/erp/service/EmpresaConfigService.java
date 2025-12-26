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
 * Servicio para gestionar la configuraciÃ³n de la empresa
 */
@Service
public class EmpresaConfigService {

    private static final Logger log = LoggerFactory.getLogger(EmpresaConfigService.class);

    private final EmpresaConfigRepository empresaConfigRepository;

    public EmpresaConfigService(EmpresaConfigRepository empresaConfigRepository) {
        this.empresaConfigRepository = empresaConfigRepository;
    }

    /**
     * Obtiene la configuraciÃ³n activa de la empresa
     */
    public Optional<EmpresaConfig> getConfiguracionActiva() {
        return empresaConfigRepository.findActive();
    }

    /**
     * Obtiene la configuraciÃ³n activa o lanza excepciÃ³n si no existe
     */
    public EmpresaConfig getConfiguracionActivaOrThrow() {
        return getConfiguracionActiva()
            .orElseThrow(() -> new IllegalStateException("No hay configuraciÃ³n de empresa activa. Configure los datos de su empresa primero."));
    }

    /**
     * Guarda o actualiza la configuraciÃ³n de empresa
     */
    @Transactional
    public EmpresaConfig save(EmpresaConfig config) {
        // Si se marca como activa, desactivar las demÃ¡s
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
        log.info("ConfiguraciÃ³n de empresa guardada: {}", saved.getNombreEmpresa());
        return saved;
    }

    /**
     * Obtiene todas las configuraciones
     */
    public List<EmpresaConfig> findAll() {
        return empresaConfigRepository.findAll();
    }

    /**
     * Verifica si existe configuraciÃ³n activa
     */
    public boolean existeConfiguracionActiva() {
        return empresaConfigRepository.existsActive();
    }

    /**
     * Obtiene configuraciÃ³n por ID
     */
    public Optional<EmpresaConfig> findById(Long id) {
        return empresaConfigRepository.findById(id);
    }

    /**
     * Activa una configuraciÃ³n especÃ­fica
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
            log.info("ConfiguraciÃ³n {} activada", config.getNombreEmpresa());
        }
    }

    /**
     * Elimina una configuraciÃ³n
     */
    @Transactional
    public void deleteById(Long id) {
        Optional<EmpresaConfig> opt = empresaConfigRepository.findById(id);
        if (opt.isPresent() && Boolean.TRUE.equals(opt.get().getActivo())) {
            throw new IllegalStateException("No se puede eliminar la configuraciÃ³n activa. Primero active otra configuraciÃ³n.");
        }
        empresaConfigRepository.deleteById(id);
        log.info("ConfiguraciÃ³n {} eliminada", id);
    }
}


