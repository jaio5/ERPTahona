package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaCompraLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaCompraLineaRepository extends JpaRepository<FacturaCompraLinea, Long> {

    List<FacturaCompraLinea> findByFacturaCompraId(Long facturaCompraId);

    @Query("SELECT l FROM FacturaCompraLinea l LEFT JOIN FETCH l.articulo WHERE l.facturaCompra.id = :facturaCompraId")
    List<FacturaCompraLinea> findByFacturaCompraIdWithArticulo(@Param("facturaCompraId") Long facturaCompraId);

    List<FacturaCompraLinea> findByArticuloId(Long articuloId);

    void deleteByFacturaCompraId(Long facturaCompraId);
}
