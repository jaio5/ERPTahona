package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Horneada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HorneadaRepository extends JpaRepository<Horneada, Long> {
    List<Horneada> findByOrdenProduccionId(Long ordenProduccionId);
    List<Horneada> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<Horneada> findByFecha(LocalDate fecha);

    @Query(value = "SELECT h FROM Horneada h LEFT JOIN FETCH h.ordenProduccion WHERE (:ordenId IS NULL OR h.ordenProduccion.id = :ordenId) AND (:fecha IS NULL OR h.fecha = :fecha)",
           countQuery = "SELECT COUNT(h) FROM Horneada h WHERE (:ordenId IS NULL OR h.ordenProduccion.id = :ordenId) AND (:fecha IS NULL OR h.fecha = :fecha)")
    Page<Horneada> findPage(Long ordenId, LocalDate fecha, Pageable pageable);

    @Query("SELECT h FROM Horneada h WHERE " +
           "LOWER(h.tipoHorneada) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(h.resultado) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Horneada> buscar(@Param("q") String q);
}
