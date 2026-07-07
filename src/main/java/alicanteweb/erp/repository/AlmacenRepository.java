package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    Optional<Almacen> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    @Query("SELECT a FROM Almacen a WHERE " +
           "LOWER(a.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.codigo) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Almacen> buscar(@Param("q") String q);
}

