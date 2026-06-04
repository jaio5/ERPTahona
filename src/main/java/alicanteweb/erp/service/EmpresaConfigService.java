package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
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
        protegerEstadoVerifactu(config);
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

    @Transactional
    public EmpresaConfig guardarDatosVerifactu(String nifEmisor) {
        EmpresaConfig config = getConfiguracionActivaOrThrow();
        if (nifEmisor != null && !nifEmisor.isBlank()) {
            config.setVerifactuNifEmisor(nifEmisor.trim());
        }
        return empresaConfigRepository.save(config);
    }

    @Transactional
    public EmpresaConfig iniciarFuncionamientoVerifactu(String nifEmisor) {
        if (nifEmisor == null || nifEmisor.isBlank()) {
            throw new IllegalArgumentException("El NIF del emisor es obligatorio para iniciar VERI*FACTU.");
        }

        EmpresaConfig config = getConfiguracionActivaOrThrow();
        boolean yaEstabaVigente = isFuncionamientoVerifactuVigente(config);
        config.setVerifactuHabilitado(true);
        config.setVerifactuNifEmisor(nifEmisor.trim());
        if (config.getVerifactuFechaInicio() == null || !yaEstabaVigente) {
            config.setVerifactuFechaInicio(LocalDate.now());
        }
        config.setVerifactuFechaRenuncia(null);
        EmpresaConfig saved = empresaConfigRepository.save(config);
        log.info("Funcionamiento VERI*FACTU iniciado para {} desde {}", saved.getNombreEmpresa(), saved.getVerifactuFechaInicio());
        return saved;
    }

    @Transactional
    public EmpresaConfig programarRenunciaVerifactuFinDeAnio() {
        EmpresaConfig config = getConfiguracionActivaOrThrow();
        if (!isFuncionamientoVerifactuVigente(config)) {
            throw new IllegalStateException("La empresa no esta funcionando actualmente como VERI*FACTU.");
        }

        LocalDate finDeAnio = LocalDate.of(LocalDate.now().getYear(), Month.DECEMBER, 31);
        config.setVerifactuFechaRenuncia(finDeAnio);
        EmpresaConfig saved = empresaConfigRepository.save(config);
        log.info("Renuncia VERI*FACTU programada para {} en {}", saved.getNombreEmpresa(), finDeAnio);
        return saved;
    }

    public boolean isFuncionamientoVerifactuVigente(EmpresaConfig config) {
        if (config == null || !Boolean.TRUE.equals(config.getVerifactuHabilitado())) {
            return false;
        }
        LocalDate renuncia = config.getVerifactuFechaRenuncia();
        return renuncia == null || !LocalDate.now().isAfter(renuncia);
    }

    private void protegerEstadoVerifactu(EmpresaConfig config) {
        if (config == null || config.getId() == null) {
            return;
        }

        empresaConfigRepository.findById(config.getId()).ifPresent(actual -> {
            boolean estabaVigente = isFuncionamientoVerifactuVigente(actual);
            boolean quedaHabilitado = Boolean.TRUE.equals(config.getVerifactuHabilitado());
            if (estabaVigente && !quedaHabilitado) {
                throw new IllegalStateException("VERI*FACTU no puede desactivarse directamente. Debe programarse la renuncia para el 31 de diciembre del anio en curso.");
            }
        });
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
