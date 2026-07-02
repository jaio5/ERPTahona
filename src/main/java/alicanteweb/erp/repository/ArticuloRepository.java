package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Articulo;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Articulo a WHERE a.id = :id")
    Optional<Articulo> findByIdForUpdate(@Param("id") Long id);

    Optional<Articulo> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Articulo> findByDescripcionContainingIgnoreCase(String text);
    List<Articulo> findByActivo(Boolean activo);

    @Query("SELECT a FROM Articulo a WHERE :q IS NULL OR " +
           "LOWER(a.nombre) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "LOWER(COALESCE(a.codigo,'')) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Articulo> buscarPaginado(@Param("q") String q, Pageable pageable);

    @Query("""
        SELECT a FROM Articulo a
        WHERE (:activo IS NULL OR a.activo = :activo)
          AND (:q IS NULL
               OR LOWER(a.nombre) LIKE LOWER(CONCAT('%',:q,'%'))
               OR LOWER(COALESCE(a.codigo,''))       LIKE LOWER(CONCAT('%',:q,'%'))
               OR LOWER(COALESCE(a.descripcion,''))  LIKE LOWER(CONCAT('%',:q,'%'))
               OR LOWER(COALESCE(a.categoria,''))    LIKE LOWER(CONCAT('%',:q,'%')))
        ORDER BY a.nombre
        """)
    Page<Articulo> buscarParaApi(@Param("q") String q, @Param("activo") Boolean activo, Pageable pageable);

    @Query("SELECT a FROM Articulo a WHERE a.stock IS NOT NULL AND a.stockMinimo IS NOT NULL AND a.stock < a.stockMinimo")
    List<Articulo> findConStockBajo();

    @Query("""
        SELECT a.id AS id, a.codigo AS codigo, a.nombre AS nombre,
               a.stock AS stock, a.coste AS coste,
               (a.stock * COALESCE(a.coste, 0)) AS valor
        FROM Articulo a
        WHERE a.stock IS NOT NULL AND a.stock > 0
        ORDER BY a.nombre
        """)
    List<ValoracionInventario> findValoracionInventario();

    @Query("""
        SELECT COALESCE(SUM(a.stock * COALESCE(a.coste, 0)), 0)
        FROM Articulo a
        WHERE a.stock IS NOT NULL AND a.stock > 0
        """)
    BigDecimal sumValorInventario();

    interface ValoracionInventario {
        Long getId();
        String getCodigo();
        String getNombre();
        BigDecimal getStock();
        BigDecimal getCoste();
        BigDecimal getValor();
    }
}
