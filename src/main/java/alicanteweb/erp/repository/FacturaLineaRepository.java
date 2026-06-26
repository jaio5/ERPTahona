package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaLineaRepository extends JpaRepository<FacturaLinea, Long> {

    List<FacturaLinea> findByFactura_Id(Long facturaId);

    @Query("SELECT l FROM FacturaLinea l LEFT JOIN FETCH l.articulo WHERE l.factura.id = :facturaId")
    List<FacturaLinea> findByFacturaIdWithArticulo(@Param("facturaId") Long facturaId);

    List<FacturaLinea> findByArticulo_Id(Long articuloId);
}
