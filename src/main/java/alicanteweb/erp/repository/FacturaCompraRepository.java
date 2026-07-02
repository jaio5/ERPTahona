package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar Facturas de Compra
 */
@Repository
public interface FacturaCompraRepository extends JpaRepository<FacturaCompra, Long> {

    /**
     * Busca facturas por número
     */
    Optional<FacturaCompra> findByNumero(String numero);

    /**
     * Busca facturas por proveedor
     */
    @Query("SELECT f FROM FacturaCompra f WHERE f.proveedor.id = :proveedorId ORDER BY f.fecha DESC")
    List<FacturaCompra> findByProveedorId(@Param("proveedorId") Long proveedorId);

    /**
     * Busca facturas por estado
     */
    List<FacturaCompra> findByEstado(String estado);

    /**
     * Busca facturas por rango de fechas
     */
    @Query("SELECT f FROM FacturaCompra f LEFT JOIN FETCH f.proveedor WHERE f.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY f.fecha DESC")
    List<FacturaCompra> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio,
                                            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca facturas pendientes de pago
     */
    @Query("SELECT f FROM FacturaCompra f WHERE f.pagada = false ORDER BY f.fechaVencimiento ASC")
    List<FacturaCompra> findPendientesPago();

    /**
     * Busca facturas por número o nombre de proveedor
     */
    @Query("SELECT f FROM FacturaCompra f WHERE " +
           "LOWER(f.numero) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(f.proveedor.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "ORDER BY f.fecha DESC")
    List<FacturaCompra> buscar(@Param("busqueda") String busqueda);

    /**
     * Obtiene todas las facturas ordenadas por fecha descendente
     */
    @Query("SELECT f FROM FacturaCompra f ORDER BY f.fecha DESC")
    List<FacturaCompra> findAllOrdenadas();

    @Query("SELECT f FROM FacturaCompra f LEFT JOIN FETCH f.proveedor WHERE "
         + "(:q IS NULL OR LOWER(f.numero) LIKE LOWER(CONCAT('%',:q,'%')) "
         + "OR LOWER(f.proveedor.nombre) LIKE LOWER(CONCAT('%',:q,'%'))) "
         + "AND (:estado IS NULL OR f.estado = :estado) "
         + "ORDER BY f.fecha DESC")
    Page<FacturaCompra> findPage(@Param("q") String q, @Param("estado") String estado, Pageable pageable);

    @Query("SELECT f FROM FacturaCompra f LEFT JOIN FETCH f.proveedor WHERE f.id = :id")
    Optional<FacturaCompra> findDetailById(@Param("id") Long id);
}

