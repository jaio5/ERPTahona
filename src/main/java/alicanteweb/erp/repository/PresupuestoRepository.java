package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar Presupuestos
 */
@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    /**
     * Busca presupuestos por número
     */
    Optional<Presupuesto> findByNumero(String numero);

    /**
     * Busca presupuestos por cliente
     */
    @Query("SELECT p FROM Presupuesto p WHERE p.cliente.id = :clienteId ORDER BY p.fecha DESC")
    List<Presupuesto> findByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Busca presupuestos por estado
     */
    List<Presupuesto> findByEstado(String estado);

    /**
     * Busca presupuestos por rango de fechas
     */
    @Query("SELECT p FROM Presupuesto p WHERE p.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY p.fecha DESC")
    List<Presupuesto> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio,
                                          @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca presupuestos por rango de fecha de validez
     */
    List<Presupuesto> findByFechaValidezBetween(LocalDate desde, LocalDate hasta);

    /**
     * Busca presupuestos por número o nombre de cliente
     */
    @Query("SELECT p FROM Presupuesto p WHERE " +
           "LOWER(p.numero) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(p.cliente.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "ORDER BY p.fecha DESC")
    List<Presupuesto> buscar(@Param("busqueda") String busqueda);

    /**
     * Obtiene todos los presupuestos ordenados por fecha descendente
     */
    @Query("SELECT p FROM Presupuesto p ORDER BY p.fecha DESC")
    List<Presupuesto> findAllOrdenados();
}

