package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PedidoLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoLineaRepository extends JpaRepository<PedidoLinea, Long> {
    List<PedidoLinea> findByPedido_Id(Long pedidoId);
    List<PedidoLinea> findByArticulo_Id(Long articuloId);
}

