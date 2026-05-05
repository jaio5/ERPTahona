package alicanteweb.erp.service;

import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.entities.PedidoCompraSerieSequence;
import alicanteweb.erp.repository.PedidoCompraRepository;
import alicanteweb.erp.repository.PedidoCompraSerieSequenceRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para pedidos de compra a proveedores.
 */
@Service
@Transactional(readOnly = true)
public class PedidoCompraService {
    private static final Logger log = LoggerFactory.getLogger(PedidoCompraService.class);
    private static final String SERIE_PEDIDO_COMPRA = "PC";
    private static final int MAX_REINTENTOS_SECUENCIA = 3;

    private final PedidoCompraRepository repository;
    private final PedidoCompraSerieSequenceRepository sequenceRepository;
    private final EntityManager entityManager;

    public PedidoCompraService(PedidoCompraRepository repository,
                               PedidoCompraSerieSequenceRepository sequenceRepository,
                               EntityManager entityManager) {
        this.repository = repository;
        this.sequenceRepository = sequenceRepository;
        this.entityManager = entityManager;
    }

    public List<PedidoCompra> findAll() {
        return repository.findAllWithProveedor();
    }

    public Optional<PedidoCompra> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<PedidoCompra> findByIdWithLineas(Long id) {
        return repository.findByIdWithLineas(id);
    }

    public Optional<PedidoCompra> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public List<PedidoCompra> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }

    @Transactional
    public PedidoCompra save(PedidoCompra pedido) {
        if (pedido.getNumero() == null || pedido.getNumero().isBlank()) {
            LocalDate fecha = pedido.getFecha() != null ? pedido.getFecha() : LocalDate.now();
            pedido.setNumero(generarNumero(fecha));
        }
        return repository.save(pedido);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Genera un número de pedido de compra automático basado en año y secuencia.
     */
    public String generarNumero() {
        return generarNumero(LocalDate.now());
    }

    @Transactional
    public String generarNumero(LocalDate fecha) {
        int ejercicio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();

        for (int intento = 1; intento <= MAX_REINTENTOS_SECUENCIA; intento++) {
            try {
                PedidoCompraSerieSequence sequence = sequenceRepository.findBySerieAndEjercicio(SERIE_PEDIDO_COMPRA, ejercicio)
                    .orElseGet(() -> crearSecuenciaPedidoCompra(ejercicio));

                long siguienteNumero = sequence.getUltimoNumero() + 1L;
                sequence.setUltimoNumero(siguienteNumero);
                sequenceRepository.saveAndFlush(sequence);

                return String.format("%s-%d-%05d", SERIE_PEDIDO_COMPRA, ejercicio, siguienteNumero);
            } catch (DataIntegrityViolationException ex) {
                entityManager.clear();
                log.warn("Conflicto inicializando secuencia de pedidos de compra {}/{} en intento {}",
                    SERIE_PEDIDO_COMPRA, ejercicio, intento);
            }
        }

        throw new IllegalStateException("No se pudo reservar un numero de pedido de compra para " + ejercicio);
    }

    private PedidoCompraSerieSequence crearSecuenciaPedidoCompra(int ejercicio) {
        PedidoCompraSerieSequence nueva = new PedidoCompraSerieSequence();
        nueva.setSerie(SERIE_PEDIDO_COMPRA);
        nueva.setEjercicio(ejercicio);
        nueva.setUltimoNumero(obtenerUltimoNumeroExistente(ejercicio));
        return nueva;
    }

    private long obtenerUltimoNumeroExistente(int ejercicio) {
        String prefix = SERIE_PEDIDO_COMPRA + "-" + ejercicio + "-";
        return repository.findMaxNumeroSecuencialByPrefijo(prefix + "%", prefix.length());
    }

    @Transactional
    public PedidoCompra cambiarEstado(Long id, String nuevoEstado) {
        PedidoCompra pedido = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido de compra no encontrado: " + id));
        pedido.setEstado(nuevoEstado);
        return repository.save(pedido);
    }
}
