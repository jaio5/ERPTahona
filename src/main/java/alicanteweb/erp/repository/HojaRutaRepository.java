package alicanteweb.erp.repository;

import alicanteweb.erp.entities.HojaRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HojaRutaRepository extends JpaRepository<HojaRuta, Long> {
    List<HojaRuta> findByFecha(LocalDate fecha);
    List<HojaRuta> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<HojaRuta> findByRutaId(Long rutaId);
    List<HojaRuta> findByEstado(String estado);
    List<HojaRuta> findByVehiculoId(Long vehiculoId);
}
