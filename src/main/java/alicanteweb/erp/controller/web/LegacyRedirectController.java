package alicanteweb.erp.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyRedirectController {

    @GetMapping("/web/caja")
    public String caja() { return "redirect:/web/tesoreria"; }

    @GetMapping("/web/movimientos-banco")
    public String movimientosBanco() { return "redirect:/web/tesoreria/extractos"; }

    @GetMapping("/web/asientos")
    public String asientos() { return "redirect:/web/contabilidad"; }

    @GetMapping("/web/backup")
    public String backup() { return "redirect:/web/backups"; }
}
