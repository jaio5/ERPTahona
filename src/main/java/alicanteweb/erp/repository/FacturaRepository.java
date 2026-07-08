package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumero(String numero);
    Optional<Factura> findBySerieAndNumero(String serie, String numero);
    boolean existsByNumero(String numero);
    boolean existsBySerieAndNumero(String serie, String numero);
    Optional<Factura> findTopByCliente_IdOrderByFechaDesc(Long clienteId);
    List<Factura> findByCliente_Id(Long clienteId);
    List<Factura> findByEstado(String estado);
    long countByPagadaFalse();

    List<Factura> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(f.numero, :prefixLength + 1, 4) AS integer)), 0)
        FROM Factura f
        WHERE f.serie = :serie
          AND f.numero LIKE :prefixPattern
        """)
    long findMaxNumeroSecuencialBySerieAndPrefijo(@Param("serie") String serie,
                                                  @Param("prefixPattern") String prefixPattern,
                                                  @Param("prefixLength") int prefixLength);

    @Query("SELECT DISTINCT f FROM Factura f LEFT JOIN FETCH f.cliente")
    List<Factura> findAllWithCliente();

    @Modifying
    @Transactional
    @Query("UPDATE Factura f SET f.total = :total, f.baseImponible = :base, f.totalIva = :totalIva WHERE f.id = :id")
    void updateTotales(@Param("id") Long id, @Param("total") BigDecimal total,
                       @Param("base") BigDecimal base, @Param("totalIva") BigDecimal totalIva);

    @Query(value = "SELECT f FROM Factura f LEFT JOIN FETCH f.cliente c WHERE " +
           "(:q IS NULL OR LOWER(f.numero) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.nombre) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:estado IS NULL OR f.estado = :estado) " +
           "AND (:fechaDesde IS NULL OR f.fecha >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR f.fecha <= :fechaHasta)",
           countQuery = "SELECT COUNT(f) FROM Factura f LEFT JOIN f.cliente c WHERE " +
           "(:q IS NULL OR LOWER(f.numero) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.nombre) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:estado IS NULL OR f.estado = :estado) " +
           "AND (:fechaDesde IS NULL OR f.fecha >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR f.fecha <= :fechaHasta)")
    Page<Factura> buscarPaginado(@Param("q") String q, @Param("estado") String estado,
                                 @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta,
                                 Pageable pageable);

    @Query("SELECT DISTINCT f FROM Factura f LEFT JOIN FETCH f.cliente LEFT JOIN FETCH f.facturaLineas l LEFT JOIN FETCH l.articulo WHERE f.id = :id")
    Optional<Factura> findByIdWithPdfData(Long id);

    @Query("SELECT YEAR(f.fecha), MONTH(f.fecha), COALESCE(SUM(f.total), 0) " +
           "FROM Factura f WHERE f.fecha >= :desde " +
           "GROUP BY YEAR(f.fecha), MONTH(f.fecha) " +
           "ORDER BY YEAR(f.fecha), MONTH(f.fecha)")
    List<Object[]> findVentasMensuales(@Param("desde") LocalDate desde);

    List<Factura> findByNumeroContainingIgnoreCase(String q);

    @Query("SELECT f FROM Factura f LEFT JOIN FETCH f.cliente WHERE f.fecha BETWEEN :inicio AND :fin AND f.estado = :estado")
    List<Factura> findByFechaBetweenAndEstado(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin, @Param("estado") String estado);

    @Query("SELECT f FROM Factura f WHERE f.fechaVencimiento IS NOT NULL AND f.fechaVencimiento < :hoy AND f.estado IN ('EMITIDA', 'VENCIDA')")
    List<Factura> findVencidas(@Param("hoy") LocalDate hoy);

    @Query("SELECT f FROM Factura f LEFT JOIN FETCH f.cliente " +
           "WHERE f.estado IN ('PENDIENTE', 'EMITIDA') AND f.fecha < :fechaLimite")
    List<Factura> findPendientesCobro(@Param("fechaLimite") LocalDate fechaLimite);
}
