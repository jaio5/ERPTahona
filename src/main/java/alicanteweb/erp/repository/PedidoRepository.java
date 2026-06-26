package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumero(String numero);

    boolean existsByNumero(String numero);

    @EntityGraph(attributePaths = {"cliente"})
    List<Pedido> findByEstado(String estado);

    @EntityGraph(attributePaths = {"cliente"})
    Page<Pedido> findByEstado(String estado, Pageable pageable);

    long countByEstado(String estado);
    List<Pedido> findByCliente_Id(Long clienteId);

    List<Pedido> findTop10ByCliente_IdOrderByFechaDescIdDesc(Long clienteId);

    default List<Pedido> findByClienteId(Long clienteId) {
        return findByCliente_Id(clienteId);
    }

    @EntityGraph(attributePaths = {"cliente"})
    List<Pedido> findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(String numero, String nombre);

    @EntityGraph(attributePaths = {"cliente"})
    Page<Pedido> findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase(String numero, String nombre, Pageable pageable);

    List<Pedido> findByFecha(LocalDate fecha);
    List<Pedido> findByFechaBetween(LocalDate inicio, LocalDate fin);

    @Override
    @EntityGraph(attributePaths = {"cliente"})
    Page<Pedido> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"cliente", "pedidoLineas", "pedidoLineas.articulo"})
    Optional<Pedido> findById(Long id);
}
