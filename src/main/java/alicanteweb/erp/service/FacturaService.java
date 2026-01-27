package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.FacturaRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para manejar facturas.
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
        return repository.findAllWithCliente();
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
        // NOTA: Eliminada la lógica de registro automático en Verifactu aquí para mantener
        // el comportamiento manual. El registro de evidencias se realiza únicamente
        // cuando el usuario ejecuta la acción desde la vista VeriFacTur (VerifactuController).
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

    /**
     * Devuelve el número de facturas pendientes de pago.
     */
    public int countFacturasPendientes() {
        // Suponiendo que existe un campo 'pagada' en la entidad Factura
        return (int) repository.countByPagadaFalse();
    }

    /**
     * Devuelve todas las facturas como ObservableList para la UI.
     */
    public ObservableList<Factura> findAllObservable() {
        return FXCollections.observableArrayList(findAll());
    }

    /**
     * Envía una factura a revisión (cambia estado de BORRADOR a REVISION)
     * Este método NO intenta registrar en Verifactu, solo cambia el estado
     */
    @Transactional
    public Factura enviarARevision(Long facturaId, String observaciones) {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"BORRADOR".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden enviar a revisión facturas en estado BORRADOR");
        }

        factura.setEstado("REVISION");
        if (observaciones != null && !observaciones.isEmpty()) {
            factura.setObservacionesRevision(observaciones);
        }

        return repository.save(factura);
    }

    /**
     * Aprueba y emite una factura (cambia estado de REVISION a EMITIDA)
     * Este método SÍ intenta registrar en Verifactu
     */
    @Transactional
    public Factura aprobarYEmitir(Long facturaId) {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"REVISION".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden emitir facturas en estado REVISION");
        }

        // Cambiar estado antes de persistir
        factura.setEstado("EMITIDA");
        Factura saved = repository.save(factura);

        // NOTA: Anteriormente aquí se intentaba registrar evidencia en Verifactu.
        // Para mantener el envío manual (desde la vista de VeriFacTur) eliminamos esa llamada.
        // Si más adelante se requiere un envío automático configurable, podemos introducir
        // una propiedad 'verifactu.autoSendOnEmit' y ejecutar el envío en background.

        return saved;
    }

    /**
     * Vuelve una factura de REVISION a BORRADOR
     */
    @Transactional
    public Factura volverABorrador(Long facturaId) {
        Factura factura = repository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        if (!"REVISION".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se pueden devolver a borrador facturas en REVISION");
        }

        factura.setEstado("BORRADOR");
        return repository.save(factura);
    }

    /**
     * Guarda una factura sin intentar registrar en Verifactu
     * Útil para actualizaciones simples de datos
     */
    @Transactional
    public Factura saveSimple(Factura factura) {
        return repository.save(factura);
    }
}
