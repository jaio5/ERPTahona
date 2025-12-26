package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RgpdConsentimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad RgpdConsentimiento
 */
@Repository
public interface RgpdConsentimientoRepository extends JpaRepository<RgpdConsentimiento, Long> {

    List<RgpdConsentimiento> findByClienteId(Long clienteId);

    List<RgpdConsentimiento> findByClienteIdAndActivoTrue(Long clienteId);

    Optional<RgpdConsentimiento> findFirstByClienteIdAndTipoConsentimientoOrderByFechaConsentimientoDesc(
        Long clienteId, String tipoConsentimiento);

    List<RgpdConsentimiento> findByEmail(String email);

    List<RgpdConsentimiento> findByOtorgadoTrueAndActivoTrue();

    @Query("SELECT c FROM RgpdConsentimiento c WHERE c.fechaRevocacion IS NOT NULL")
    List<RgpdConsentimiento> findRevocados();

    List<RgpdConsentimiento> findByTipoConsentimientoAndActivoTrue(String tipoConsentimiento);

    List<RgpdConsentimiento> findByFechaConsentimientoBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM RgpdConsentimiento c " +
           "WHERE c.cliente.id = :clienteId AND c.tipoConsentimiento = :tipo " +
           "AND c.otorgado = true AND c.activo = true AND c.fechaRevocacion IS NULL")
    boolean tieneConsentimientoActivo(@Param("clienteId") Long clienteId, @Param("tipo") String tipo);

    long countByActivoTrue();

    long countByTipoConsentimientoAndActivoTrue(String tipoConsentimiento);
}


