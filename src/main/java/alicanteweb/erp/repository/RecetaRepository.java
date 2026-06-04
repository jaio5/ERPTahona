package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    Optional<Receta> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Receta> findByActivo(Boolean activo);
    List<Receta> findByNombreContainingIgnoreCase(String texto);
}
