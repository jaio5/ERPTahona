package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PedidoLinea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoLineaRepository extends JpaRepository<PedidoLinea, Long> {

    List<PedidoLinea> findByPedido_Id(Long pedidoId);

    @Query("SELECT l FROM PedidoLinea l LEFT JOIN FETCH l.articulo WHERE l.pedido.id = :pedidoId")
    List<PedidoLinea> findByPedidoIdWithArticulo(@Param("pedidoId") Long pedidoId);

    List<PedidoLinea> findByArticulo_Id(Long articuloId);
}
