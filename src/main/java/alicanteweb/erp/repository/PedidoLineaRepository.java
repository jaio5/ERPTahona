package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PedidoLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoLineaRepository extends JpaRepository<PedidoLinea, Integer> {
}
