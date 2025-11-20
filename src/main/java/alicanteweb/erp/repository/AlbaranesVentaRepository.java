package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranVentaLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbaranesVentaRepository extends JpaRepository<AlbaranVentaLinea, Integer> {
}
