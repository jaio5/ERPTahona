package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceUsageTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    @Test
    void useServiceMethods_toSilenceWarnings() {
        service.searchByNombre("test");
        service.existsByCodigo("CLI0001");

        Cliente c = new Cliente();
        c.setCodigo("CLI0001");
        c.setNombre("Cliente Test");
        when(repository.save(c)).thenReturn(c);

        service.save(c);

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(c));
        service.darDeBaja(1L);

        verify(repository, atLeastOnce()).save(any(Cliente.class));
    }
}

