package alicanteweb.erp.repository;

import alicanteweb.erp.entities.PlanContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanContableRepository extends JpaRepository<PlanContable, Long> {
    Optional<PlanContable> findByCodigo(String codigo);
    List<PlanContable> findByTipo(String tipo);
    List<PlanContable> findByNivel(Integer nivel);
    List<PlanContable> findByActivaTrue();

    List<PlanContable> findByCodigoStartingWith(String prefijo);
}

