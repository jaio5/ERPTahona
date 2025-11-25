package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranesVentaFactura;
import alicanteweb.erp.entities.AlbaranesVentaFacturaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbaranesVentaFacturaRepository extends JpaRepository<AlbaranesVentaFactura, AlbaranesVentaFacturaId> {
}

