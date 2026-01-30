package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para manejar facturas.
 */
@Service
@Transactional(readOnly = true)
public class FacturaService {

    // Repositorio JPA que maneja la persistencia de Factura.
    private final FacturaRepository repository;
    private final VerifactuService verifactuService;

    // Inyección por constructor: la forma recomendada (evita @Autowired).
    public FacturaService(FacturaRepository repository, VerifactuService verifactuService) {
        this.repository = repository;
        this.verifactuService = verifactuService;
    }

    // Consultas de solo lectura (no necesitan transacción de escritura).
    public List<Factura> findAll() {
        return repository.findAllWithCliente();
    }

    public Optional<Factura> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Factura> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    // Operación que modifica datos: anotada con @Transactional para permitir commit.
    @Transactional
    public Factura save(Factura factura) {
        // Guardamos la factura usando JPA.
        return repository.save(factura);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Aprueba y emite una factura: exige el envío a AEAT mediante Verifactu.
     * Si el envío falla o AEAT no está disponible, se lanza excepción y se revierte la transacción.
     */
    @Transactional
    public Factura aprobarYEmitir(Long facturaId) throws Exception {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"REVISION".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden emitir facturas en estado REVISION");
        }

        // Exigir que AEAT esté disponible (keystore + cliente SOAP + propiedad)
        if (!verifactuService.isAeatAvailable()) {
            throw new IllegalStateException("Imposible emitir: AEAT no está disponible. Configure verifactu.aeat.enabled, el keystore y el cliente SOAP.");
        }

        // Delegar al servicio Verifactu para realizar todas las validaciones y el envío.
        // Este método lanzará excepción si algo falla (por ejemplo, error de conexión a AEAT).
        verifactuService.enviarFacturaVerifactu(factura);

        // Si llegamos aquí, el envío fue correcto; guardar la factura con estado actualizado por VerifactuService
        return repository.save(factura);
    }

}
