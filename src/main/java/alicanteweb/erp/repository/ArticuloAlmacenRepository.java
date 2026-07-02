package alicanteweb.erp.repository;

import alicanteweb.erp.entities.ArticuloAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticuloAlmacenRepository extends JpaRepository<ArticuloAlmacen, Long> {

    Optional<ArticuloAlmacen> findByArticuloIdAndAlmacenId(Long articuloId, Long almacenId);

    List<ArticuloAlmacen> findByArticuloId(Long articuloId);

    @Query("SELECT aa FROM ArticuloAlmacen aa JOIN FETCH aa.almacen WHERE aa.articulo.id = :articuloId")
    List<ArticuloAlmacen> findByArticuloIdWithAlmacen(@Param("articuloId") Long articuloId);
}
