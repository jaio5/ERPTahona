package alicanteweb.erp.repository;

import alicanteweb.erp.entities.EmpresaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaConfigRepository extends JpaRepository<EmpresaConfig, Long> {

    /**
     * Obtiene la configuraciÃ³n activa de la empresa
     */
    @Query("SELECT e FROM EmpresaConfig e WHERE e.activo = true")
    Optional<EmpresaConfig> findActive();

    /**
     * Verifica si existe una configuraciÃ³n activa
     */
    @Query("SELECT COUNT(e) > 0 FROM EmpresaConfig e WHERE e.activo = true")
    boolean existsActive();
}


