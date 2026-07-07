package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RemesaSepa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RemesaSepaRepository extends JpaRepository<RemesaSepa, Long> {

    List<RemesaSepa> findAllByOrderByIdDesc();

    /** Facturas ya incluidas en alguna remesa viva (no anulada). */
    @Query("SELECT l.factura.id FROM RemesaSepaLinea l WHERE l.remesa.estado <> 'ANULADA'")
    List<Long> facturasEnRemesasVivas();
}
