package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PedidoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para PedidoCompra.
 */
@Repository
public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {

    Optional<PedidoCompra> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<PedidoCompra> findByEstado(String estado);

    @Query("""
        SELECT COALESCE(MAX(CAST(SUBSTRING(p.numero, :prefixLength + 1, 5) AS integer)), 0)
        FROM PedidoCompra p
        WHERE p.numero LIKE :prefixPattern
        """)
    long findMaxNumeroSecuencialByPrefijo(@Param("prefixPattern") String prefixPattern,
                                          @Param("prefixLength") int prefixLength);

    @Query("SELECT p FROM PedidoCompra p LEFT JOIN FETCH p.proveedor ORDER BY p.fecha DESC")
    List<PedidoCompra> findAllWithProveedor();

    @Query("""
        SELECT DISTINCT p
        FROM PedidoCompra p
        LEFT JOIN FETCH p.proveedor
        LEFT JOIN FETCH p.lineas l
        LEFT JOIN FETCH l.articulo
        WHERE p.id = :id
        """)
    Optional<PedidoCompra> findByIdWithLineas(@Param("id") Long id);
}

