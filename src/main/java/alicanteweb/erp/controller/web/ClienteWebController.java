package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/web/clientes")
public class ClienteWebController {
    private final ClienteService service;
    private final RgpdSolicitudService rgpdSolicitudService;
    private final AuditoriaService auditoriaService;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;

    public ClienteWebController(ClienteService service,
                                RgpdSolicitudService rgpdSolicitudService,
                                AuditoriaService auditoriaService,
                                UsuarioService usuarioService) {
        this.service = service;
        this.rgpdSolicitudService = rgpdSolicitudService;
        this.auditoriaService = auditoriaService;
        this.usuarioService = usuarioService;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required = false) String q) {
        if(WebController.requireLogin(s))return"redirect:/web/login";
        List<Cliente> items = service.findAll();
        if(q!=null&&!q.isBlank()){String t=q.toLowerCase();items=items.stream().filter(c->(c.getNombre()!=null&&c.getNombre().toLowerCase().contains(t))||(c.getCodigo()!=null&&c.getCodigo().toLowerCase().contains(t))||(c.getCif()!=null&&c.getCif().toLowerCase().contains(t))).toList();}
        m.addAttribute("moduloActivo","clientes");m.addAttribute("titulo","Clientes");m.addAttribute("clientes",items);m.addAttribute("q",q);
        return WebController.layout(m,"clientes/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession s,Model m){if(WebController.requireLogin(s))return"redirect:/web/login";m.addAttribute("moduloActivo","clientes");m.addAttribute("titulo","Nuevo cliente");return WebController.layout(m,"clientes/formulario");}
    @GetMapping("/{id}")
    public String ver(HttpSession s,@PathVariable Long id,Model m){if(WebController.requireLogin(s))return"redirect:/web/login";return service.findById(id).map(c->{m.addAttribute("moduloActivo","clientes");m.addAttribute("titulo",c.getNombre());m.addAttribute("cliente",c);return WebController.layout(m,"clientes/ver");}).orElse("redirect:/web/clientes");}
    @PostMapping
    public String guardar(HttpSession s,@RequestParam(required=false)Long id,@RequestParam String codigo,@RequestParam String nombre,@RequestParam(required=false)String cif,@RequestParam(required=false)String telefono,@RequestParam(required=false)String email,@RequestParam(required=false)String direccion,@RequestParam(required=false)String poblacion,@RequestParam(required=false)String codigoPostal,RedirectAttributes ra){if(WebController.requireLogin(s))return"redirect:/web/login";try{Cliente c=id!=null?service.findById(id).orElse(new Cliente()):new Cliente();c.setCodigo(codigo);c.setNombre(nombre);c.setCif(cif);c.setTelefono(telefono);c.setEmail(email);c.setDireccion(direccion);c.setPoblacion(poblacion);c.setCodigoPostal(codigoPostal);service.save(c);ra.addFlashAttribute("exito","Cliente guardado");}catch(Exception e){ra.addFlashAttribute("error",e.getMessage());}return"redirect:/web/clientes";}

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exportarListadoCsv(HttpSession s, @RequestParam(required = false) String q) {
        if (WebController.requireLogin(s)) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/web/login").build();
        }
        List<Cliente> clientes = service.findAll();
        if (q != null && !q.isBlank()) {
            String t = q.toLowerCase();
            clientes = clientes.stream()
                .filter(c -> (c.getNombre() != null && c.getNombre().toLowerCase().contains(t))
                    || (c.getCodigo() != null && c.getCodigo().toLowerCase().contains(t))
                    || (c.getCif() != null && c.getCif().toLowerCase().contains(t)))
                .toList();
        }

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
    public ResponseEntity<byte[]> exportarDatosCliente(HttpSession s, @PathVariable Long id) throws Exception {
        if (WebController.requireLogin(s)) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/web/login").build();
        }
        Map<String, Object> datos = rgpdSolicitudService.exportarDatosCliente(id);
        auditoriaService.registrarExportacion(usuarioActual(s), "CLIENTE_RGPD_JSON",
            "Exportacion RGPD/portabilidad del cliente " + id);
        byte[] body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(datos);
        return descarga(body, "cliente_" + id + "_rgpd_" + LocalDate.now() + ".json", MediaType.APPLICATION_JSON_VALUE);
    }

    private Usuario usuarioActual(HttpSession session) {
        Object usuarioId = session != null ? session.getAttribute("usuarioId") : null;
        if (usuarioId instanceof Long id) {
            return usuarioService.buscarPorId(id).orElse(null);
        }
        if (usuarioId instanceof Number n) {
            return usuarioService.buscarPorId(n.longValue()).orElse(null);
        }
        return null;
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
