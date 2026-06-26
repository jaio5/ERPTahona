package alicanteweb.erp.service;

import alicanteweb.erp.entities.Receta;
import alicanteweb.erp.entities.RecetaIngrediente;
import alicanteweb.erp.repository.RecetaIngredienteRepository;
import alicanteweb.erp.repository.RecetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final RecetaIngredienteRepository ingredienteRepository;

    public RecetaService(RecetaRepository recetaRepository, RecetaIngredienteRepository ingredienteRepository) {
        this.recetaRepository = recetaRepository;
        this.ingredienteRepository = ingredienteRepository;
    }

    public List<Receta> findAll() {
        return recetaRepository.findAll();
    }

    public long count() {
        return recetaRepository.count();
    }

    public Optional<Receta> findById(Long id) {
        return recetaRepository.findById(id);
    }

    public Optional<Receta> findDetailById(Long id) {
        return recetaRepository.findDetailById(id);
    }

    public Optional<Receta> findByCodigo(String codigo) {
        return recetaRepository.findByCodigo(codigo);
    }

    public List<Receta> findByActivo(boolean activo) {
        return recetaRepository.findByActivo(activo);
    }

    public List<Receta> searchByNombre(String texto) {
        return recetaRepository.findByNombreContainingIgnoreCase(texto);
    }

    @Transactional
    public Receta save(Receta receta) {
        if (receta == null) throw new IllegalArgumentException("Receta nula");
        if (receta.getCodigo() == null || receta.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la receta es obligatorio");
        }
        var opt = recetaRepository.findByCodigo(receta.getCodigo().trim());
        if (opt.isPresent() && (receta.getId() == null || !opt.get().getId().equals(receta.getId()))) {
            throw new IllegalArgumentException("Ya existe una receta con el código: " + receta.getCodigo());
        }
        return recetaRepository.save(receta);
    }

    @Transactional
    public void deleteById(Long id) {
        recetaRepository.deleteById(id);
    }

    public List<RecetaIngrediente> getIngredientes(Long recetaId) {
        return ingredienteRepository.findDetailByRecetaId(recetaId);
    }

    @Transactional
    public RecetaIngrediente addIngrediente(RecetaIngrediente ingrediente) {
        if (ingrediente.getOrden() == null) {
            int maxOrden = ingredienteRepository.findByRecetaIdOrderByOrden(ingrediente.getReceta().getId())
                    .stream().mapToInt(i -> i.getOrden() != null ? i.getOrden() : 0).max().orElse(0);
            ingrediente.setOrden(maxOrden + 1);
        }
        return ingredienteRepository.save(ingrediente);
    }

    @Transactional
    public void removeIngrediente(Long ingredienteId) {
        ingredienteRepository.deleteById(ingredienteId);
    }

    @Transactional
    public void darDeBaja(Long id) {
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada con id: " + id));
        receta.setActivo(false);
        recetaRepository.save(receta);
    }

    @Transactional
    public void activar(Long id) {
        Receta receta = recetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada con id: " + id));
        receta.setActivo(true);
        recetaRepository.save(receta);
    }

    /**
     * Calcula el coste total de una receta sumando los costes de sus ingredientes.
     * @return Coste total de la receta
     */
    public java.math.BigDecimal calcularCoste(Long recetaId) {
        List<RecetaIngrediente> ingredientes = ingredienteRepository.findByRecetaIdOrderByOrden(recetaId);
        if (ingredientes.isEmpty()) return java.math.BigDecimal.ZERO;

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (RecetaIngrediente ing : ingredientes) {
            if (ing.getArticulo() != null && ing.getArticulo().getCoste() != null && ing.getCantidad() != null) {
                total = total.add(ing.getArticulo().getCoste().multiply(ing.getCantidad()));
            }
        }
        return total;
    }

    /**
     * Calcula el coste por unidad de rendimiento.
     */
    public java.math.BigDecimal calcularCostePorUnidad(Long recetaId) {
        Receta receta = recetaRepository.findById(recetaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada: " + recetaId));
        java.math.BigDecimal costeTotal = calcularCoste(recetaId);
        if (receta.getRendimientoCantidad() == null || receta.getRendimientoCantidad().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return costeTotal;
        }
        return costeTotal.divide(receta.getRendimientoCantidad(), 4, java.math.RoundingMode.HALF_UP);
    }
}
