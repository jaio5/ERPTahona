package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaAlbaran;
import alicanteweb.erp.entities.FacturaAlbaranId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaAlbaranRepository extends JpaRepository<FacturaAlbaran, FacturaAlbaranId> {
    List<FacturaAlbaran> findByFactura_Id(Long facturaId);
    List<FacturaAlbaran> findByAlbaran_Id(Long albaranId);
}

