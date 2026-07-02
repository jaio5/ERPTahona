package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByMatricula(String matricula);
    boolean existsByMatricula(String matricula);
    List<Vehiculo> findByActivo(Boolean activo);

    @Query("SELECT v FROM Vehiculo v WHERE " +
           "LOWER(v.matricula) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(v.marca) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(v.modelo) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Vehiculo> buscar(@Param("q") String q);
}
