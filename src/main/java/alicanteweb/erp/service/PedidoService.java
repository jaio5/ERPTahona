package alicanteweb.erp.service;

import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository repository;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
    }

    public List<Pedido> findAll() {
        return repository.findAll();
    }

    public Optional<Pedido> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Pedido> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    public List<Pedido> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public List<Pedido> findByClienteId(Long clienteId) {
        return repository.findByCliente_Id(clienteId);
    }

    public List<Pedido> findByNumeroOrCliente(String search) {
        // Buscar por número o por nombre de cliente
        return repository.findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(search, search);
    }

    @Transactional
    public Pedido save(Pedido p) {
        return repository.save(p);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
