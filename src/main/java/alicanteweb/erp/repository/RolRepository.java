package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Rol
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Buscar rol por nombre
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Buscar roles activos
     */
    List<Rol> findByActivoTrue();

    /**
     * Buscar roles del sistema (no eliminables)
     */
    List<Rol> findByEsSistemaTrue();

    /**
     * Buscar roles no sistema (personalizados)
     */
    List<Rol> findByEsSistemaFalse();

    /**
     * Verificar si existe un rol con ese nombre
     */
    boolean existsByNombre(String nombre);

    /**
     * Contar roles activos
     */
    long countByActivoTrue();

    /**
     * Buscar roles personalizados activos
     */
    List<Rol> findByEsSistemaFalseAndActivoTrue();

    /**
     * Búsqueda flexible
     */
    @Query("SELECT r FROM Rol r WHERE " +
           "LOWER(r.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Rol> buscar(@Param("search") String search);
}


