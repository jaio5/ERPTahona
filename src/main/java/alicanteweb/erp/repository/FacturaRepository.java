package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumero(String numero);
    boolean existsByNumero(String numero);
    Optional<Factura> findTopByCliente_IdOrderByFechaDesc(Long clienteId);
    long countByPagadaFalse();

    List<Factura> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT DISTINCT f FROM Factura f LEFT JOIN FETCH f.cliente")
    List<Factura> findAllWithCliente();
}
