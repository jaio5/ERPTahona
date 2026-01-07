package alicanteweb.erp.service;

import alicanteweb.erp.entities.PlanContable;
import alicanteweb.erp.repository.PlanContableRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión del Plan Contable
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PlanContableService {
    private static final Logger log = LoggerFactory.getLogger(PlanContableService.class);

    private final PlanContableRepository planContableRepository;

    /**
     * Obtiene todas las cuentas del plan contable
     */
    public List<PlanContable> obtenerTodas() {
        log.debug("Obteniendo todas las cuentas del plan contable");
        return planContableRepository.findAll();
    }

    /**
     * Obtiene una cuenta por ID
     */
    public Optional<PlanContable> obtenerPorId(Long id) {
        log.debug("Obteniendo cuenta con ID: {}", id);
        return planContableRepository.findById(id);
    }

    /**
     * Obtiene una cuenta por código
     */
    public Optional<PlanContable> obtenerPorCodigo(String codigo) {
        log.debug("Obteniendo cuenta con código: {}", codigo);
        return planContableRepository.findByCodigo(codigo);
    }

    /**
     * Obtiene cuentas por tipo
     */
    public List<PlanContable> obtenerPorTipo(String tipo) {
        log.debug("Obteniendo cuentas de tipo: {}", tipo);
        return planContableRepository.findByTipo(tipo);
    }

    /**
     * Obtiene cuentas por nivel
     */
    public List<PlanContable> obtenerPorNivel(Integer nivel) {
        log.debug("Obteniendo cuentas de nivel: {}", nivel);
        return planContableRepository.findByNivel(nivel);
    }

    /**
     * Guarda una cuenta
     */
    public PlanContable guardar(PlanContable planContable) {
        log.info("Guardando cuenta: {} - {}", planContable.getCodigo(), planContable.getNombre());
        return planContableRepository.save(planContable);
    }

    /**
     * Actualiza una cuenta
     */
    public PlanContable actualizar(Long id, PlanContable planActualizado) {
        log.info("Actualizando cuenta con ID: {}", id);
        return planContableRepository.findById(id)
            .map(plan -> {
                plan.setCodigo(planActualizado.getCodigo());
                plan.setNombre(planActualizado.getNombre());
                plan.setTipo(planActualizado.getTipo());
                plan.setNivel(planActualizado.getNivel());
                return planContableRepository.save(plan);
            })
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con ID: " + id));
    }

    /**
     * Elimina una cuenta
     */
    public void eliminar(Long id) {
        log.info("Eliminando cuenta con ID: {}", id);
        planContableRepository.deleteById(id);
    }
}

