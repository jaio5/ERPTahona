package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AsientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para AsientoContable
 */
@Repository
public interface AsientoContableRepository extends JpaRepository<AsientoContable, Long> {

    /**
     * Buscar por número de asiento
     */
    Optional<AsientoContable> findByNumero(String numero);

    /**
     * Buscar asientos por rango de fechas (Libro Diario)
     */
    @Query("SELECT a FROM AsientoContable a WHERE a.fecha >= :desde AND a.fecha <= :hasta ORDER BY a.fecha, a.numero")
    List<AsientoContable> findByFechaBetween(LocalDate desde, LocalDate hasta);

    /**
     * Buscar asientos por tipo
     */
    List<AsientoContable> findByTipoOrderByFechaDesc(String tipo);

    /**
     * Buscar asientos de una factura
     */
    List<AsientoContable> findByFactura_Id(Long facturaId);

    /**
     * Obtener último número de asiento del año
     */
    @Query("SELECT MAX(CAST(SUBSTRING(a.numero, 6) AS int)) FROM AsientoContable a WHERE a.numero LIKE :pattern")
    Integer findMaxNumeroByYear(String pattern);

    /**
     * Verificar si un asiento existe
     */
    boolean existsByNumero(String numero);

    /**
     * Buscar por concepto (like)
     */
    @Query("SELECT a FROM AsientoContable a WHERE LOWER(a.concepto) LIKE LOWER(CONCAT('%', :concepto, '%'))")
    List<AsientoContable> findByConcepto(String concepto);

    /**
     * Balance de sumas y saldos hasta una fecha dada.
     * Devuelve: [codigo_cuenta, nombre_cuenta, tipo_cuenta, suma_debe, suma_haber]
     */
    @Query("""
        SELECT l.cuenta.codigo, l.cuenta.nombre, l.cuenta.tipo,
               COALESCE(SUM(l.debe), 0), COALESCE(SUM(l.haber), 0)
        FROM LineaAsiento l
        JOIN l.asiento a
        WHERE a.fecha <= :hasta
        GROUP BY l.cuenta.codigo, l.cuenta.nombre, l.cuenta.tipo
        ORDER BY l.cuenta.codigo
        """)
    List<Object[]> calcularBalanceHasta(java.time.LocalDate hasta);

    /**
     * Buscar asientos de apertura
     */
    @Query("SELECT a FROM AsientoContable a WHERE a.tipo = 'APERTURA' ORDER BY a.fecha DESC")
    List<AsientoContable> findByAsientoAperturaTrue();

    /**
     * Buscar asientos de cierre
     */
    @Query("SELECT a FROM AsientoContable a WHERE a.tipo = 'CIERRE' ORDER BY a.fecha DESC")
    List<AsientoContable> findByAsientoCierreTrue();
}

