package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PresupuestoLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gestionar líneas de presupuestos
 */
@Repository
public interface PresupuestoLineaRepository extends JpaRepository<PresupuestoLinea, Long> {

    /**
     * Busca líneas por presupuesto
     */
    List<PresupuestoLinea> findByPresupuestoId(Long presupuestoId);

    /**
     * Busca líneas por artículo
     */
    List<PresupuestoLinea> findByArticuloId(Long articuloId);

    /**
     * Elimina todas las líneas de un presupuesto
     */
    void deleteByPresupuestoId(Long presupuestoId);
}

