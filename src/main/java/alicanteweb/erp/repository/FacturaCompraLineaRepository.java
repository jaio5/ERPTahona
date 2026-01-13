package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaCompraLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gestionar líneas de facturas de compra
 */
@Repository
public interface FacturaCompraLineaRepository extends JpaRepository<FacturaCompraLinea, Long> {

    /**
     * Busca líneas por factura de compra
     */
    List<FacturaCompraLinea> findByFacturaCompraId(Long facturaCompraId);

    /**
     * Busca líneas por artículo
     */
    List<FacturaCompraLinea> findByArticuloId(Long articuloId);

    /**
     * Elimina todas las líneas de una factura de compra
     */
    void deleteByFacturaCompraId(Long facturaCompraId);
}

