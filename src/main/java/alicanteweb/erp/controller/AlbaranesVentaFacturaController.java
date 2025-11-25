package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AlbaranesVentaFactura;
import alicanteweb.erp.entities.AlbaranesVentaFacturaId;
import alicanteweb.erp.service.AlbaranesVentaFacturaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/albaranes-venta-factura")
public class AlbaranesVentaFacturaController {
    private final AlbaranesVentaFacturaService service;
    public AlbaranesVentaFacturaController(AlbaranesVentaFacturaService service) { this.service = service; }

    @GetMapping
    @ResponseBody
    public List<AlbaranesVentaFactura> getAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseBody
    public AlbaranesVentaFactura save(@RequestBody AlbaranesVentaFactura avf) {
        return service.save(avf);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable AlbaranesVentaFacturaId id) {
        service.deleteById(id);
    }
}
