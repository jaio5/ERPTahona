package alicanteweb.erp.repository;

import alicanteweb.erp.entities.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {
    Optional<OrdenProduccion> findByNumero(String numero);
    List<OrdenProduccion> findByEstado(String estado);
    List<OrdenProduccion> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<OrdenProduccion> findByRecetaId(Long recetaId);
    List<OrdenProduccion> findByArticuloId(Long articuloId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(o.numero, LENGTH(o.numero) - 3, 4) AS int)), 0) " +
           "FROM OrdenProduccion o WHERE o.numero LIKE CONCAT(?1, '-%')")
    int findMaxNumeroSecuencialBySerie(String prefijo);
}
