package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumero(String numero);

    boolean existsByNumero(String numero);
    List<Pedido> findByEstado(String estado);
    List<Pedido> findByCliente_Id(Long clienteId);

    /**
     * Alias para findByCliente_Id
     */
    default List<Pedido> findByClienteId(Long clienteId) {
        return findByCliente_Id(clienteId);
    }

    List<Pedido> findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(String numero, String nombre);
    List<Pedido> findByFecha(LocalDate fecha);
}
