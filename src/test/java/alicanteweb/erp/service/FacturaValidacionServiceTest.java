package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaValidacionServiceTest {

    @Mock FacturaLineaService lineaService;
    @Mock FacturaRepository repository;
    FacturaValidacionService service;

    @BeforeEach
    void setUp() {
        service = new FacturaValidacionService(lineaService, repository);
    }

    @Test
    void facturaOrdinariaValidaPuedeEmitirse() {
        Factura factura = facturaValida();
        when(lineaService.findByFacturaId(1L)).thenReturn(List.of(lineaValida()));
        when(repository.findBySerieAndNumero("GEN", factura.getNumero())).thenReturn(Optional.of(factura));

        assertTrue(service.validarParaEmision(factura).isEmpty());
        assertTrue(service.puedeEmitirse(factura));
        assertEquals("La factura es válida y puede ser emitida", service.obtenerInformeValidacion(factura));
    }

    @Test
    void informaCamposClienteLineasEImportesInvalidos() {
        Factura factura = new Factura();
        factura.setId(2L);
        factura.setNumero("bad number");
        factura.setSerie("serie no valida");
        factura.setTipoFactura("");
        factura.setTotal(new BigDecimal("-1"));
        factura.setBaseImponible(new BigDecimal("-2"));
        factura.setTotalIva(new BigDecimal("-3"));
        factura.setPagado(BigDecimal.TEN);

        FacturaLinea linea = new FacturaLinea();
        linea.setCantidad(BigDecimal.ZERO);
        linea.setPrecio(new BigDecimal("-1"));
        linea.setIva(new BigDecimal("101"));
        when(lineaService.findByFacturaId(2L)).thenReturn(List.of(linea));
        when(repository.findBySerieAndNumero("SERIE NO VALIDA", "bad number")).thenReturn(Optional.empty());

        List<String> errores = service.validarParaEmision(factura);

        assertTrue(errores.stream().anyMatch(e -> e.contains("fecha de expedición")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("serie contiene")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("tipo de factura")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("cliente asignado")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("artículo asignado")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("cantidad")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("precio")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("IVA")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("total de la factura")));
        assertTrue(errores.stream().anyMatch(e -> e.contains("número de factura contiene")));
        assertFalse(service.obtenerInformeValidacion(factura).isBlank());
    }

    @Test
    void validaRectificativaSimplificadaDuplicadoYVerifactu() {
        Factura factura = facturaValida();
        factura.setTipoFactura("RECTIFICATIVA");
        factura.setFacturaRectificadaNumero(null);
        factura.setFacturaRectificadaFecha(null);
        factura.setMotivoRectificacion(null);
        factura.setTotal(new BigDecimal("3001"));
        factura.setBaseImponible(new BigDecimal("100"));
        factura.setTotalIva(new BigDecimal("21"));
        factura.setVerifactuHash("hash");
        factura.setVerifactuQr(null);
        Factura existente = new Factura();
        existente.setId(99L);

        when(lineaService.findByFacturaId(1L)).thenReturn(List.of(lineaValida()));
        when(repository.findBySerieAndNumero("GEN", factura.getNumero())).thenReturn(Optional.of(existente));

        List<String> erroresRectificativa = service.validarParaEmision(factura);
        assertTrue(erroresRectificativa.stream().anyMatch(e -> e.contains("factura original")));
        assertTrue(erroresRectificativa.stream().anyMatch(e -> e.contains("motivo")));
        assertTrue(erroresRectificativa.stream().anyMatch(e -> e.contains("Ya existe")));
        assertTrue(erroresRectificativa.stream().anyMatch(e -> e.contains("código QR")));

        factura.setTipoFactura("SIMPLIFICADA");
        List<String> erroresSimplificada = service.validarParaEmision(factura);
        assertTrue(erroresSimplificada.stream().anyMatch(e -> e.contains("3.000")));
    }

    @Test
    void validaFormatosCifNifNie() {
        assertTrue(service.validarCifNif("12345678Z"));
        assertTrue(service.validarCifNif("X1234567L"));
        assertTrue(service.validarCifNif("B12345674"));
        assertFalse(service.validarCifNif("12345678A"));
        assertFalse(service.validarCifNif("B12345670"));
        assertFalse(service.validarCifNif(""));
        assertFalse(service.validarCifNif(null));
        assertFalse(service.validarCifNif("INVALIDO"));
    }

    @Test
    void facturaSinIdOSinLineasNoPuedeEmitirse() {
        Factura sinId = facturaValida();
        sinId.setId(null);
        assertTrue(service.validarParaEmision(sinId).stream().anyMatch(e -> e.contains("guardarse")));

        Factura sinLineas = facturaValida();
        when(lineaService.findByFacturaId(1L)).thenReturn(List.of());
        assertTrue(service.validarParaEmision(sinLineas).stream().anyMatch(e -> e.contains("al menos una línea")));
    }

    private Factura facturaValida() {
        Factura factura = new Factura();
        factura.setId(1L);
        factura.setNumero("F-GEN-2026-0001");
        factura.setSerie("GEN");
        factura.setFecha(LocalDate.of(2026, 6, 18));
        factura.setTipoFactura("ORDINARIA");
        factura.setCliente(clienteValido());
        factura.setBaseImponible(new BigDecimal("100"));
        factura.setTotalIva(new BigDecimal("21"));
        factura.setTotalRecargo(BigDecimal.ZERO);
        factura.setRetencionIrpf(BigDecimal.ZERO);
        factura.setTotal(new BigDecimal("121"));
        factura.setPagado(BigDecimal.ZERO);
        return factura;
    }

    private Cliente clienteValido() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente");
        cliente.setCif("B12345674");
        return cliente;
    }

    private FacturaLinea lineaValida() {
        FacturaLinea linea = new FacturaLinea();
        linea.setArticulo(new Articulo());
        linea.setCantidad(BigDecimal.ONE);
        linea.setPrecio(new BigDecimal("100"));
        linea.setIva(new BigDecimal("21"));
        return linea;
    }
}
