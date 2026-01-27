package alicanteweb.erp.repository;

import alicanteweb.erp.entities.EmpresaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaConfigRepository extends JpaRepository<EmpresaConfig, Long> {

    /**
     * Obtiene la configuración activa de la empresa (el primer registro activo)
     * Se usa findFirstByActivoTrue para evitar NonUniqueResultException si hay varias filas activas.
     */
    Optional<EmpresaConfig> findFirstByActivoTrue();

    /**
     * Alias para compatibilidad con código existente
     */
    default Optional<EmpresaConfig> findActive() {
        return findFirstByActivoTrue();
    }

    /**
     * Verifica si existe una configuración activa
     */
    @Query("SELECT COUNT(e) > 0 FROM EmpresaConfig e WHERE e.activo = true")
    boolean existsActive();
}
