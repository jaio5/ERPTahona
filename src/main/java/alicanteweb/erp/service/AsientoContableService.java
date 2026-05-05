package alicanteweb.erp.service;

import alicanteweb.erp.entities.AsientoContable;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.AsientoContableRepository;
import alicanteweb.erp.repository.FacturaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de asientos contables
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AsientoContableService {

    private final AsientoContableRepository repository;
    private final FacturaRepository facturaRepository;
    private final ContabilidadService contabilidadService;

    public AsientoContableService(AsientoContableRepository repository,
                                  FacturaRepository facturaRepository,
                                  @Lazy ContabilidadService contabilidadService) {
        this.repository = repository;
        this.facturaRepository = facturaRepository;
        this.contabilidadService = contabilidadService;
    }

    /**
     * Obtener todos los asientos
     */
    public List<AsientoContable> findAll() {
        return repository.findAll();
    }

    /**
     * Buscar por ID
     */
    public Optional<AsientoContable> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Buscar asientos entre fechas
     */
    public List<AsientoContable> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findByFechaBetween(fechaInicio, fechaFin);
    }

    /**
     * Buscar por concepto
     */
    public List<AsientoContable> findByConcepto(String concepto) {
        return repository.findByConcepto(concepto);
    }

    /**
     * Buscar asientos de apertura
     */
    public List<AsientoContable> findAsientosApertura() {
        return repository.findByAsientoAperturaTrue();
    }

    /**
     * Buscar asientos de cierre
     */
    public List<AsientoContable> findAsientosCierre() {
        return repository.findByAsientoCierreTrue();
    }

    /**
     * Guardar un asiento
     */
    @Transactional
    public AsientoContable save(AsientoContable asiento) {
        log.info("Guardando asiento contable: {}", asiento.getNumero());

        // Calcular descuadre antes de guardar
        asiento.calcularDescuadre();

        // Validar que esté cuadrado
        if (!asiento.estaCuadrado()) {
            log.warn("Asiento {} descuadrado por: {}", asiento.getNumero(), asiento.getDescuadre());
        }

        return repository.save(asiento);
    }

    /**
     * Eliminar un asiento
     */
    @Transactional
    public void deleteById(Long id) {
        log.info("Eliminando asiento contable con ID: {}", id);
        repository.deleteById(id);
    }

    /**
     * Validar que un asiento esté cuadrado (debe = haber)
     */
    public boolean validarAsiento(AsientoContable asiento) {
        asiento.calcularDescuadre();
        return asiento.estaCuadrado();
    }

    /**
     * Calcular total del Debe de un asiento
     */
    public BigDecimal calcularTotalDebe(AsientoContable asiento) {
        if (asiento.getLineas() == null || asiento.getLineas().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return asiento.getLineas().stream()
            .map(l -> l.getDebe() != null ? l.getDebe() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcular total del Haber de un asiento
     */
    public BigDecimal calcularTotalHaber(AsientoContable asiento) {
        if (asiento.getLineas() == null || asiento.getLineas().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return asiento.getLineas().stream()
            .map(l -> l.getHaber() != null ? l.getHaber() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtener el siguiente número de asiento
     */
    public Integer obtenerSiguienteNumero() {
        List<AsientoContable> asientos = repository.findAll();
        if (asientos.isEmpty()) {
            return 1;
        }

        return asientos.stream()
            .map(AsientoContable::getNumero)
            .filter(numero -> numero != null && numero.matches("\\d+"))
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0) + 1;
    }

    /**
     * Crear asiento desde factura delegando en ContabilidadService
     */
    @Transactional
    public AsientoContable crearAsientoDesdeFactura(Long facturaId) {
        log.info("Creando asiento contable desde factura ID: {}", facturaId);
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada con ID: " + facturaId));
        return contabilidadService.generarAsientoFactura(factura, null);
    }
}

