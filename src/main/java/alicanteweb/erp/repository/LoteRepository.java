package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Lote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    Optional<Lote> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    @EntityGraph(attributePaths = {"articulo", "almacen"})
    List<Lote> findByArticuloId(Long articuloId);

    @EntityGraph(attributePaths = {"articulo", "almacen"})
    List<Lote> findByEstado(String estado);

    @EntityGraph(attributePaths = {"articulo", "almacen"})
    List<Lote> findByFechaCaducidadBetween(LocalDate inicio, LocalDate fin);
    long countByFechaCaducidadBetween(LocalDate inicio, LocalDate fin);
    List<Lote> findByFechaCaducidadBefore(LocalDate fecha);
    long countByFechaCaducidadBefore(LocalDate fecha);
    @EntityGraph(attributePaths = {"articulo", "almacen"})
    List<Lote> findByAlmacenId(Long almacenId);

    @EntityGraph(attributePaths = {"articulo", "almacen"})
    List<Lote> findByCodigoContainingIgnoreCase(String q);

    @Override
    @EntityGraph(attributePaths = {"articulo", "almacen"})
    List<Lote> findAll();

    @Query("SELECT l FROM Lote l LEFT JOIN FETCH l.articulo LEFT JOIN FETCH l.almacen LEFT JOIN FETCH l.ordenProduccion WHERE l.id = :id")
    Optional<Lote> findDetailById(Long id);
}
