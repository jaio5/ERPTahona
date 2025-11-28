package alicanteweb.erp.service;

import alicanteweb.erp.entities.PedidoCompra;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import alicanteweb.erp.repository.PedidoCompraRepository;

@Service
public class PedidoCompraService {
    private final PedidoCompraRepository repository;

    public PedidoCompraService(PedidoCompraRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PedidoCompra> findAll() {
        return repository.findAll();
    }

    @Transactional
    public PedidoCompra save(PedidoCompra pedido) {
        return repository.save(pedido);
    }

    @Transactional
    public void delete(PedidoCompra pedido) {
        repository.delete(pedido);
    }

    @Transactional
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<PedidoCompra> findById(String id) {
        return repository.findById(id);
    }
}
