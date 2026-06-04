package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    List<Devolucion> findByClienteId(Long clienteId);
    List<Devolucion> findByAlbaranId(Long albaranId);
    List<Devolucion> findByFacturaId(Long facturaId);
    List<Devolucion> findByEstado(String estado);
    List<Devolucion> findByFechaBetween(LocalDate inicio, LocalDate fin);
}
