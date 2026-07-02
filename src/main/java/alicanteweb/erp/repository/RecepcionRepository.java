package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Recepcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecepcionRepository extends JpaRepository<Recepcion, Long> {

    @Query("SELECT r FROM Recepcion r LEFT JOIN FETCH r.proveedor LEFT JOIN FETCH r.almacen LEFT JOIN FETCH r.pedidoCompra WHERE r.estado = :estado ORDER BY r.fecha DESC")
    List<Recepcion> findByEstado(@Param("estado") String estado);

    @Query("SELECT r FROM Recepcion r LEFT JOIN FETCH r.proveedor LEFT JOIN FETCH r.almacen LEFT JOIN FETCH r.pedidoCompra ORDER BY r.fecha DESC")
    List<Recepcion> findAllConRelaciones();

    @Query("SELECT r FROM Recepcion r LEFT JOIN FETCH r.proveedor LEFT JOIN FETCH r.almacen LEFT JOIN FETCH r.pedidoCompra WHERE r.id = :id")
    Optional<Recepcion> findDetailById(@Param("id") Long id);
}
