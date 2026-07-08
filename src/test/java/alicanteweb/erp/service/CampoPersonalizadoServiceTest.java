package alicanteweb.erp.service;

import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.repository.CampoPersonalizadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampoPersonalizadoServiceTest {

    @Mock CampoPersonalizadoRepository repository;
    CampoPersonalizadoService service;

    @BeforeEach
    void setUp() {
        service = new CampoPersonalizadoService(repository);
    }

    private CampoPersonalizado campo(String ambito, Long clienteId, String etiqueta) {
        CampoPersonalizado c = new CampoPersonalizado();
        c.setAmbito(ambito);
        c.setClienteId(clienteId);
        c.setEtiqueta(etiqueta);
        return c;
    }

    @Test
    void guardaGlobalYLimpiaCliente() {
        CampoPersonalizado c = campo(CampoPersonalizado.AMBITO_EMPRESA, 9L, "Nota");
        when(repository.save(c)).thenReturn(c);

        service.save(c);

        // Un campo global no debe quedar asociado a ningún cliente.
        assertNull(c.getClienteId());
    }

    @Test
    void rechazaClienteSinIdCuandoAmbitoCliente() {
        CampoPersonalizado c = campo(CampoPersonalizado.AMBITO_CLIENTE, null, "Ref");
        assertThrows(IllegalArgumentException.class, () -> service.save(c));
    }

    @Test
    void rechazaEtiquetaVacia() {
        assertThrows(IllegalArgumentException.class,
                () -> service.save(campo(CampoPersonalizado.AMBITO_EMPRESA, null, "  ")));
    }

    @Test
    void guardaCampoDeCliente() {
        CampoPersonalizado c = campo(CampoPersonalizado.AMBITO_CLIENTE, 3L, "Ref. proveedor");
        when(repository.save(c)).thenReturn(c);
        service.save(c);
        assertEquals(3L, c.getClienteId());
    }

    @Test
    void aplicablesDelegaEnElRepositorio() {
        when(repository.findAplicables("FACTURA", 5L)).thenReturn(List.of(new CampoPersonalizado()));
        assertEquals(1, service.aplicables("FACTURA", 5L).size());
    }
}
