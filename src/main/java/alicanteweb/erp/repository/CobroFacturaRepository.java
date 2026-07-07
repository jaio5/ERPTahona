package alicanteweb.erp.repository;

import alicanteweb.erp.entities.CobroFactura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CobroFacturaRepository extends JpaRepository<CobroFactura, Long> {

    List<CobroFactura> findByFacturaIdOrderByFechaAsc(Long facturaId);
}
