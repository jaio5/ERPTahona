package alicanteweb.erp.repository;

import alicanteweb.erp.entities.HojaRutaEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HojaRutaEntregaRepository extends JpaRepository<HojaRutaEntrega, Long> {
    List<HojaRutaEntrega> findByHojaRutaIdOrderByOrden(Long hojaRutaId);
    List<HojaRutaEntrega> findByClienteId(Long clienteId);
    List<HojaRutaEntrega> findByAlbaranId(Long albaranId);
    List<HojaRutaEntrega> findByHojaRutaIdAndEntregadoFalse(Long hojaRutaId);
}
