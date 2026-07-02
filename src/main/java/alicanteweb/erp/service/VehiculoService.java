package alicanteweb.erp.service;

import alicanteweb.erp.entities.Vehiculo;
import alicanteweb.erp.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VehiculoService {

    private final VehiculoRepository repository;

    public VehiculoService(VehiculoRepository repository) {
        this.repository = repository;
    }

    public List<Vehiculo> findAll() {
        return repository.findAll();
    }

    public Optional<Vehiculo> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Vehiculo> findByMatricula(String matricula) {
        return repository.findByMatricula(matricula);
    }

    public List<Vehiculo> findByActivo(boolean activo) {
        return repository.findByActivo(activo);
    }

    public List<Vehiculo> buscar(String q) {
        return repository.buscar(q);
    }

    @Transactional
    public Vehiculo save(Vehiculo vehiculo) {
        if (vehiculo == null) throw new IllegalArgumentException("Vehículo nulo");
        if (vehiculo.getMatricula() == null || vehiculo.getMatricula().isBlank()) {
            throw new IllegalArgumentException("La matrícula es obligatoria");
        }
        var opt = repository.findByMatricula(vehiculo.getMatricula().trim());
        if (opt.isPresent() && (vehiculo.getId() == null || !opt.get().getId().equals(vehiculo.getId()))) {
            throw new IllegalArgumentException("Ya existe un vehículo con matrícula: " + vehiculo.getMatricula());
        }
        return repository.save(vehiculo);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Vehiculo vehiculo = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado: " + id));
        vehiculo.setActivo(false);
        repository.save(vehiculo);
    }
}
