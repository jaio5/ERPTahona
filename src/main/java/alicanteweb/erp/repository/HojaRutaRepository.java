package alicanteweb.erp.repository;

import alicanteweb.erp.entities.HojaRuta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HojaRutaRepository extends JpaRepository<HojaRuta, Long> {
    @EntityGraph(attributePaths = {"vehiculo"})
    List<HojaRuta> findByFecha(LocalDate fecha);
    List<HojaRuta> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<HojaRuta> findByRutaId(Long rutaId);
    List<HojaRuta> findByEstado(String estado);
    long countByEstado(String estado);
    List<HojaRuta> findByVehiculoId(Long vehiculoId);
    @EntityGraph(attributePaths = {"vehiculo"})
    Optional<HojaRuta> findFirstByFechaAndUsuarioId(LocalDate fecha, Long usuarioId);
    @EntityGraph(attributePaths = {"vehiculo"})
    Optional<HojaRuta> findFirstByFechaAndUsuarioIsNullAndConductorIgnoreCase(LocalDate fecha, String conductor);
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByIdAndUsuarioIsNullAndConductorIgnoreCase(Long id, String conductor);

    @Query("SELECT h FROM HojaRuta h LEFT JOIN FETCH h.vehiculo LEFT JOIN FETCH h.ruta WHERE h.id = :id")
    Optional<HojaRuta> findDetailById(@Param("id") Long id);
}
