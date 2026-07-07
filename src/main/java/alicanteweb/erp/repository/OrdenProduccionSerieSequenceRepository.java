package alicanteweb.erp.repository;

import alicanteweb.erp.entities.OrdenProduccionSerieSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdenProduccionSerieSequenceRepository extends JpaRepository<OrdenProduccionSerieSequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrdenProduccionSerieSequence> findBySerieAndEjercicio(String serie, Integer ejercicio);
}
