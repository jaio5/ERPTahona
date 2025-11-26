package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.FacturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para manejar facturas.
 *
 * Explicación para un estudiante de DAM:
 * - Esta clase es un bean de Spring anotado con @Service. Spring la gestiona y
 *   la expone para que otros beans (por ejemplo controladores FXML) la inyecten.
 * - Se utiliza @Transactional(readOnly = true) a nivel de clase para que, por defecto,
 *   las operaciones sean de solo lectura (más eficiente para consultas). Métodos que
 *   modifican datos se anotan con @Transactional para permitir commit/rollback.
 * - La lógica de negocio (guardar, borrar, imprimir) debe residir en este servicio,
 *   no en los controladores de la UI.
 * - Aquí, tras guardar una factura, intentamos registrar una evidencia en Verifactu.
 *   Si ocurre un error en el registro de evidencia no abortamos el guardado: la factura
 *   ya está persistida. Esto es una decisión de diseño: si la evidencia fuese obligatoria
 *   habría que propagar la excepción.
 */
@Service
@Transactional(readOnly = true)
public class FacturaService {

    // Repositorio JPA que maneja la persistencia de Factura.
    private final FacturaRepository repository;
    // Servicio que registra evidencias en Verifactu (firma/huella/envío a AEAT)
    private final VerifactuEvidenceService verifactuEvidenceService;

    // Inyección por constructor: la forma recomendada (evita @Autowired).
    public FacturaService(FacturaRepository repository, VerifactuEvidenceService verifactuEvidenceService) {
        this.repository = repository;
        this.verifactuEvidenceService = verifactuEvidenceService;
    }

    // Consultas de solo lectura (no necesitan transacción de escritura).
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

    // Operación que modifica datos: anotada con @Transactional para permitir commit.
    @Transactional
    public Factura save(Factura factura) {
        // Guardamos la factura usando JPA.
        Factura saved = repository.save(factura);
        try {
            // Intentamos registrar evidencia en Verifactu (firma/huella y envío a AEAT)
            // La implementación actual de registrarEvidenciaAEAT espera: datosFactura, serie, numero.
            // Aquí pasamos el id como string y el número de factura. Ajusta según tu entidad.
            verifactuEvidenceService.registrarEvidenciaAEAT(
                saved.getId() != null ? saved.getId().toString() : "",
                "", // No hay campo serie en Factura (ajustar si existe)
                saved.getNumero() != null ? saved.getNumero() : ""
            );
        } catch (Exception e) {
            // No lanzamos la excepción para no impedir la continuación de la aplicación.
            // En una aplicación real, aquí deberías guardar el error en logs estructurados
            // y/o mostrar un aviso en la UI para que el usuario sepa que la evidencia falló.
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
     * Ejemplo de método de conveniencia que delega en el repositorio.
     */
    public Optional<Factura> findUltimaFacturaPorCliente(Long clienteId) {
        return repository.findTopByCliente_IdOrderByFechaDesc(clienteId);
    }

    /**
     * Ejemplo de método que dispararía la lógica de impresión (PDF/JasperReports).
     * Actualmente solo imprime una línea por consola; aquí deberías integrar
     * con la librería que uses para generar documentos.
     */
    public void imprimirFactura(Factura factura) {
        // Aquí deberías implementar la lógica real de impresión (PDF, JasperReports, etc.)
        System.out.println("Imprimiendo factura: " + factura.getNumero());
    }
}
