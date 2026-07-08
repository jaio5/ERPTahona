package alicanteweb.erp.service;

import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.repository.CampoPersonalizadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Gestión de los campos personalizados del formato de impresión.
 */
@Service
@Transactional(readOnly = true)
public class CampoPersonalizadoService {

    private final CampoPersonalizadoRepository repository;

    public CampoPersonalizadoService(CampoPersonalizadoRepository repository) {
        this.repository = repository;
    }

    public List<CampoPersonalizado> findAll() {
        return repository.findAllByOrderByAmbitoAscOrdenAsc();
    }

    public Optional<CampoPersonalizado> findById(Long id) {
        return repository.findById(id);
    }

    /** Campos que deben pintarse en un documento (por tipo y cliente). */
    public List<CampoPersonalizado> aplicables(String documento, Long clienteId) {
        return repository.findAplicables(documento, clienteId);
    }

    @Transactional
    public CampoPersonalizado save(CampoPersonalizado campo) {
        validar(campo);
        return repository.save(campo);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private void validar(CampoPersonalizado campo) {
        if (campo == null) throw new IllegalArgumentException("Campo nulo");
        if (campo.getEtiqueta() == null || campo.getEtiqueta().isBlank()) {
            throw new IllegalArgumentException("La etiqueta es obligatoria");
        }
        if (CampoPersonalizado.AMBITO_CLIENTE.equals(campo.getAmbito())) {
            if (campo.getClienteId() == null) {
                throw new IllegalArgumentException("Selecciona el cliente para un campo de ámbito CLIENTE");
            }
        } else {
            // Un campo global (ámbito EMPRESA) no va asociado a ningún cliente.
            campo.setClienteId(null);
        }
    }
}
