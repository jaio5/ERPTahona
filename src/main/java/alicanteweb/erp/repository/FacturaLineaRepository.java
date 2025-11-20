package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaLineaRepository extends JpaRepository<FacturaLinea, Integer> {
}
