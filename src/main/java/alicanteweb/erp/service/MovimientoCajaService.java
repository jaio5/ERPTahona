package alicanteweb.erp.service;

import alicanteweb.erp.entities.MovimientoCaja;
import alicanteweb.erp.repository.MovimientoCajaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de movimientos de caja
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class MovimientoCajaService {

    private final MovimientoCajaRepository repository;

    public MovimientoCajaService(MovimientoCajaRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtener todos los movimientos
     */
    public List<MovimientoCaja> findAll() {
        return repository.findAll();
    }

    /**
     * Buscar por ID
     */
    public Optional<MovimientoCaja> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * Buscar movimientos entre fechas
     */
    public List<MovimientoCaja> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findByFechaBetween(fechaInicio, fechaFin);
    }

    /**
     * Buscar por tipo (INGRESO o GASTO)
     */
    public List<MovimientoCaja> findByTipo(String tipo) {
        return repository.findByTipo(tipo);
    }

    /**
     * Buscar movimientos de una fecha específica
     */
    public List<MovimientoCaja> findByFecha(LocalDate fecha) {
        return repository.findByFechaOrderByFechaDesc(fecha);
    }

    /**
     * Calcular saldo actual de caja
     */
    public BigDecimal calcularSaldoActual() {
        return repository.calcularSaldoCaja();
    }

    /**
     * Calcular total de ingresos
     */
    public BigDecimal calcularTotalIngresos() {
        return repository.calcularTotalIngresos();
    }

    /**
     * Calcular total de gastos
     */
    public BigDecimal calcularTotalGastos() {
        return repository.calcularTotalGastos();
    }

    /**
     * Guardar un movimiento
     */
    @Transactional
    public MovimientoCaja save(MovimientoCaja movimiento) {
        log.info("Guardando movimiento de caja: {} - {} €", movimiento.getConcepto(), movimiento.getImporte());
        return repository.save(movimiento);
    }

    /**
     * Eliminar un movimiento
     */
    @Transactional
    public void deleteById(Long id) {
        log.info("Eliminando movimiento de caja con ID: {}", id);
        repository.deleteById(id);
    }

    /**
     * Registrar un ingreso
     */
    @Transactional
    public MovimientoCaja registrarIngreso(String concepto, BigDecimal importe, String categoria) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setFecha(LocalDate.now());
        movimiento.setTipo("INGRESO");
        movimiento.setConcepto(concepto);
        movimiento.setImporte(importe);
        movimiento.setCategoria(categoria);
        return save(movimiento);
    }

    /**
     * Registrar un gasto
     */
    @Transactional
    public MovimientoCaja registrarGasto(String concepto, BigDecimal importe, String categoria) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setFecha(LocalDate.now());
        movimiento.setTipo("GASTO");
        movimiento.setConcepto(concepto);
        movimiento.setImporte(importe);
        movimiento.setCategoria(categoria);
        return save(movimiento);
    }
}

