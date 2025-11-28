package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranVentaFactura;
import alicanteweb.erp.entities.AlbaranVentaFacturaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbaranVentaFacturaRepository extends JpaRepository<AlbaranVentaFactura, AlbaranVentaFacturaId> {
}

