package alicanteweb.erp.repository;

import alicanteweb.erp.entities.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {
    Optional<OrdenProduccion> findByNumero(String numero);
    List<OrdenProduccion> findByEstado(String estado);
    long countByEstado(String estado);
    @Query("SELECT o FROM OrdenProduccion o LEFT JOIN FETCH o.receta WHERE o.fecha BETWEEN :inicio AND :fin")
    List<OrdenProduccion> findByFechaBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
    List<OrdenProduccion> findByRecetaId(Long recetaId);
    List<OrdenProduccion> findByArticuloId(Long articuloId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(o.numero, LENGTH(o.numero) - 3, 4) AS int)), 0) " +
           "FROM OrdenProduccion o WHERE o.numero LIKE CONCAT(?1, '-%')")
    int findMaxNumeroSecuencialBySerie(String prefijo);

    @Query("SELECT o.estado, COUNT(o) FROM OrdenProduccion o WHERE o.estado IS NOT NULL GROUP BY o.estado")
    List<Object[]> countByEstado();

    @Query("SELECT o FROM OrdenProduccion o LEFT JOIN FETCH o.receta LEFT JOIN FETCH o.articulo LEFT JOIN FETCH o.almacen LEFT JOIN FETCH o.usuario WHERE o.id = :id")
    Optional<OrdenProduccion> findDetailById(Long id);

    @Query(value = "SELECT o FROM OrdenProduccion o LEFT JOIN FETCH o.receta LEFT JOIN FETCH o.articulo WHERE (:estado IS NULL OR o.estado = :estado) AND (:recetaId IS NULL OR o.receta.id = :recetaId)",
           countQuery = "SELECT COUNT(o) FROM OrdenProduccion o WHERE (:estado IS NULL OR o.estado = :estado) AND (:recetaId IS NULL OR o.receta.id = :recetaId)")
    Page<OrdenProduccion> findPage(String estado, Long recetaId, Pageable pageable);
}
