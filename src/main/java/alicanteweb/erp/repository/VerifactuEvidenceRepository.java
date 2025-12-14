package alicanteweb.erp.repository;

import alicanteweb.erp.entities.VerifactuEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VerifactuEvidenceRepository extends JpaRepository<VerifactuEvidence, Long> {
    Optional<VerifactuEvidence> findByFacturaId(String facturaId);
    Optional<VerifactuEvidence> findByHash(String hash);
    List<VerifactuEvidence> findBySerieContainingIgnoreCase(String serie);

    // Métodos para la cadena de bloques
    Optional<VerifactuEvidence> findFirstBySerieOrderByFechaEmisionDesc(String serie);
    List<VerifactuEvidence> findAllBySerieOrderByFechaEmisionAsc(String serie);

    // Métodos para estados
    List<VerifactuEvidence> findByEstado(String estado);
    long countByEstado(String estado);
}

