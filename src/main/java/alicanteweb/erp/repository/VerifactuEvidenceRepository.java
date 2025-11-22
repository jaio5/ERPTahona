package alicanteweb.erp.repository;

import alicanteweb.erp.entities.VerifactuEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerifactuEvidenceRepository extends JpaRepository<VerifactuEvidence, Long> {
    Optional<VerifactuEvidence> findByFacturaId(String facturaId);
    boolean existsByFacturaId(String facturaId);
}
