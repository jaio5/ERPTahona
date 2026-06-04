package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    Optional<Lote> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Lote> findByArticuloId(Long articuloId);
    List<Lote> findByEstado(String estado);
    List<Lote> findByFechaCaducidadBetween(LocalDate inicio, LocalDate fin);
    List<Lote> findByFechaCaducidadBefore(LocalDate fecha);
    List<Lote> findByAlmacenId(Long almacenId);
}
