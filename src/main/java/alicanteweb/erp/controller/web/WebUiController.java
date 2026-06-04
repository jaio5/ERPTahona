package alicanteweb.erp.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class WebUiController {

    private final List<WebModule> modules = List.of(
            new WebModule("dashboard", "Resumen", "Panel"),
            new WebModule("clientes", "Clientes", "Comercial"),
            new WebModule("articulos", "Articulos", "Comercial"),
            new WebModule("proveedores", "Proveedores", "Compras"),
            new WebModule("presupuestos", "Presupuestos", "Comercial"),
            new WebModule("pedidos-venta", "Pedidos venta", "Comercial"),
            new WebModule("albaranes", "Albaranes", "Comercial"),
            new WebModule("facturas", "Facturas", "Comercial"),
            new WebModule("pedidos-compra", "Pedidos compra", "Compras"),
            new WebModule("facturas-compra", "Facturas compra", "Compras"),
            new WebModule("almacenes", "Almacenes", "Almacen"),
            new WebModule("recetas", "Recetas", "Produccion"),
            new WebModule("ordenes-produccion", "Ordenes produccion", "Produccion"),
            new WebModule("horneadas", "Horneadas", "Produccion"),
            new WebModule("lotes", "Lotes", "Produccion"),
            new WebModule("appcc", "APPCC", "Produccion"),
            new WebModule("vehiculos", "Vehiculos", "Reparto"),
            new WebModule("rutas-reparto", "Rutas reparto", "Reparto"),
            new WebModule("hojas-ruta", "Hojas ruta", "Reparto"),
            new WebModule("devoluciones", "Devoluciones", "Reparto"),
            new WebModule("direcciones-envio", "Direcciones envio", "Comercial"),
            new WebModule("asientos", "Asientos", "Contabilidad"),
            new WebModule("plan-contable", "Plan contable", "Contabilidad"),
            new WebModule("movimientos-caja", "Caja", "Tesoreria"),
            new WebModule("cajas", "Cajas diarias", "Tesoreria"),
            new WebModule("caja-movimientos", "Movimientos caja diaria", "Tesoreria"),
            new WebModule("movimientos-banco", "Banco", "Tesoreria"),
            new WebModule("bancos", "Cuentas bancarias", "Tesoreria"),
            new WebModule("modelo347", "Modelo 347", "Fiscal"),
            new WebModule("auditoria", "Auditoria", "Sistema"),
            new WebModule("verifactu-evidencias", "VeriFactu", "Fiscal"),
            new WebModule("usuarios", "Usuarios", "Sistema"),
            new WebModule("empresa", "Empresa", "Sistema")
    );

    @GetMapping("/")
    public String root() {
        return "redirect:/web";
    }

    @GetMapping("/ui")
    public String dashboard(Model model) {
        return render(model, "dashboard");
    }

    @GetMapping("/ui/{moduleId}")
    public String module(Model model, @PathVariable String moduleId) {
        boolean known = modules.stream().anyMatch(module -> module.id().equals(moduleId));
        return render(model, known ? moduleId : "dashboard");
    }

    private String render(Model model, String activeModule) {
        model.addAttribute("modules", modules);
        model.addAttribute("activeModule", activeModule);
        return "web/app";
    }

    public record WebModule(String id, String label, String group) {
    }
}
