package alicanteweb.erp.service;

import alicanteweb.erp.entities.TipoImpositivo;
import alicanteweb.erp.repository.TipoImpositivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Gestión del catálogo de tipos de IVA. Garantiza que solo haya un tipo "por defecto"
 * y valida los porcentajes. No toca documentos existentes: el catálogo solo se usa al
 * crear líneas nuevas.
 */
@Service
@Transactional(readOnly = true)
public class TipoImpositivoService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final TipoImpositivoRepository repository;

    public TipoImpositivoService(TipoImpositivoRepository repository) {
        this.repository = repository;
    }

    public List<TipoImpositivo> findAll() {
        return repository.findAllByOrderByOrdenAscPorcentajeAsc();
    }

    /** Tipos activos, para poblar los desplegables de IVA. */
    public List<TipoImpositivo> findActivos() {
        return repository.findByActivoTrueOrderByOrdenAscPorcentajeAsc();
    }

    public Optional<TipoImpositivo> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<TipoImpositivo> findDefecto() {
        return repository.findFirstByEsDefectoTrue();
    }

    @Transactional
    public TipoImpositivo save(TipoImpositivo tipo) {
        validar(tipo);
        TipoImpositivo guardado = repository.save(tipo);
        // Unicidad del "por defecto": al marcar uno, se desmarcan los demás.
        if (Boolean.TRUE.equals(guardado.getEsDefecto())) {
            repository.desmarcarDefectoExcepto(guardado.getId());
        }
        return guardado;
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private void validar(TipoImpositivo tipo) {
        if (tipo == null) throw new IllegalArgumentException("Tipo impositivo nulo");
        if (tipo.getNombre() == null || tipo.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (tipo.getPorcentaje() == null) {
            throw new IllegalArgumentException("El porcentaje es obligatorio");
        }
        validarRango(tipo.getPorcentaje(), "El porcentaje");
        if (tipo.getRecargoEquivalencia() != null) {
            validarRango(tipo.getRecargoEquivalencia(), "El recargo de equivalencia");
        }
    }

    private void validarRango(BigDecimal valor, String campo) {
        if (valor.signum() < 0 || valor.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException(campo + " debe estar entre 0 y 100");
        }
    }
}
