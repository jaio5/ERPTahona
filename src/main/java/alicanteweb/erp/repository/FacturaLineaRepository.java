package alicanteweb.erp.repository;

import alicanteweb.erp.entities.FacturaLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaLineaRepository extends JpaRepository<FacturaLinea, Long> {
    List<FacturaLinea> findByFactura_Id(Long facturaId);
    List<FacturaLinea> findByArticulo_Id(Long articuloId);
}

