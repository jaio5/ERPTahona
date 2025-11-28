package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    Optional<Articulo> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Articulo> findByDescripcionContainingIgnoreCase(String text);
}
