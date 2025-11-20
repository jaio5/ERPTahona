package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaAlbaranId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaAlbaranIdRepository extends JpaRepository<FacturaAlbaranId, Integer> {
}
