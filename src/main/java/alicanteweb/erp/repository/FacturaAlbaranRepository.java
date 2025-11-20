package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaAlbaran;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaAlbaranRepository extends JpaRepository<FacturaAlbaran, Integer> {
}
