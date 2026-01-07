package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FacturaCompraRepository extends JpaRepository<FacturaCompra, Long> {

    List<FacturaCompra> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    List<FacturaCompra> findByProveedorId(Long proveedorId);

    List<FacturaCompra> findByPagadaFalse();
}

