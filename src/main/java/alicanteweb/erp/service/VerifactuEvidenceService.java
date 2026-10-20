package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Consulta de las evidencias (registros de facturación) VERI*FACTU. Es de solo lectura: los
 * registros son inalterables (RRSIF), así que no se exponen guardado ni borrado, y las acciones
 * que cambiaban el estado de remisión sin llamar a la AEAT están desactivadas.
 */
@Service
@Transactional(readOnly = true)
public class VerifactuEvidenceService {

    private final VerifactuEvidenceRepository repository;
    private final VerifactuService verifactuService;

    public VerifactuEvidenceService(VerifactuEvidenceRepository repository, VerifactuService verifactuService) {
        this.repository = repository;
        this.verifactuService = verifactuService;
    }

    public List<VerifactuEvidence> findAll() { return repository.findAll(); }
    public Optional<VerifactuEvidence> findById(Long id) { return repository.findById(id); }
    public Optional<VerifactuEvidence> findByFacturaId(String facturaId) {
        return repository.findFirstByFacturaIdOrderByFechaGeneracionRegistroDescIdDesc(facturaId)
                .or(() -> repository.findByFacturaId(facturaId));
    }
    public List<VerifactuEvidence> findAllByFacturaId(String facturaId) {
        return repository.findAllByFacturaIdOrderByFechaGeneracionRegistroAscIdAsc(facturaId);
    }
    public Optional<VerifactuEvidence> findByHash(String hash) { return repository.findByHash(hash); }
    public List<VerifactuEvidence> findBySerie(String serie) { return repository.findBySerieContainingIgnoreCase(serie); }
    public List<VerifactuEvidence> findByEstado(String estado) { return repository.findByEstado(estado); }
    public long countByEstado(String estado) { return repository.countByEstado(estado); }
    public Page<VerifactuEvidence> findPage(String estado, String q, Pageable pageable) {
        return repository.findPage(normalize(estado), normalize(q), pageable);
    }

    /**
     * Mensaje para las acciones de reenvío y verificación, desactivadas: hasta que exista una
     * remisión real (con respuesta de la AEAT o del proveedor) no se puede cambiar el estado de
     * remisión de un registro sin llamar a la AEAT, porque se mostraría un estado fiscal falso.
     */
    public static final String MENSAJE_ACCION_DESACTIVADA =
            "Acción desactivada: el ERP todavía no remite registros a la AEAT, así que no puede reenviar "
            + "ni verificar su estado. El estado mostrado es el real del registro.";

    /**
     * Desactivado (contención VERI*FACTU F0): antes ponía ENVIADO sin llamar a la AEAT.
     * Los registros de facturación son inalterables y no se reenvían desde aquí.
     */
    public VerifactuEvidence reenviarEvidencia(Long id) {
        getRequired(id);
        throw new IllegalStateException(MENSAJE_ACCION_DESACTIVADA);
    }

    /**
     * Desactivado (contención VERI*FACTU F0): antes ponía VERIFICADO sin consultar a la AEAT.
     */
    public VerifactuEvidence verificarEstadoAEAT(Long id) {
        getRequired(id);
        throw new IllegalStateException(MENSAJE_ACCION_DESACTIVADA);
    }

    public boolean validarCadenaIntegridad(String serie) {
        return verifactuService.validarCadenaIntegridad(serie);
    }

    private VerifactuEvidence getRequired(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evidencia no encontrada con ID: " + id));
    }
    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
