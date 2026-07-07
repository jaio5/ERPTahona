package alicanteweb.erp.repository;

import alicanteweb.erp.entities.TarifaCliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaClienteRepository extends JpaRepository<TarifaCliente, Long> {
    @EntityGraph(attributePaths = {"cliente", "articulo"})
    List<TarifaCliente> findByClienteId(Long clienteId);
    Optional<TarifaCliente> findByClienteIdAndArticuloId(Long clienteId, Long articuloId);
    List<TarifaCliente> findByClienteIdAndActivoTrue(Long clienteId);
}
