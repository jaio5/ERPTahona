package alicanteweb.erp.service;

import alicanteweb.erp.controller.dto.CampoImpresionView;
import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.enums.CampoSistema;
import alicanteweb.erp.repository.CampoPersonalizadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampoPersonalizadoServiceTest {

    @Mock CampoPersonalizadoRepository repository;
    @Mock EmpresaConfigService empresaConfigService;
    CampoPersonalizadoService service;

    @BeforeEach
    void setUp() {
        service = new CampoPersonalizadoService(repository, empresaConfigService);
    }

    private CampoPersonalizado propio(String etiqueta, String valor) {
        CampoPersonalizado c = new CampoPersonalizado();
        c.setOrigen(CampoPersonalizado.ORIGEN_PROPIO);
        c.setEtiqueta(etiqueta);
        c.setValor(valor);
        c.setVisibilidad(CampoPersonalizado.VIS_TODOS);
        return c;
    }

    private CampoPersonalizado sistema(CampoSistema cs) {
        CampoPersonalizado c = new CampoPersonalizado();
        c.setOrigen(CampoPersonalizado.ORIGEN_SISTEMA);
        c.setClaveSistema(cs.getClave());
        c.setVisibilidad(CampoPersonalizado.VIS_TODOS);
        return c;
    }

    @Test
    void guardaCampoPropio() {
        CampoPersonalizado c = propio("Forma de pago", "30 días");
        when(repository.save(c)).thenReturn(c);
        service.save(c);
        assertEquals("Forma de pago", c.getEtiqueta());
    }

    @Test
    void rechazaEtiquetaVaciaEnCampoPropio() {
        assertThrows(IllegalArgumentException.class, () -> service.save(propio("  ", "x")));
    }

    @Test
    void guardaCampoSistemaYLimpiaValor() {
        CampoPersonalizado c = sistema(CampoSistema.EMPRESA_REGISTRO_SANITARIO);
        c.setValor("no debería persistir");
        when(repository.save(c)).thenReturn(c);
        service.save(c);
        assertNull(c.getValor(), "El valor de un campo de sistema no se almacena");
    }

    @Test
    void rechazaClaveDeSistemaInvalida() {
        CampoPersonalizado c = new CampoPersonalizado();
        c.setOrigen(CampoPersonalizado.ORIGEN_SISTEMA);
        c.setClaveSistema("NO_EXISTE");
        c.setVisibilidad(CampoPersonalizado.VIS_TODOS);
        assertThrows(IllegalArgumentException.class, () -> service.save(c));
    }

    @Test
    void visibilidadSoloExigeAlMenosUnCliente() {
        CampoPersonalizado c = propio("Ref", "x");
        c.setVisibilidad(CampoPersonalizado.VIS_SOLO);
        assertThrows(IllegalArgumentException.class, () -> service.save(c));
    }

    @Test
    void visibilidadTodosLimpiaLosClientes() {
        CampoPersonalizado c = propio("Ref", "x");
        c.getClientes().add(7L);
        when(repository.save(c)).thenReturn(c);
        service.save(c);
        assertTrue(c.getClientes().isEmpty());
    }

    @Test
    void aplicablesResuelveElValorDeUnCampoDeSistema() {
        EmpresaConfig empresa = new EmpresaConfig();
        empresa.setRegistroSanitario("ES 10.1234/CA");
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa));
        when(repository.findActivosConClientes())
            .thenReturn(List.of(sistema(CampoSistema.EMPRESA_REGISTRO_SANITARIO)));

        List<CampoImpresionView> vistas = service.aplicables(CampoPersonalizado.DOC_FACTURA, null);

        assertEquals(1, vistas.size());
        assertEquals("Registro sanitario", vistas.get(0).getEtiqueta());
        assertEquals("ES 10.1234/CA", vistas.get(0).getValor());
    }

    @Test
    void aplicablesFiltraPorVisibilidadSolo() {
        lenient().when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.empty());
        CampoPersonalizado c = propio("Aviso", "solo para VIP");
        c.setVisibilidad(CampoPersonalizado.VIS_SOLO);
        c.setClientes(Set.of(5L));
        when(repository.findActivosConClientes()).thenReturn(List.of(c));

        Cliente otro = new Cliente();
        otro.setId(9L);
        Cliente elegido = new Cliente();
        elegido.setId(5L);

        assertTrue(service.aplicables(CampoPersonalizado.DOC_FACTURA, otro).isEmpty(),
            "No debe imprimirse a un cliente fuera del conjunto SOLO");
        assertEquals(1, service.aplicables(CampoPersonalizado.DOC_FACTURA, elegido).size(),
            "Debe imprimirse al cliente del conjunto SOLO");
    }
}
