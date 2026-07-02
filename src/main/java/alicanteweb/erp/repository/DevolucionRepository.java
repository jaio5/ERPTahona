package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    List<Devolucion> findByClienteId(Long clienteId);
    List<Devolucion> findByAlbaranId(Long albaranId);
    List<Devolucion> findByFacturaId(Long facturaId);
    List<Devolucion> findByEstado(String estado);
    List<Devolucion> findByFechaBetween(LocalDate inicio, LocalDate fin);

    @Query(value = "SELECT d FROM Devolucion d JOIN FETCH d.cliente WHERE (:clienteId IS NULL OR d.cliente.id = :clienteId) AND (:estado IS NULL OR d.estado = :estado)",
           countQuery = "SELECT COUNT(d) FROM Devolucion d WHERE (:clienteId IS NULL OR d.cliente.id = :clienteId) AND (:estado IS NULL OR d.estado = :estado)")
    Page<Devolucion> findPage(Long clienteId, String estado, Pageable pageable);

    @Query("SELECT d FROM Devolucion d LEFT JOIN FETCH d.cliente WHERE " +
           "LOWER(d.numero) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.motivo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(d.cliente.nombre) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Devolucion> buscar(@Param("q") String q);

    @Query("SELECT d FROM Devolucion d LEFT JOIN FETCH d.cliente LEFT JOIN FETCH d.albaran LEFT JOIN FETCH d.factura WHERE d.id = :id")
    Optional<Devolucion> findDetailById(@Param("id") Long id);

    @Query("SELECT d FROM Devolucion d LEFT JOIN FETCH d.cliente ORDER BY d.fecha DESC")
    List<Devolucion> findAllConCliente();
}
