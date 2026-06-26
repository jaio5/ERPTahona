package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PresupuestoLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresupuestoLineaRepository extends JpaRepository<PresupuestoLinea, Long> {

    List<PresupuestoLinea> findByPresupuestoId(Long presupuestoId);

    @Query("SELECT l FROM PresupuestoLinea l LEFT JOIN FETCH l.articulo WHERE l.presupuesto.id = :presupuestoId")
    List<PresupuestoLinea> findByPresupuestoIdWithArticulo(@Param("presupuestoId") Long presupuestoId);

    List<PresupuestoLinea> findByArticuloId(Long articuloId);

    void deleteByPresupuestoId(Long presupuestoId);
}
