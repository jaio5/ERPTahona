package alicanteweb.erp.controller;

import alicanteweb.erp.controller.rest.DocumentoRestController;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.DocumentoService;
import alicanteweb.erp.service.FacturaService;
import alicanteweb.erp.service.PedidoService;
import alicanteweb.erp.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoRestControllerTest {

    @Mock DocumentoService documentoService;
    @Mock AlbaranService albaranService;
    @Mock FacturaService facturaService;
    @Mock PedidoService pedidoService;
    @Mock UsuarioService usuarioService;
    @Mock ClienteService clienteService;
    DocumentoRestController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentoRestController(documentoService, albaranService, facturaService,
                pedidoService, usuarioService, clienteService);
        Factura factura = new Factura();
        factura.setId(9L);
        factura.setNumero("F-A-2026-0009");
        factura.setEstado("BORRADOR");
        when(albaranService.convertirVariosAFactura(any(), any())).thenReturn(factura);
        when(facturaService.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void facturaDesdeAlbaranesConEmitirPasaPorRevisionAntesDeEmitir() {
        controller.facturarDesdeAlbaranes(Map.of("albaranIds", List.of(1, 2), "emitir", true), null);

        InOrder orden = inOrder(facturaService);
        orden.verify(facturaService).pasarARevision(9L);
        orden.verify(facturaService).aprobarYEmitir(9L);
    }

    @Test
    void facturaDesdeAlbaranesSinEmitirSeQuedaEnBorrador() {
        controller.facturarDesdeAlbaranes(Map.of("albaranIds", List.of(1, 2)), null);

        verify(facturaService, never()).pasarARevision(anyLong());
        verify(facturaService, never()).aprobarYEmitir(anyLong());
    }
}
