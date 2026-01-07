package alicanteweb.erp.service;

import alicanteweb.erp.entities.Caja;
import alicanteweb.erp.repository.CajaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de cajas
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CajaService {
    private static final Logger log = LoggerFactory.getLogger(CajaService.class);

    private final CajaRepository cajaRepository;

    /**
     * Obtiene todas las cajas
     */
    public List<Caja> obtenerTodas() {
        log.debug("Obteniendo todas las cajas");
        return cajaRepository.findAll();
    }

    /**
     * Obtiene una caja por ID
     */
    public Optional<Caja> obtenerPorId(Long id) {
        log.debug("Obteniendo caja con ID: {}", id);
        return cajaRepository.findById(id);
    }

    /**
     * Obtiene una caja por código
     */
    public Optional<Caja> obtenerPorCodigo(String codigo) {
        log.debug("Obteniendo caja con código: {}", codigo);
        return cajaRepository.findByCodigo(codigo);
    }

    /**
     * Obtiene todas las cajas activas
     */
    public List<Caja> obtenerActivas() {
        log.debug("Obteniendo cajas activas");
        return cajaRepository.findByActivaTrue();
    }

    /**
     * Guarda una caja
     */
    public Caja guardar(Caja caja) {
        log.info("Guardando caja: {} - {}", caja.getCodigo(), caja.getNombre());
        if (caja.getSaldoActual() == null) {
            caja.setSaldoActual(caja.getSaldoInicial() != null ? caja.getSaldoInicial() : BigDecimal.ZERO);
        }
        return cajaRepository.save(caja);
    }

    /**
     * Actualiza una caja
     */
    public Caja actualizar(Long id, Caja cajaActualizada) {
        log.info("Actualizando caja con ID: {}", id);
        return cajaRepository.findById(id)
            .map(caja -> {
                caja.setCodigo(cajaActualizada.getCodigo());
                caja.setNombre(cajaActualizada.getNombre());
                caja.setObservaciones(cajaActualizada.getObservaciones());
                caja.setActiva(cajaActualizada.getActiva());
                return cajaRepository.save(caja);
            })
            .orElseThrow(() -> new RuntimeException("Caja no encontrada con ID: " + id));
    }

    /**
     * Elimina una caja
     */
    public void eliminar(Long id) {
        log.info("Eliminando caja con ID: {}", id);
        cajaRepository.deleteById(id);
    }

    /**
     * Desactiva una caja
     */
    public void desactivar(Long id) {
        log.info("Desactivando caja con ID: {}", id);
        cajaRepository.findById(id).ifPresent(caja -> {
            caja.setActiva(false);
            cajaRepository.save(caja);
        });
    }

    /**
     * Activa una caja
     */
    public void activar(Long id) {
        log.info("Activando caja con ID: {}", id);
        cajaRepository.findById(id).ifPresent(caja -> {
            caja.setActiva(true);
            cajaRepository.save(caja);
        });
    }

    /**
     * Actualiza el saldo de una caja
     */
    public void actualizarSaldo(Long id, BigDecimal nuevoSaldo) {
        log.info("Actualizando saldo de caja con ID: {} - Nuevo saldo: {}", id, nuevoSaldo);
        cajaRepository.findById(id).ifPresent(caja -> {
            caja.setSaldoActual(nuevoSaldo);
            cajaRepository.save(caja);
        });
    }

    /**
     * Suma una cantidad al saldo de la caja
     */
    public void agregarAlSaldo(Long id, BigDecimal cantidad) {
        log.info("Agregando {} al saldo de caja con ID: {}", cantidad, id);
        cajaRepository.findById(id).ifPresent(caja -> {
            BigDecimal nuevoSaldo = caja.getSaldoActual().add(cantidad);
            caja.setSaldoActual(nuevoSaldo);
            cajaRepository.save(caja);
        });
    }

    /**
     * Resta una cantidad del saldo de la caja
     */
    public void restarDelSaldo(Long id, BigDecimal cantidad) {
        log.info("Restando {} del saldo de caja con ID: {}", cantidad, id);
        cajaRepository.findById(id).ifPresent(caja -> {
            BigDecimal nuevoSaldo = caja.getSaldoActual().subtract(cantidad);
            caja.setSaldoActual(nuevoSaldo);
            cajaRepository.save(caja);
        });
    }
}

