package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AppccControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppccControlRepository extends JpaRepository<AppccControl, Long> {
    List<AppccControl> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<AppccControl> findByFecha(LocalDate fecha);
    List<AppccControl> findByPuntoCritico(String puntoCritico);
    List<AppccControl> findByLoteId(Long loteId);
}
