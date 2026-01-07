package alicanteweb.erp.service;

import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de Pedidos de Venta
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PedidoVentaService {
    private static final Logger log = LoggerFactory.getLogger(PedidoVentaService.class);

    private final PedidoRepository pedidoRepository;

    /**
     * Obtiene todos los pedidos
     */
    public List<Pedido> obtenerTodos() {
        log.debug("Obteniendo todos los pedidos");
        return pedidoRepository.findAll();
    }

    /**
     * Obtiene un pedido por ID
     */
    public Optional<Pedido> obtenerPorId(Long id) {
        log.debug("Obteniendo pedido con ID: {}", id);
        return pedidoRepository.findById(id);
    }

    /**
     * Guarda un pedido
     */
    public Pedido guardar(Pedido pedido) {
        log.info("Guardando pedido: {}", pedido.getId());
        return pedidoRepository.save(pedido);
    }

    /**
     * Actualiza un pedido
     */
    public Pedido actualizar(Long id, Pedido pedidoActualizado) {
        log.info("Actualizando pedido con ID: {}", id);
        return pedidoRepository.findById(id)
            .map(pedido -> {
                pedido.setFecha(pedidoActualizado.getFecha());
                return pedidoRepository.save(pedido);
            })
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Elimina un pedido
     */
    public void eliminar(Long id) {
        log.info("Eliminando pedido con ID: {}", id);
        pedidoRepository.deleteById(id);
    }
}

