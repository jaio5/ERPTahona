package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturacionEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturacionEventoRepository extends JpaRepository<FacturacionEvento, Long> {
    Optional<FacturacionEvento> findFirstByAmbitoOrderByFechaDescIdDesc(String ambito);
    List<FacturacionEvento> findByAmbitoOrderByFechaAscIdAsc(String ambito);
}
