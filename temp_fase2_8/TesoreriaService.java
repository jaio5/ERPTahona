package alicanteweb.erp.service;

import alicanteweb.erp.entities.Banco;
import alicanteweb.erp.entities.MovimientoBancario;
import alicanteweb.erp.repository.BancoRepository;
import alicanteweb.erp.repository.MovimientoBancarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión bancaria y tesorería
 * FASE 4: Financiero
 */
@Service
@Slf4j
public class TesoreriaService {

    private final BancoRepository bancoRepository;
    private final MovimientoBancarioRepository movimientoRepository;
    private final AuditoriaService auditoriaService;

    public TesoreriaService(BancoRepository bancoRepository,
                           MovimientoBancarioRepository movimientoRepository,
                           AuditoriaService auditoriaService) {
        this.bancoRepository = bancoRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Registra un movimiento bancario
     */
    @Transactional
    public MovimientoBancario registrarMovimiento(MovimientoBancario movimiento) {
        log.info("Registrando movimiento bancario: {} - {}",
                movimiento.getTipo(), movimiento.getImporte());

        Banco banco = movimiento.getBanco();

        // Calcular saldos
        movimiento.setSaldoAnterior(banco.getSaldoActual());

        BigDecimal nuevoSaldo;
        if ("INGRESO".equals(movimiento.getTipo())) {
            nuevoSaldo = banco.getSaldoActual().add(movimiento.getImporte());
        } else {
            nuevoSaldo = banco.getSaldoActual().subtract(movimiento.getImporte());
        }

        movimiento.setSaldoPosterior(nuevoSaldo);

        // Actualizar saldo del banco
        banco.setSaldoActual(nuevoSaldo);
        bancoRepository.save(banco);

        MovimientoBancario guardado = movimientoRepository.save(movimiento);

        auditoriaService.registrarCreacion(null, "MovimientoBancario",
                guardado.getId().toString(),
                "Movimiento: " + movimiento.getTipo() + " - " + movimiento.getImporte());

        return guardado;
    }

    /**
     * Concilia un movimiento
     */
    @Transactional
    public void conciliarMovimiento(Long movimientoId) {
        MovimientoBancario movimiento = movimientoRepository.findById(movimientoId)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado"));

        movimiento.setConciliado(true);
        movimiento.setFechaConciliacion(LocalDate.now());
        movimientoRepository.save(movimiento);

        log.info("Movimiento conciliado: {}", movimientoId);
    }

    /**
     * Obtiene el flujo de caja (cash flow)
     */
    public Map<String, Object> obtenerFlujoCaja(LocalDate inicio, LocalDate fin) {
        Map<String, Object> flujo = new HashMap<>();

        List<Banco> bancos = bancoRepository.findByActivoTrueOrderByNombreAsc();

        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalPagos = BigDecimal.ZERO;

        for (Banco banco : bancos) {
            List<MovimientoBancario> movimientos =
                    movimientoRepository.findByBancoIdAndFechaBetweenOrderByFechaDescIdDesc(
                            banco.getId(), inicio, fin);

            for (MovimientoBancario mov : movimientos) {
                if ("INGRESO".equals(mov.getTipo())) {
                    totalIngresos = totalIngresos.add(mov.getImporte());
                } else {
                    totalPagos = totalPagos.add(mov.getImporte());
                }
            }
        }

        flujo.put("ingresos", totalIngresos);
        flujo.put("pagos", totalPagos);
        flujo.put("saldo", totalIngresos.subtract(totalPagos));
        flujo.put("periodo_inicio", inicio);
        flujo.put("periodo_fin", fin);

        return flujo;
    }

    /**
     * Obtiene posición de tesorería
     */
    public Map<String, Object> obtenerPosicionTesoreria() {
        List<Banco> bancos = bancoRepository.findByActivoTrueOrderByNombreAsc();

        BigDecimal totalDisponible = bancos.stream()
                .map(Banco::getSaldoActual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> posicion = new HashMap<>();
        posicion.put("total_disponible", totalDisponible);
        posicion.put("bancos", bancos);
        posicion.put("fecha", LocalDate.now());

        return posicion;
    }

    /**
     * Lista movimientos sin conciliar
     */
    public List<MovimientoBancario> obtenerMovimientosSinConciliar() {
        return movimientoRepository.findByConciliadoFalse();
    }
}

