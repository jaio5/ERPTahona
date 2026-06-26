package alicanteweb.erp.repository;

import alicanteweb.erp.entities.VerifactuEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface VerifactuEvidenceRepository extends JpaRepository<VerifactuEvidence, Long> {
    Optional<VerifactuEvidence> findByFacturaId(String facturaId);
    Optional<VerifactuEvidence> findFirstByFacturaIdOrderByFechaGeneracionRegistroDescIdDesc(String facturaId);
    List<VerifactuEvidence> findAllByFacturaIdOrderByFechaGeneracionRegistroAscIdAsc(String facturaId);
    Optional<VerifactuEvidence> findByHash(String hash);
    List<VerifactuEvidence> findBySerieContainingIgnoreCase(String serie);

    // Métodos para la cadena de bloques
    Optional<VerifactuEvidence> findFirstBySerieOrderByFechaEmisionDesc(String serie);
    Optional<VerifactuEvidence> findFirstBySerieOrderByFechaGeneracionRegistroDescIdDesc(String serie);
    List<VerifactuEvidence> findAllBySerieOrderByFechaEmisionAsc(String serie);

    // Métodos para estados
    List<VerifactuEvidence> findByEstado(String estado);
    List<VerifactuEvidence> findAllByOrderByIdAsc();
    long countByEstado(String estado);

    @Query("""
        SELECT e FROM VerifactuEvidence e
        WHERE (:estado IS NULL OR e.estado = :estado)
          AND (:q IS NULL OR LOWER(COALESCE(e.serie, '')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(e.numero, '')) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(e.facturaId, '')) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<VerifactuEvidence> findPage(String estado, String q, Pageable pageable);
}

