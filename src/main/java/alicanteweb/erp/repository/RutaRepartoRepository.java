package alicanteweb.erp.repository;

import alicanteweb.erp.entities.RutaReparto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutaRepartoRepository extends JpaRepository<RutaReparto, Long> {
    List<RutaReparto> findByActivo(Boolean activo);
    List<RutaReparto> findByVehiculoId(Long vehiculoId);
    List<RutaReparto> findByNombreContainingIgnoreCase(String texto);
}
