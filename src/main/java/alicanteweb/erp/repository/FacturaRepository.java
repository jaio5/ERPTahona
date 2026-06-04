package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT DISTINCT f FROM Factura f LEFT JOIN FETCH f.cliente LEFT JOIN FETCH f.facturaLineas l LEFT JOIN FETCH l.articulo WHERE f.id = :id")
    Optional<Factura> findByIdWithPdfData(Long id);
}
