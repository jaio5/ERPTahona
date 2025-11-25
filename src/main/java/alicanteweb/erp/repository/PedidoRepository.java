package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumero(String numero);
    List<Pedido> findByEstado(String estado);
    List<Pedido> findByCliente_Id(Long clienteId);
    List<Pedido> findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(String numero, String nombre);
}
