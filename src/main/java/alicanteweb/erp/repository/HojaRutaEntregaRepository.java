package alicanteweb.erp.repository;

import alicanteweb.erp.entities.HojaRutaEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HojaRutaEntregaRepository extends JpaRepository<HojaRutaEntrega, Long> {
    List<HojaRutaEntrega> findByHojaRutaIdOrderByOrden(Long hojaRutaId);
    List<HojaRutaEntrega> findByClienteId(Long clienteId);
    List<HojaRutaEntrega> findByAlbaranId(Long albaranId);
    List<HojaRutaEntrega> findByHojaRutaIdAndEntregadoFalse(Long hojaRutaId);
    long countByHojaRutaId(Long hojaRutaId);

    @Query("SELECT e.albaran.id FROM HojaRutaEntrega e WHERE e.albaran IS NOT NULL")
    List<Long> findAllAlbaranIdsAsignados();

    @Query("SELECT e FROM HojaRutaEntrega e LEFT JOIN FETCH e.cliente WHERE e.hojaRuta.id = :hojaRutaId ORDER BY e.orden ASC")
    List<HojaRutaEntrega> findDetailByHojaRutaId(@Param("hojaRutaId") Long hojaRutaId);

    boolean existsByIdAndHojaRutaUsuarioId(Long id, Long usuarioId);
    boolean existsByIdAndHojaRutaUsuarioIsNullAndHojaRutaConductorIgnoreCase(Long id, String conductor);
}
