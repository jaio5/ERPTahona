package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.TarifaCliente;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.ClienteRepository;
import alicanteweb.erp.service.TarifaClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web")
public class TarifaClienteRestController {

    private final TarifaClienteService tarifaClienteService;
    private final ClienteRepository clienteRepository;
    private final ArticuloRepository articuloRepository;

    public TarifaClienteRestController(TarifaClienteService tarifaClienteService,
                                        ClienteRepository clienteRepository,
                                        ArticuloRepository articuloRepository) {
        this.tarifaClienteService = tarifaClienteService;
        this.clienteRepository = clienteRepository;
        this.articuloRepository = articuloRepository;
    }

    @GetMapping("/tarifas-cliente")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listarPorCliente(@RequestParam Long clienteId) {
        List<Map<String, Object>> result = tarifaClienteService.findByCliente(clienteId).stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId());
                    m.put("clienteId", t.getCliente() != null ? t.getCliente().getId() : null);
                    m.put("articuloId", t.getArticulo() != null ? t.getArticulo().getId() : null);
                    m.put("articulo", t.getArticulo() != null ? t.getArticulo().getNombre() : null);
                    m.put("precioEspecial", t.getPrecioEspecial());
                    m.put("descuento", t.getDescuento());
                    m.put("activo", t.getActivo());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    record TarifaClienteRequest(Long clienteId, Long articuloId, BigDecimal precioEspecial, BigDecimal descuento) {}

    @PostMapping("/tarifas-cliente")
    @Transactional
    public ResponseEntity<Map<String, Object>> guardar(@RequestBody TarifaClienteRequest req) {
        Cliente cliente = clienteRepository.findById(req.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + req.clienteId()));
        Articulo articulo = articuloRepository.findById(req.articuloId())
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado: " + req.articuloId()));

        TarifaCliente tarifa = new TarifaCliente();
        tarifa.setCliente(cliente);
        tarifa.setArticulo(articulo);
        tarifa.setPrecioEspecial(req.precioEspecial());
        tarifa.setDescuento(req.descuento());

        TarifaCliente saved = tarifaClienteService.save(tarifa);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", saved.getId());
        m.put("clienteId", saved.getCliente().getId());
        m.put("articuloId", saved.getArticulo().getId());
        m.put("precioEspecial", saved.getPrecioEspecial());
        m.put("descuento", saved.getDescuento());
        m.put("activo", saved.getActivo());
        return ResponseEntity.ok(m);
    }

    @DeleteMapping("/tarifas-cliente/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tarifaClienteService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
