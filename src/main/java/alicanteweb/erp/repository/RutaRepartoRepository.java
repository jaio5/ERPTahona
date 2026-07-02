package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RutaReparto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepartoRepository extends JpaRepository<RutaReparto, Long> {
    List<RutaReparto> findByActivo(Boolean activo);
    List<RutaReparto> findByVehiculoId(Long vehiculoId);
    List<RutaReparto> findByNombreContainingIgnoreCase(String texto);

    @Query("SELECT DISTINCT r FROM RutaReparto r LEFT JOIN FETCH r.vehiculo LEFT JOIN FETCH r.paradas p LEFT JOIN FETCH p.cliente WHERE r.id = :id")
    Optional<RutaReparto> findDetailById(Long id);
}
