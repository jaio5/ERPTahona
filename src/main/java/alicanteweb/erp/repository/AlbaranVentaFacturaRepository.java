package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranVentaFactura;
import alicanteweb.erp.entities.AlbaranVentaFacturaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AlbaranVentaFacturaRepository extends JpaRepository<AlbaranVentaFactura, AlbaranVentaFacturaId> {
    boolean existsByAlbaran_Id(Long albaranId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO albaranes_venta_facturas (albaranes_ventas_id, facturas_id) VALUES (:albaranId, :facturaId)", nativeQuery = true)
    void vincular(@Param("albaranId") Long albaranId, @Param("facturaId") Long facturaId);
}

