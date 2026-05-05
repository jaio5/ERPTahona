package alicanteweb.erp.service;

import alicanteweb.erp.entities.Banco;
import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.repository.BancoRepository;
import alicanteweb.erp.repository.MovimientoBancoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para movimientos bancarios.
 */
@Service
@Transactional(readOnly = true)
public class MovimientoBancoService {

    private final MovimientoBancoRepository repository;
    private final BancoRepository bancoRepository;

    public MovimientoBancoService(MovimientoBancoRepository repository, BancoRepository bancoRepository) {
        this.repository = repository;
        this.bancoRepository = bancoRepository;
    }

    public List<MovimientoBanco> findAll() {
        return repository.findAllWithBanco();
    }

    public List<Banco> findBancosActivos() {
        return bancoRepository.findByActivoTrue();
    }

    public Optional<MovimientoBanco> findById(Long id) {
        return repository.findById(id);
    }

    public List<MovimientoBanco> findByFechas(LocalDate desde, LocalDate hasta) {
        return repository.findByFechaBetween(desde, hasta);
    }

    public List<MovimientoBanco> findNoConciliados() {
        return repository.findByConciliado(false);
    }

    public List<MovimientoBanco> buscarPorConcepto(String concepto) {
        return repository.findByConceptoContainingIgnoreCase(concepto);
    }

    @Transactional
    public MovimientoBanco save(MovimientoBanco movimiento) {
        MovimientoBanco guardado = repository.save(movimiento);
        recalcularSaldosBanco(guardado.getBanco().getId());
        return guardado;
    }

    @Transactional
    public void deleteById(Long id) {
        Long bancoId = repository.findById(id)
            .map(m -> m.getBanco() != null ? m.getBanco().getId() : null)
            .orElse(null);
        repository.deleteById(id);
        if (bancoId != null) {
            recalcularSaldosBanco(bancoId);
        }
    }

    @Transactional
    public MovimientoBanco conciliar(Long id) {
        MovimientoBanco m = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento bancario no encontrado: " + id));
        m.setConciliado(true);
        m.setFechaConciliacion(LocalDate.now());
        return repository.save(m);
    }

    private void recalcularSaldosBanco(Long bancoId) {
        Banco banco = bancoRepository.findById(bancoId)
            .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado: " + bancoId));
        BigDecimal saldo = BigDecimal.ZERO;
        for (MovimientoBanco movimiento : repository.findByBancoIdOrderByFechaAscIdAsc(bancoId)) {
            BigDecimal importe = movimiento.getImporte() != null ? movimiento.getImporte() : BigDecimal.ZERO;
            saldo = "GASTO".equals(movimiento.getTipo()) ? saldo.subtract(importe) : saldo.add(importe);
            movimiento.setSaldoResultante(saldo);
            repository.save(movimiento);
        }
        banco.setSaldoActual(saldo);
        bancoRepository.save(banco);
    }
}
