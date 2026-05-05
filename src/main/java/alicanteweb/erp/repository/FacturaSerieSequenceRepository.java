package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaSerieSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaSerieSequenceRepository extends JpaRepository<FacturaSerieSequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FacturaSerieSequence> findBySerieAndEjercicio(String serie, Integer ejercicio);
}
