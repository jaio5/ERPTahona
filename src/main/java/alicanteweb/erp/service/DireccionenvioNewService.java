package alicanteweb.erp.service;

import alicanteweb.erp.entities.DireccionenvioNew;
import alicanteweb.erp.repository.DireccionenvioNewRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class DireccionenvioNewService {

    //instancia del repositorio
    private final DireccionenvioNewRepository repository;

    //constructor para la clase
    public DireccionenvioNewService(DireccionenvioNewRepository repository) {
        this.repository = repository;
    }

    //devolver la lista con todas las direcciones de envio
    public List<DireccionenvioNew> findAll() {
        return repository.findAll();
    }

    //buscar por id
    public Optional<DireccionenvioNew> findById(Long id) {
        return repository.findById(id);
    }

    //buscar por codigo de direccion (devuelve Optional)
    public Optional<DireccionenvioNew> findByCodigoDireccion(Integer codigoDireccion) {
        if (codigoDireccion == null) return Optional.empty();
        return Optional.ofNullable(repository.findByCodigoDireccion(codigoDireccion));
    }

    //compatibilidad: buscar por codigo puesto en texto (p. ej. desde UI)
    public Optional<DireccionenvioNew> findByCodigoPostal(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.isBlank()) return Optional.empty();
        try {
            Integer codigo = Integer.valueOf(codigoPostal.trim());
            return findByCodigoDireccion(codigo);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    //buscar por cliente id
    public List<DireccionenvioNew> findByClienteId(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    //buscar por poblacion (contiene, ignore case)
    public List<DireccionenvioNew> searchByPoblacion(String texto) {
        if (texto == null) return List.of();
        return repository.findByPoblacionContainingIgnoreCase(texto);
    }

    //guardar una nueva direccion de envio
    @Transactional
    public DireccionenvioNew save(DireccionenvioNew direccionenvio) {
        return repository.save(direccionenvio);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }


}