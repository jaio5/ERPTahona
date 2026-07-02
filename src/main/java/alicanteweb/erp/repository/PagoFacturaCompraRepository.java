package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PagoFacturaCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoFacturaCompraRepository extends JpaRepository<PagoFacturaCompra, Long> {

    List<PagoFacturaCompra> findByFacturaCompraIdOrderByFechaAsc(Long facturaCompraId);
}
