package alicanteweb.erp.repository;

import alicanteweb.erp.entities.TipoImpositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoImpositivoRepository extends JpaRepository<TipoImpositivo, Long> {

    List<TipoImpositivo> findAllByOrderByOrdenAscPorcentajeAsc();

    List<TipoImpositivo> findByActivoTrueOrderByOrdenAscPorcentajeAsc();

    Optional<TipoImpositivo> findFirstByEsDefectoTrue();

    /** Desmarca el "por defecto" de todos salvo el indicado (garantiza unicidad). */
    @Modifying
    @Query("UPDATE TipoImpositivo t SET t.esDefecto = false WHERE t.id <> :id")
    void desmarcarDefectoExcepto(Long id);
}
