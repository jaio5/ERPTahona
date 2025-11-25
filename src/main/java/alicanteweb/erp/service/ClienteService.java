package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> findAll() {
        return repository.findAll();
    }

    public Optional<Cliente> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Cliente> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public List<Cliente> searchByNombre(String texto) {
        return repository.findByNombreContainingIgnoreCase(texto);
    }

    public boolean existsByCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    @Transactional
    public Cliente save(Cliente cliente) {
        return repository.save(cliente);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

