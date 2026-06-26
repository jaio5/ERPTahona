package alicanteweb.erp.service;

import alicanteweb.erp.entities.TarifaCliente;
import alicanteweb.erp.repository.TarifaClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TarifaClienteService {

    private final TarifaClienteRepository repository;

    public TarifaClienteService(TarifaClienteRepository repository) {
        this.repository = repository;
    }

    public List<TarifaCliente> findByCliente(Long clienteId) {
        return repository.findByClienteId(clienteId);
    }

    public Optional<TarifaCliente> findByClienteAndArticulo(Long clienteId, Long articuloId) {
        return repository.findByClienteIdAndArticuloId(clienteId, articuloId);
    }

    @Transactional
    public TarifaCliente save(TarifaCliente tarifa) {
        if (tarifa.getCliente() == null || tarifa.getArticulo() == null) {
            throw new IllegalArgumentException("Cliente y artículo obligatorios");
        }
        var existente = repository.findByClienteIdAndArticuloId(tarifa.getCliente().getId(), tarifa.getArticulo().getId());
        if (existente.isPresent() && (tarifa.getId() == null || !existente.get().getId().equals(tarifa.getId()))) {
            throw new IllegalArgumentException("Ya existe tarifa para este cliente y artículo");
        }
        return repository.save(tarifa);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public BigDecimal resolverPrecio(Long clienteId, Long articuloId, BigDecimal pvpBase) {
        return repository.findByClienteIdAndArticuloId(clienteId, articuloId)
                .filter(t -> Boolean.TRUE.equals(t.getActivo()) && t.getPrecioEspecial() != null)
                .map(TarifaCliente::getPrecioEspecial)
                .orElse(pvpBase);
    }

    public BigDecimal resolverDescuento(Long clienteId, Long articuloId) {
        return repository.findByClienteIdAndArticuloId(clienteId, articuloId)
                .filter(t -> Boolean.TRUE.equals(t.getActivo()) && t.getDescuento() != null)
                .map(TarifaCliente::getDescuento)
                .orElse(BigDecimal.ZERO);
    }
}
