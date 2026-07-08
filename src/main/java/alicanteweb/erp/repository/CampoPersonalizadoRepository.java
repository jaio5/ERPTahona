package alicanteweb.erp.repository;

import alicanteweb.erp.entities.CampoPersonalizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoPersonalizadoRepository extends JpaRepository<CampoPersonalizado, Long> {

    List<CampoPersonalizado> findAllByOrderByAmbitoAscOrdenAsc();

    /**
     * Campos aplicables a un documento concreto: activos, del tipo de documento (o AMBOS) y
     * globales (ámbito EMPRESA) o del cliente indicado. Ordenados por zona y orden para pintarlos.
     */
    @Query("SELECT c FROM CampoPersonalizado c WHERE c.activo = true "
        + "AND (c.documento = :documento OR c.documento = 'AMBOS') "
        + "AND (c.ambito = 'EMPRESA' OR (c.ambito = 'CLIENTE' AND c.clienteId = :clienteId)) "
        + "ORDER BY c.ubicacion, c.orden")
    List<CampoPersonalizado> findAplicables(@Param("documento") String documento,
                                            @Param("clienteId") Long clienteId);
}
