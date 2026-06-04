package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByMatricula(String matricula);
    boolean existsByMatricula(String matricula);
    List<Vehiculo> findByActivo(Boolean activo);
}
