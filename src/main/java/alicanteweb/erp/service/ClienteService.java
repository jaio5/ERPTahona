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
        if (cliente == null) throw new IllegalArgumentException("Cliente nulo");

        String codigo = cliente.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del cliente es obligatorio");
        }

        // Comprobar duplicados por código
        var optCodigo = repository.findByCodigo(codigo.trim());
        if (optCodigo.isPresent()) {
            var existente = optCodigo.get();
            if (cliente.getId() == null || !existente.getId().equals(cliente.getId())) {
                throw new IllegalArgumentException("Ya existe un cliente con el código: " + codigo);
            }
        }

        // Comprobar duplicados por CIF (si presente)
        if (cliente.getCif() != null && !cliente.getCif().trim().isEmpty()) {
            var optCif = repository.findByCif(cliente.getCif().trim());
            if (optCif.isPresent()) {
                var existente = optCif.get();
                if (cliente.getId() == null || !existente.getId().equals(cliente.getId())) {
                    throw new IllegalArgumentException("Ya existe un cliente con el CIF: " + cliente.getCif());
                }
            }
        }

        return repository.save(cliente);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
        cliente.setActivo(false);
        repository.save(cliente);
    }

    @Transactional
    public void activar(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con id: " + id));
        cliente.setActivo(true);
        repository.save(cliente);
    }
}
