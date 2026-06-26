package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AppccControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppccControlRepository extends JpaRepository<AppccControl, Long> {
    List<AppccControl> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<AppccControl> findByFecha(LocalDate fecha);
    List<AppccControl> findByPuntoCritico(String puntoCritico);
    List<AppccControl> findByLoteId(Long loteId);

    @Query("SELECT a FROM AppccControl a WHERE " +
           "LOWER(a.puntoCritico) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.responsable) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<AppccControl> buscar(@Param("q") String q);
}
