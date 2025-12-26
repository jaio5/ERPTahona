package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.RgpdConsentimiento;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RgpdConsentimientoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestiÃ³n de consentimientos RGPD
 */
@Service
@Slf4j
public class RgpdConsentimientoService {

    private final RgpdConsentimientoRepository consentimientoRepository;
    private final AuditoriaService auditoriaService;

    public RgpdConsentimientoService(RgpdConsentimientoRepository consentimientoRepository,
                                    AuditoriaService auditoriaService) {
        this.consentimientoRepository = consentimientoRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Registrar un nuevo consentimiento
     */
    @Transactional
    public RgpdConsentimiento registrarConsentimiento(Cliente cliente, String tipoConsentimiento,
                                                     Boolean otorgado, String ip, Usuario usuarioRegistro) {
        log.info("Registrando consentimiento {} para cliente {}", tipoConsentimiento,
                cliente != null ? cliente.getId() : "sin cliente");

        RgpdConsentimiento consentimiento = new RgpdConsentimiento();
        consentimiento.setCliente(cliente);
        consentimiento.setTipoConsentimiento(tipoConsentimiento);
        consentimiento.setOtorgado(otorgado);
        consentimiento.setFechaConsentimiento(LocalDateTime.now());
        consentimiento.setIpOrigen(ip);
        consentimiento.setUsuarioRegistro(usuarioRegistro);
        consentimiento.setActivo(true);

        // Metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fecha_registro", LocalDateTime.now().toString());
        if (usuarioRegistro != null) {
            metadata.put("usuario_registro", usuarioRegistro.getUsername());
        }
        consentimiento.setMetadata(metadata);

        RgpdConsentimiento guardado = consentimientoRepository.save(consentimiento);

        // Auditar
        auditoriaService.registrarCreacion(usuarioRegistro, "RgpdConsentimiento",
                guardado.getId().toString(),
                "Consentimiento " + tipoConsentimiento + " " + (otorgado ? "otorgado" : "denegado"));

        log.info("Consentimiento registrado: {}", guardado.getId());
        return guardado;
    }

    /**
     * Revocar un consentimiento
     */
    @Transactional
    public void revocarConsentimiento(Long consentimientoId, Usuario usuario) {
        RgpdConsentimiento consentimiento = consentimientoRepository.findById(consentimientoId)
                .orElseThrow(() -> new IllegalArgumentException("Consentimiento no encontrado"));

        consentimiento.setFechaRevocacion(LocalDateTime.now());
        consentimiento.setOtorgado(false);
        consentimientoRepository.save(consentimiento);

        // Auditar
        auditoriaService.registrarActualizacion(usuario, "RgpdConsentimiento",
                consentimientoId.toString(),
                "Consentimiento " + consentimiento.getTipoConsentimiento() + " revocado");

        log.info("Consentimiento {} revocado", consentimientoId);
    }

    /**
     * Verificar si un cliente tiene consentimiento activo para un tipo
     */
    public boolean tieneConsentimientoActivo(Long clienteId, String tipo) {
        return consentimientoRepository.tieneConsentimientoActivo(clienteId, tipo);
    }

    /**
     * Obtener consentimientos de un cliente
     */
    public List<RgpdConsentimiento> obtenerConsentimientos(Long clienteId) {
        return consentimientoRepository.findByClienteIdAndActivoTrue(clienteId);
    }

    /**
     * Obtener todos los consentimientos de un cliente (incluidos inactivos)
     */
    public List<RgpdConsentimiento> obtenerTodosConsentimientos(Long clienteId) {
        return consentimientoRepository.findByClienteId(clienteId);
    }

    /**
     * Obtener Ãºltimo consentimiento de un cliente por tipo
     */
    public RgpdConsentimiento obtenerUltimoConsentimiento(Long clienteId, String tipo) {
        return consentimientoRepository
                .findFirstByClienteIdAndTipoConsentimientoOrderByFechaConsentimientoDesc(clienteId, tipo)
                .orElse(null);
    }

    /**
     * Obtener consentimientos por email
     */
    public List<RgpdConsentimiento> obtenerPorEmail(String email) {
        return consentimientoRepository.findByEmail(email);
    }

    /**
     * Obtener consentimientos otorgados activos
     */
    public List<RgpdConsentimiento> obtenerConsentimientosOtorgados() {
        return consentimientoRepository.findByOtorgadoTrueAndActivoTrue();
    }

    /**
     * Obtener consentimientos revocados
     */
    public List<RgpdConsentimiento> obtenerConsentimientosRevocados() {
        return consentimientoRepository.findRevocados();
    }

    /**
     * Obtener consentimientos por tipo
     */
    public List<RgpdConsentimiento> obtenerPorTipo(String tipo) {
        return consentimientoRepository.findByTipoConsentimientoAndActivoTrue(tipo);
    }

    /**
     * Obtener consentimientos por rango de fechas
     */
    public List<RgpdConsentimiento> obtenerPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return consentimientoRepository.findByFechaConsentimientoBetween(inicio, fin);
    }

    /**
     * Contar consentimientos activos
     */
    public long contarActivos() {
        return consentimientoRepository.countByActivoTrue();
    }

    /**
     * Contar consentimientos por tipo
     */
    public long contarPorTipo(String tipo) {
        return consentimientoRepository.countByTipoConsentimientoAndActivoTrue(tipo);
    }

    /**
     * Actualizar polÃ­tica de privacidad
     * Marca todos los consentimientos activos para revalidaciÃ³n
     */
    @Transactional
    public void actualizarPoliticaPrivacidad(String nuevaVersion, Usuario usuario) {
        log.warn("Actualizando polÃ­tica de privacidad a versiÃ³n: {}", nuevaVersion);

        List<RgpdConsentimiento> consentimientosActivos =
                consentimientoRepository.findByOtorgadoTrueAndActivoTrue();

        for (RgpdConsentimiento consentimiento : consentimientosActivos) {
            // Marcar para revalidaciÃ³n
            consentimiento.setActivo(false);
            Map<String, Object> metadata = consentimiento.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }
            metadata.put("requiere_revalidacion", true);
            metadata.put("version_anterior", consentimiento.getVersionPolitica());
            metadata.put("nueva_version", nuevaVersion);
            metadata.put("fecha_cambio_politica", LocalDateTime.now().toString());
            consentimiento.setMetadata(metadata);
            consentimientoRepository.save(consentimiento);
        }

        // Auditar
        auditoriaService.registrarAccion(usuario, "ACTUALIZACION_POLITICA_PRIVACIDAD",
                "PoliticaPrivacidad", nuevaVersion,
                "PolÃ­tica actualizada - " + consentimientosActivos.size() + " consentimientos requieren revalidaciÃ³n");

        log.info("PolÃ­tica de privacidad actualizada. {} consentimientos requieren revalidaciÃ³n",
                consentimientosActivos.size());
    }
}


