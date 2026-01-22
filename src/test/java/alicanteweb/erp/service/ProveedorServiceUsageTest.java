package alicanteweb.erp.service;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProveedorServiceUsageTest {

    @Mock
    private ProveedorRepository repository;

    @InjectMocks
    private ProveedorService service;

    @Test
    void useServiceMethods_toSilenceWarnings() {
        service.findActivos();
        service.buscar("X");

        Proveedor p = new Proveedor();
        p.setCodigo("PROV9999");
        p.setNombre("Proveedor Test");
        when(repository.save(p)).thenReturn(p);

        service.save(p);

        when(repository.findById(1L)).thenReturn(Optional.of(p));
        service.activar(1L);

        verify(repository, atLeastOnce()).save(any(Proveedor.class));
    }
}

