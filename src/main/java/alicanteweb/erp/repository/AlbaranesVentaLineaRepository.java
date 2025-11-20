package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranesVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbaranesVentaLineaRepository extends JpaRepository<AlbaranesVenta, Integer> {
}
