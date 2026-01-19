package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@Transactional
public class AlbaranVentaFacturaIntegrationTest {

    @Autowired
    private AlbaranVentaService albaranVentaService;

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private AlbaranVentaFacturaService albaranVentaFacturaService;

    // no direct repos needed

    @Test
    public void testVincularYDesvincularAlbaran() {
        // Crear factura
        Factura factura = new Factura();
        factura.setNumero("TEST-F-001");
        factura.setFecha(LocalDate.now());
        factura.setTotal(BigDecimal.ZERO);
        factura = facturaService.save(factura);

        // Crear albaran
        AlbaranVenta alb = new AlbaranVenta();
        alb.setNumero("TEST-ALB-001");
        alb.setFecha(LocalDate.now());
        alb.setTotal(new BigDecimal("123.45"));
        alb = albaranVentaService.save(alb);

        // Vincular
        AlbaranVentaFacturaId id = new AlbaranVentaFacturaId();
        id.setAlbaranesventasId(alb.getId());
        id.setFacturasId(factura.getId());
        AlbaranVentaFactura avf = new AlbaranVentaFactura();
        avf.setId(id);
        avf.setFacturas(factura);
        albaranVentaFacturaService.save(avf);

        // Comprobar existencia
        final Long facturaId = factura.getId();
        final Long albId = alb.getId();
        List<AlbaranVentaFactura> todos = albaranVentaFacturaService.findAll();
        Assertions.assertTrue(todos.stream().anyMatch(e -> e.getId().getAlbaranesventasId().equals(albId)
                && e.getId().getFacturasId().equals(facturaId)));

        // Desvincular
        albaranVentaFacturaService.deleteById(id);

        List<AlbaranVentaFactura> despues = albaranVentaFacturaService.findAll();
        Assertions.assertFalse(despues.stream().anyMatch(e -> e.getId().getAlbaranesventasId().equals(albId)
                && e.getId().getFacturasId().equals(facturaId)));
    }
}
