package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.FacturaLineaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaLineaServiceTest {

    @Mock
    FacturaLineaRepository repository;

    @InjectMocks
    FacturaLineaService service;

    @Test
    void save_rechazaLineaDeFacturaEmitida() {
        Factura factura = new Factura();
        factura.setEstado("EMITIDA");

        FacturaLinea linea = new FacturaLinea();
        linea.setFactura(factura);

        assertThrows(IllegalStateException.class, () -> service.save(linea));
        verify(repository, never()).save(linea);
    }

    @Test
    void deleteById_rechazaLineaDeFacturaEnviada() {
        Factura factura = new Factura();
        factura.setVerifactuEnviada(true);

        FacturaLinea linea = new FacturaLinea();
        linea.setId(7L);
        linea.setFactura(factura);

        when(repository.findById(7L)).thenReturn(Optional.of(linea));

        assertThrows(IllegalStateException.class, () -> service.deleteById(7L));
        verify(repository, never()).deleteById(7L);
    }
}
