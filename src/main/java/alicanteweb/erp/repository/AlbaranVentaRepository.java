package alicanteweb.erp.repository;

import alicanteweb.erp.entities.AlbaranVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbaranVentaRepository extends JpaRepository<AlbaranVenta, Long> {
    Optional<AlbaranVenta> findByNumero(String numero);
}

