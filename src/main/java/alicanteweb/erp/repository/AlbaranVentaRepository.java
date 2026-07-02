package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlbaranVentaRepository extends JpaRepository<AlbaranVenta, Long> {
    Optional<AlbaranVenta> findByNumero(String numero);
    boolean existsByNumero(String numero);

    @Query("SELECT DISTINCT a FROM AlbaranVenta a LEFT JOIN FETCH a.cliente LEFT JOIN FETCH a.almacen")
    List<AlbaranVenta> findAllWithRelations();

    @Query("SELECT DISTINCT a FROM AlbaranVenta a LEFT JOIN FETCH a.cliente LEFT JOIN FETCH a.almacen LEFT JOIN FETCH a.albaranVentaLineas l LEFT JOIN FETCH l.articulo WHERE a.id = :id")
    Optional<AlbaranVenta> findByIdWithPdfData(Long id);

    List<AlbaranVenta> findByCliente_Id(Long clienteId);

    @Query("""
        SELECT DISTINCT a
        FROM AlbaranVenta a
        LEFT JOIN FETCH a.cliente
        WHERE a.cliente.id = :clienteId
          AND NOT EXISTS (
              SELECT 1
              FROM AlbaranVentaFactura avf
              WHERE avf.albaran = a
          )
        """)
    List<AlbaranVenta> findPendientesFacturarByClienteId(Long clienteId);

    @Query("""
        SELECT DISTINCT a
        FROM AlbaranVenta a
        LEFT JOIN FETCH a.cliente
        WHERE NOT EXISTS (
              SELECT 1
              FROM AlbaranVentaFactura avf
              WHERE avf.albaran = a
          )
        """)
    List<AlbaranVenta> findPendientesFacturar();

    /**
     * Alias para findByCliente_Id
     */
    default List<AlbaranVenta> findByClienteId(Long clienteId) {
        return findByCliente_Id(clienteId);
    }

    @Query("SELECT a FROM AlbaranVenta a LEFT JOIN FETCH a.cliente WHERE a.fecha = :fecha")
    List<AlbaranVenta> findByFecha(@Param("fecha") java.time.LocalDate fecha);

    @Query("SELECT DISTINCT a FROM AlbaranVenta a LEFT JOIN FETCH a.albaranVentaLineas l LEFT JOIN FETCH l.articulo WHERE a.cliente.id = :clienteId AND a.estado <> 'ANULADO' ORDER BY a.fecha DESC, a.id DESC")
    List<AlbaranVenta> findTop10ByClienteIdWithLineas(@Param("clienteId") Long clienteId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE AlbaranVenta a SET a.total = :total WHERE a.id = :id")
    void updateTotal(@Param("id") Long id, @Param("total") BigDecimal total);

    @Query(value = "SELECT a FROM AlbaranVenta a LEFT JOIN FETCH a.cliente c WHERE " +
           "(:q IS NULL OR LOWER(a.numero) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.nombre) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:estado IS NULL OR a.estado = :estado)",
           countQuery = "SELECT COUNT(a) FROM AlbaranVenta a LEFT JOIN a.cliente c WHERE " +
           "(:q IS NULL OR LOWER(a.numero) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.nombre) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:estado IS NULL OR a.estado = :estado)")
    Page<AlbaranVenta> buscarPaginado(@Param("q") String q, @Param("estado") String estado, Pageable pageable);
}

