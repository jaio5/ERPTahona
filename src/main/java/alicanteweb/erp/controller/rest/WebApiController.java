package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.PedidoCompraRepository;
import alicanteweb.erp.repository.PedidoRepository;
import alicanteweb.erp.repository.ProveedorRepository;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/web")
public class WebApiController {

    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final ProveedorRepository proveedorRepository;
    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final PedidoCompraRepository pedidoCompraRepository;

    public WebApiController(ClienteService clienteService,
                            ArticuloService articuloService,
                            ProveedorRepository proveedorRepository,
                            FacturaRepository facturaRepository,
                            PedidoRepository pedidoRepository,
                            PedidoCompraRepository pedidoCompraRepository) {
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.proveedorRepository = proveedorRepository;
        this.facturaRepository = facturaRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoCompraRepository = pedidoCompraRepository;
    }

    @GetMapping("/resumen")
    public WebResumen resumen() {
        return new WebResumen(
                clienteService.findAll().size(),
                articuloService.findAll().size(),
                proveedorRepository.count(),
                facturaRepository.count(),
                pedidoRepository.count(),
                pedidoCompraRepository.count()
        );
    }

    @GetMapping("/clientes")
    public List<ClienteDto> clientes(@RequestParam(required = false) String q) {
        return clienteService.findAll().stream()
                .filter(cliente -> matches(q, cliente.getCodigo(), cliente.getNombre(), cliente.getCif(), cliente.getPoblacion()))
                .map(ClienteDto::from)
                .toList();
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<ClienteDto> cliente(@PathVariable Long id) {
        return clienteService.findById(id)
                .map(ClienteDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clientes")
    public ClienteDto crearCliente(@RequestBody ClienteDto dto) {
        return ClienteDto.from(clienteService.save(dto.toEntity(new Cliente())));
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<ClienteDto> actualizarCliente(@PathVariable Long id, @RequestBody ClienteDto dto) {
        return clienteService.findById(id)
                .map(cliente -> {
                    dto.applyTo(cliente);
                    return ResponseEntity.ok(ClienteDto.from(clienteService.save(cliente)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clientes/{id}/baja")
    public ResponseEntity<Void> bajaCliente(@PathVariable Long id) {
        clienteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clientes/{id}/activar")
    public ResponseEntity<Void> activarCliente(@PathVariable Long id) {
        clienteService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/articulos")
    public List<ArticuloDto> articulos(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) Boolean activo) {
        List<Articulo> base = activo == null ? articuloService.findAll() : articuloService.findByActivo(activo);
        return base.stream()
                .filter(articulo -> matches(q, articulo.getCodigo(), articulo.getNombre(), articulo.getDescripcion(), articulo.getCategoria()))
                .map(ArticuloDto::from)
                .toList();
    }

    @GetMapping("/articulos/{id}")
    public ResponseEntity<ArticuloDto> articulo(@PathVariable Long id) {
        return articuloService.findById(id)
                .map(ArticuloDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/articulos")
    public ArticuloDto crearArticulo(@RequestBody ArticuloDto dto) {
        return ArticuloDto.from(articuloService.save(dto.toEntity(new Articulo())));
    }

    @PutMapping("/articulos/{id}")
    public ResponseEntity<ArticuloDto> actualizarArticulo(@PathVariable Long id, @RequestBody ArticuloDto dto) {
        return articuloService.findById(id)
                .map(articulo -> {
                    dto.applyTo(articulo);
                    return ResponseEntity.ok(ArticuloDto.from(articuloService.save(articulo)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/articulos/{id}/baja")
    public ResponseEntity<Void> bajaArticulo(@PathVariable Long id) {
        articuloService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/articulos/{id}/activar")
    public ResponseEntity<Void> activarArticulo(@PathVariable Long id) {
        articuloService.activar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean matches(String query, String... values) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalized = query.trim().toLowerCase();
        for (String value : values) {
            if (value != null && value.toLowerCase().contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    public record WebResumen(long clientes,
                             long articulos,
                             long proveedores,
                             long facturas,
                             long pedidosVenta,
                             long pedidosCompra) {
    }

    public record ClienteDto(Long id,
                             String codigo,
                             String nombre,
                             String cif,
                             String telefono,
                             String email,
                             String direccion,
                             String poblacion,
                             String codigoPostal,
                             String provincia,
                             Boolean activo) {

        static ClienteDto from(Cliente cliente) {
            return new ClienteDto(
                    cliente.getId(),
                    cliente.getCodigo(),
                    cliente.getNombre(),
                    cliente.getCif(),
                    cliente.getTelefono(),
                    cliente.getEmail(),
                    cliente.getDireccion(),
                    cliente.getPoblacion(),
                    cliente.getCodigoPostal(),
                    cliente.getProvincia(),
                    cliente.getActivo()
            );
        }

        Cliente toEntity(Cliente cliente) {
            applyTo(cliente);
            return cliente;
        }

        void applyTo(Cliente cliente) {
            cliente.setCodigo(codigo);
            cliente.setNombre(nombre);
            cliente.setCif(cif);
            cliente.setTelefono(telefono);
            cliente.setEmail(email);
            cliente.setDireccion(direccion);
            cliente.setPoblacion(poblacion);
            cliente.setCodigoPostal(codigoPostal);
            cliente.setProvincia(provincia);
            cliente.setActivo(activo == null ? Boolean.TRUE : activo);
        }
    }

    public record ArticuloDto(Long id,
                              String codigo,
                              String nombre,
                              String descripcion,
                              String categoria,
                              String familia,
                              String unidad,
                              BigDecimal iva,
                              BigDecimal pvp,
                              BigDecimal coste,
                              BigDecimal stock,
                              BigDecimal stockMinimo,
                              Boolean activo) {

        static ArticuloDto from(Articulo articulo) {
            return new ArticuloDto(
                    articulo.getId(),
                    articulo.getCodigo(),
                    articulo.getNombre(),
                    articulo.getDescripcion(),
                    articulo.getCategoria(),
                    articulo.getFamilia(),
                    articulo.getUnidad(),
                    articulo.getIva(),
                    articulo.getPvp(),
                    articulo.getCoste(),
                    articulo.getStock(),
                    articulo.getStockMinimo(),
                    articulo.getActivo()
            );
        }

        Articulo toEntity(Articulo articulo) {
            applyTo(articulo);
            return articulo;
        }

        void applyTo(Articulo articulo) {
            articulo.setCodigo(codigo);
            articulo.setNombre(nombre);
            articulo.setDescripcion(descripcion);
            articulo.setCategoria(categoria);
            articulo.setFamilia(familia);
            articulo.setUnidad(unidad);
            articulo.setIva(iva);
            articulo.setPvp(pvp);
            articulo.setCoste(coste);
            articulo.setStock(stock);
            articulo.setStockMinimo(stockMinimo);
            articulo.setActivo(activo == null ? Boolean.TRUE : activo);
        }
    }
}
