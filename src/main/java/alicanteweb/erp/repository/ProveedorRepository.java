package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Proveedor
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * Busca un proveedor por su código
     */
    Optional<Proveedor> findByCodigo(String codigo);

    /**
     * Busca proveedores activos
     */
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true ORDER BY p.nombre")
    List<Proveedor> findAllActivos();

    /**
     * Busca proveedores por nombre (búsqueda parcial)
     */
    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Proveedor> findByNombreContaining(String nombre);

    /**
     * Busca proveedores por CIF
     */
    Optional<Proveedor> findByCif(String cif);
    Optional<Proveedor> findByCifIgnoreCase(String cif);
    Optional<Proveedor> findFirstByNombreIgnoreCase(String nombre);

    /**
     * Busca proveedores por múltiples criterios
     */
    @Query("SELECT p FROM Proveedor p WHERE " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
           "LOWER(p.cif) LIKE LOWER(CONCAT('%', :criterio, '%'))")
    List<Proveedor> buscarPorCriterio(String criterio);

    /**
     * Busca proveedores por nombre (case insensitive)
     */
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca proveedores activos
     */
    List<Proveedor> findByActivoTrue();

    @Query("SELECT p FROM Proveedor p WHERE "
         + "(:q IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%',:q,'%')) "
         + "OR LOWER(p.codigo) LIKE LOWER(CONCAT('%',:q,'%')) "
         + "OR LOWER(p.cif) LIKE LOWER(CONCAT('%',:q,'%'))) "
         + "ORDER BY p.nombre")
    Page<Proveedor> buscarPaginado(@Param("q") String q, Pageable pageable);
}

