package alicanteweb.erp.repository;

import alicanteweb.erp.entities.CampoPersonalizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampoPersonalizadoRepository extends JpaRepository<CampoPersonalizado, Long> {

    /** Un campo con su conjunto de clientes cargado (para editar y para guardar cambios). */
    @Query("SELECT c FROM CampoPersonalizado c LEFT JOIN FETCH c.clientes WHERE c.id = :id")
    Optional<CampoPersonalizado> findByIdConClientes(@Param("id") Long id);

    /** Todos los campos con su conjunto de clientes cargado, para el listado de administración. */
    @Query("SELECT DISTINCT c FROM CampoPersonalizado c LEFT JOIN FETCH c.clientes "
        + "ORDER BY c.ubicacion, c.orden, c.id")
    List<CampoPersonalizado> findAllConClientes();

    /** Campos activos con su conjunto de clientes, para resolver los aplicables a un documento. */
    @Query("SELECT DISTINCT c FROM CampoPersonalizado c LEFT JOIN FETCH c.clientes "
        + "WHERE c.activo = true ORDER BY c.ubicacion, c.orden, c.id")
    List<CampoPersonalizado> findActivosConClientes();
}
