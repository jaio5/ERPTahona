package alicanteweb.erp.service;

import alicanteweb.erp.entities.PedidoLinea;
import alicanteweb.erp.repository.PedidoLineaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PedidoLineaService {

    private final PedidoLineaRepository repository;

    public PedidoLineaService(PedidoLineaRepository repository) {
        this.repository = repository;
    }

    public List<PedidoLinea> findAll() {
        return repository.findAll();
    }

    public Optional<PedidoLinea> findById(Long id) {
        return repository.findById(id);
    }

    public List<PedidoLinea> findByPedidoId(Long pedidoId) {
        return repository.findByPedido_Id(pedidoId);
    }

    public List<PedidoLinea> findByArticuloId(Long articuloId) {
        return repository.findByArticulo_Id(articuloId);
    }

    @Transactional
    public PedidoLinea save(PedidoLinea linea) {
        return repository.save(linea);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

