package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PlanCuentas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para PlanCuentas
 */
@Repository
public interface PlanCuentasRepository extends JpaRepository<PlanCuentas, Long> {

    /**
     * Buscar cuenta por código
     */
    Optional<PlanCuentas> findByCodigo(String codigo);

    /**
     * Buscar cuentas activas
     */
    List<PlanCuentas> findByActivoTrueOrderByCodigoAsc();

    /**
     * Buscar cuentas por tipo
     */
    List<PlanCuentas> findByTipoAndActivoTrueOrderByCodigoAsc(String tipo);

    /**
     * Buscar cuentas por nivel
     */
    List<PlanCuentas> findByNivelOrderByCodigoAsc(Integer nivel);

    /**
     * Buscar cuentas hijas de una cuenta padre
     */
    List<PlanCuentas> findByCuentaPadre_IdOrderByCodigoAsc(Long cuentaPadreId);

    /**
     * Verificar si existe una cuenta
     */
    boolean existsByCodigo(String codigo);

    /**
     * Buscar cuentas por nombre (búsqueda)
     */
    @Query("SELECT p FROM PlanCuentas p WHERE p.activo = true AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR p.codigo LIKE CONCAT('%', :busqueda, '%')) ORDER BY p.codigo")
    List<PlanCuentas> buscar(String busqueda);
}

