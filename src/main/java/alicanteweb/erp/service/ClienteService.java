package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.ClienteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    public long count() {
        return repository.count();
    }

    public Optional<Cliente> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Cliente> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    public Optional<Cliente> findByCif(String cif) {
        if (cif == null || cif.trim().isEmpty()) {
            return Optional.empty();
        }
        return repository.findByCifIgnoreCase(cif.trim());
    }

    public List<Cliente> searchByNombre(String texto) {
        return repository.findByNombreContainingIgnoreCase(texto);
    }

    public Optional<Cliente> findByNombreExacto(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return Optional.empty();
        }
        return repository.findFirstByNombreIgnoreCase(nombre.trim());
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

        // Si es alta nueva y ya existe código -> duplicado
        if (cliente.getId() == null) {
            if (existsByCodigo(codigo.trim())) {
                throw new IllegalArgumentException("Ya existe un cliente con el código: " + codigo);
            }
        } else {
            // Comprobar duplicados por código para actualizaciones
            var optCodigo = repository.findByCodigo(codigo.trim());
            if (optCodigo.isPresent()) {
                var existente = optCodigo.get();
                if (!existente.getId().equals(cliente.getId())) {
                    throw new IllegalArgumentException("Ya existe un cliente con el código: " + codigo);
                }
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

    public Page<Cliente> buscarPaginado(String q, Pageable pageable) {
        return repository.buscarPaginado(
            (q != null && !q.isBlank()) ? q : null,
            pageable);
    }

    public List<Cliente> buscarParaApi(String q, Pageable pageable) {
        String normalized = q != null && !q.isBlank() ? q.trim() : null;
        return repository.buscarParaApi(normalized, pageable);
    }
}
