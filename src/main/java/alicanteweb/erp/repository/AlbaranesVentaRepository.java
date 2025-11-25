package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranesVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbaranesVentaRepository extends JpaRepository<AlbaranesVenta, Long> {
    Optional<AlbaranesVenta> findByNumero(String numero);
}

