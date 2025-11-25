package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FacturaService {

    private final FacturaRepository repository;
    private final VerifactuEvidenceService verifactuEvidenceService;

    public FacturaService(FacturaRepository repository, VerifactuEvidenceService verifactuEvidenceService) {
        this.repository = repository;
        this.verifactuEvidenceService = verifactuEvidenceService;
    }

    public List<Factura> findAll() {
        return repository.findAll();
    }

    public Optional<Factura> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Factura> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public boolean existsByNumero(String numero) {
        return repository.existsByNumero(numero);
    }

    @Transactional
    public Factura save(Factura factura) {
        Factura saved = repository.save(factura);
        try {
            verifactuEvidenceService.registrarEvidenciaAEAT(
                saved.getId() != null ? saved.getId().toString() : "",
                "", // No hay campo serie en Factura
                saved.getNumero() != null ? saved.getNumero() : ""
            );
        } catch (Exception e) {
            // Loguear el error, pero no impedir la emisión de la factura
            System.err.println("Error registrando evidencia Verifactur: " + e.getMessage());
        }
        return saved;
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Devuelve la última factura de un cliente por fecha descendente.
     */
    public Optional<Factura> findUltimaFacturaPorCliente(Long clienteId) {
        return repository.findTopByCliente_IdOrderByFechaDesc(clienteId);
    }

    public void imprimirFactura(Factura factura) {
        // Aquí deberías implementar la lógica real de impresión (PDF, JasperReports, etc.)
        System.out.println("Imprimiendo factura: " + factura.getNumero());
        // Ejemplo: Generar PDF, abrir diálogo de impresión, etc.
    }
}
