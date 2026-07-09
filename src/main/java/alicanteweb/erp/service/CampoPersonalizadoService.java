package alicanteweb.erp.service;

import alicanteweb.erp.controller.dto.CampoImpresionView;
import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.enums.CampoSistema;
import alicanteweb.erp.repository.CampoPersonalizadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Gestión de los campos del formato de impresión: catálogo de datos del sistema + campos propios,
 * con visibilidad por cliente (a todos, a todos excepto, o solo a algunos).
 */
@Service
@Transactional(readOnly = true)
public class CampoPersonalizadoService {

    private final CampoPersonalizadoRepository repository;
    private final EmpresaConfigService empresaConfigService;

    public CampoPersonalizadoService(CampoPersonalizadoRepository repository,
                                     EmpresaConfigService empresaConfigService) {
        this.repository = repository;
        this.empresaConfigService = empresaConfigService;
    }

    public List<CampoPersonalizado> findAll() {
        return repository.findAllConClientes();
    }

    public Optional<CampoPersonalizado> findById(Long id) {
        // Con fetch de clientes para que la vista de edición pueda leer el conjunto sin sesión abierta.
        return repository.findByIdConClientes(id);
    }

    /**
     * Campos ya resueltos que deben pintarse en un documento, filtrados por tipo de documento y por
     * la visibilidad respecto al cliente. Para los campos de sistema, el valor se calcula al vuelo
     * desde la empresa o el propio cliente; los que resulten vacíos se omiten.
     */
    public List<CampoImpresionView> aplicables(String documento, Cliente cliente) {
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActiva().orElse(null);
        Long clienteId = cliente != null ? cliente.getId() : null;
        return repository.findActivosConClientes().stream()
            .filter(c -> aplicaDocumento(c, documento))
            .filter(c -> aplicaVisibilidad(c, clienteId))
            .map(c -> toView(c, empresa, cliente))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    private boolean aplicaDocumento(CampoPersonalizado c, String documento) {
        return CampoPersonalizado.DOC_AMBOS.equals(c.getDocumento()) || c.getDocumento().equals(documento);
    }

    private boolean aplicaVisibilidad(CampoPersonalizado c, Long clienteId) {
        String vis = c.getVisibilidad();
        if (CampoPersonalizado.VIS_SOLO.equals(vis)) {
            return clienteId != null && c.getClientes().contains(clienteId);
        }
        if (CampoPersonalizado.VIS_EXCEPTO.equals(vis)) {
            return clienteId == null || !c.getClientes().contains(clienteId);
        }
        return true; // VIS_TODOS
    }

    private Optional<CampoImpresionView> toView(CampoPersonalizado c, EmpresaConfig empresa, Cliente cliente) {
        if (c.esSistema()) {
            Optional<CampoSistema> cs = CampoSistema.desde(c.getClaveSistema());
            if (cs.isEmpty()) return Optional.empty();
            String valor = cs.get().valor(empresa, cliente);
            if (valor == null || valor.isBlank()) return Optional.empty();
            String etiqueta = (c.getEtiqueta() != null && !c.getEtiqueta().isBlank())
                ? c.getEtiqueta() : cs.get().getEtiqueta();
            return Optional.of(new CampoImpresionView(etiqueta, valor, c.getUbicacion()));
        }
        if (c.getValor() == null || c.getValor().isBlank()) return Optional.empty();
        return Optional.of(new CampoImpresionView(c.getEtiqueta(), c.getValor(), c.getUbicacion()));
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

        if (campo.esSistema()) {
            if (CampoSistema.desde(campo.getClaveSistema()).isEmpty()) {
                throw new IllegalArgumentException("Selecciona un dato del sistema válido");
            }
            // El valor de un campo de sistema se calcula al imprimir; no se almacena.
            campo.setValor(null);
        } else {
            campo.setOrigen(CampoPersonalizado.ORIGEN_PROPIO);
            campo.setClaveSistema(null);
            if (campo.getEtiqueta() == null || campo.getEtiqueta().isBlank()) {
                throw new IllegalArgumentException("La etiqueta es obligatoria");
            }
        }

        String vis = campo.getVisibilidad();
        if (CampoPersonalizado.VIS_EXCEPTO.equals(vis) || CampoPersonalizado.VIS_SOLO.equals(vis)) {
            if (campo.getClientes() == null || campo.getClientes().isEmpty()) {
                throw new IllegalArgumentException("Selecciona al menos un cliente para esta visibilidad");
            }
        } else {
            // Visibilidad para todos: no se asocia ningún cliente.
            campo.setVisibilidad(CampoPersonalizado.VIS_TODOS);
            if (campo.getClientes() != null) campo.getClientes().clear();
        }
    }
}
