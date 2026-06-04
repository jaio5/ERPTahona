package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Horneada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HorneadaRepository extends JpaRepository<Horneada, Long> {
    List<Horneada> findByOrdenProduccionId(Long ordenProduccionId);
    List<Horneada> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<Horneada> findByFecha(LocalDate fecha);
}
