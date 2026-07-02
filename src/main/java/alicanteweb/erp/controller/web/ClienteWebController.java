package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.exception.ErpException;
import alicanteweb.erp.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("@permisos.puede('clientes', 'ver')")
@RequestMapping("/web/clientes")
public class ClienteWebController extends BaseWebController {

    private static final Logger log = LoggerFactory.getLogger(ClienteWebController.class);

    private final ClienteService service;
    private final FacturaService facturaService;
    private final AlbaranVentaService albaranService;
    private final RgpdSolicitudService rgpdSolicitudService;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    public ClienteWebController(ClienteService service,
                                FacturaService facturaService,
                                AlbaranVentaService albaranService,
                                RgpdSolicitudService rgpdSolicitudService,
                                AuditoriaService auditoriaService,
                                UsuarioService usuarioService) {
        super(usuarioService);
        this.service = service;
        this.facturaService = facturaService;
        this.albaranService = albaranService;
        this.rgpdSolicitudService = rgpdSolicitudService;
        this.auditoriaService = auditoriaService;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    @GetMapping
    public String lista(Model m,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "25") int size,
                        @RequestParam(defaultValue = "nombre") String sort,
                        @RequestParam(defaultValue = "asc") String dir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<Cliente> pageResult = service.buscarPaginado(q, PageRequest.of(page, size, Sort.by(direction, sort)));
        m.addAttribute("moduloActivo", "clientes");
        m.addAttribute("titulo", "Clientes");
        m.addAttribute("clientes", pageResult.getContent());
        m.addAttribute("page", pageResult);
        m.addAttribute("q", q);
        m.addAttribute("sort", sort);
        m.addAttribute("dir", dir);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.active("Clientes")));
        return WebController.layout(m, "clientes/lista");
    }
    @GetMapping("/nuevo")
    @PreAuthorize("@permisos.puede('clientes', 'crear')")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "clientes");
        m.addAttribute("titulo", "Nuevo cliente");
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.link("Clientes", "/web/clientes"),
            BreadcrumbBuilder.active("Nuevo cliente")));
        return WebController.layout(m, "clientes/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(c -> {
            m.addAttribute("moduloActivo", "clientes");
            m.addAttribute("titulo", c.getNombre());
            m.addAttribute("cliente", c);
            var facturas = facturaService.findByClienteId(id).stream()
                    .sorted(java.util.Comparator.comparing(
                            alicanteweb.erp.entities.Factura::getFecha,
                            java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                    .limit(10).toList();
            var albaranes = albaranService.findByClienteId(id).stream()
                    .sorted(java.util.Comparator.comparing(
                            alicanteweb.erp.entities.AlbaranVenta::getFecha,
                            java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                    .limit(10).toList();
            m.addAttribute("facturas", facturas);
            m.addAttribute("albaranes", albaranes);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.link("Clientes", "/web/clientes"),
                BreadcrumbBuilder.active(c.getNombre())));
            return WebController.layout(m, "clientes/ver");
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Registro no encontrado");
            return "redirect:/web/clientes";
        });
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("@permisos.puede('clientes', 'editar')")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id).map(c -> {
            m.addAttribute("moduloActivo", "clientes");
            m.addAttribute("titulo", "Editar cliente");
            m.addAttribute("cliente", c);
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.link("Clientes", "/web/clientes"),
                BreadcrumbBuilder.active("Editar")));
            return WebController.layout(m, "clientes/formulario");
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Registro no encontrado");
            return "redirect:/web/clientes";
        });
    }

    @PostMapping
    @PreAuthorize("@permisos.puedeCrearOEditar('clientes')")
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String codigo,
                          @RequestParam String nombre,
                          @RequestParam(required = false) String cif,
                          @RequestParam(required = false) String telefono,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String direccion,
                          @RequestParam(required = false) String poblacion,
                          @RequestParam(required = false) String codigoPostal,
                          RedirectAttributes ra) {
        try {
            Cliente c = id != null
                    ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de cliente no válido: " + id))
                    : new Cliente();
            c.setCodigo(codigo);
            c.setNombre(nombre);
            c.setCif(cif);
            c.setTelefono(telefono);
            c.setEmail(email);
            c.setDireccion(direccion);
            c.setPoblacion(poblacion);
            c.setCodigoPostal(codigoPostal);
            service.save(c);
            log.info("Cliente guardado correctamente [id={}, nombre={}]", c.getId(), c.getNombre());
            ra.addFlashAttribute("exito", "Cliente guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar cliente [id={}]: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            if (id != null) return "redirect:/web/clientes/" + id + "/editar";
        }
        return "redirect:/web/clientes";
    }

    @GetMapping("/export.csv")
    @PreAuthorize("@permisos.puede('clientes', 'exportar')")
    public ResponseEntity<byte[]> exportarListadoCsv(HttpSession s, @RequestParam(required = false) String q) {
        List<Cliente> clientes = (q != null && !q.isBlank())
                ? service.buscarPaginado(q, PageRequest.of(0, 10000)).getContent()
                : service.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');
        csv.append("ID;Codigo;Nombre;CIF/NIF;Telefono;Email;Direccion;Poblacion;Codigo postal;Provincia;Activo\n");
        for (Cliente c : clientes) {
            csv.append(csv(c.getId())).append(';')
                .append(csv(c.getCodigo())).append(';')
                .append(csv(c.getNombre())).append(';')
                .append(csv(c.getCif())).append(';')
                .append(csv(c.getTelefono())).append(';')
                .append(csv(c.getEmail())).append(';')
                .append(csv(c.getDireccion())).append(';')
                .append(csv(c.getPoblacion())).append(';')
                .append(csv(c.getCodigoPostal())).append(';')
                .append(csv(c.getProvincia())).append(';')
                .append(csv(Boolean.TRUE.equals(c.getActivo()) ? "SI" : "NO"))
                .append('\n');
        }

        auditoriaService.registrarExportacion(usuarioActual(s), "CLIENTES_CSV",
            "Exportacion de listado de clientes (" + clientes.size() + " registros)");
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        return descarga(body, "clientes_" + LocalDate.now() + ".csv", "text/csv; charset=UTF-8");
    }

    @GetMapping("/{id}/export.json")
    @PreAuthorize("@permisos.puede('clientes', 'exportar')")
    public ResponseEntity<byte[]> exportarDatosCliente(HttpSession s, @PathVariable Long id) {
        try {
            Map<String, Object> datos = rgpdSolicitudService.exportarDatosCliente(id);
            auditoriaService.registrarExportacion(usuarioActual(s), "CLIENTE_RGPD_JSON",
                "Exportacion RGPD/portabilidad del cliente " + id);
            byte[] body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(datos);
            return descarga(body, "cliente_" + id + "_rgpd_" + LocalDate.now() + ".json", MediaType.APPLICATION_JSON_VALUE);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Error al exportar datos RGPD del cliente {}: {}", id, e.getMessage(), e);
            throw new ErpException("Error al exportar datos RGPD: " + e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> descarga(byte[] body, String filename, String contentType) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString())
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .contentType(MediaType.parseMediaType(contentType))
            .body(body);
    }

    private String csv(Object value) {
        String text = value != null ? String.valueOf(value) : "";
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
