package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumero(String numero);
    boolean existsByNumero(String numero);
    Optional<Factura> findTopByCliente_IdOrderByFechaDesc(Long clienteId);
    long countByPagadaFalse();
}
