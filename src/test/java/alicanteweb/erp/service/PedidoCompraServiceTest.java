package alicanteweb.erp.service;

import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.entities.PedidoCompraSerieSequence;
import alicanteweb.erp.repository.PedidoCompraRepository;
import alicanteweb.erp.repository.PedidoCompraSerieSequenceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoCompraServiceTest {

    @Mock
    PedidoCompraRepository repository;

    @Mock
    PedidoCompraSerieSequenceRepository sequenceRepository;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    PedidoCompraService service;

    @Test
    void generarNumero_reintentaCuandoLaSecuenciaSeCreaConcurrentemente() {
        PedidoCompraSerieSequence sequence = new PedidoCompraSerieSequence();
        sequence.setSerie("PC");
        sequence.setEjercicio(2026);
        sequence.setUltimoNumero(4L);

        when(sequenceRepository.findBySerieAndEjercicio("PC", 2026))
            .thenReturn(Optional.empty(), Optional.of(sequence));
        when(repository.findMaxNumeroSecuencialByPrefijo("PC-2026-%", 8))
            .thenReturn(4L);
        when(sequenceRepository.saveAndFlush(any(PedidoCompraSerieSequence.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate"))
            .thenAnswer(invocation -> invocation.getArgument(0));

        String numero = service.generarNumero(LocalDate.of(2026, 4, 24));

        assertEquals("PC-2026-00005", numero);
        verify(entityManager).clear();
    }

    @Test
    void save_asignaNumeroAutomaticoCuandoLlegaVacio() {
        PedidoCompraSerieSequence sequence = new PedidoCompraSerieSequence();
        sequence.setSerie("PC");
        sequence.setEjercicio(2026);
        sequence.setUltimoNumero(1L);

        PedidoCompra pedido = new PedidoCompra();
        pedido.setFecha(LocalDate.of(2026, 2, 1));

        when(sequenceRepository.findBySerieAndEjercicio("PC", 2026))
            .thenReturn(Optional.of(sequence));
        when(sequenceRepository.saveAndFlush(any(PedidoCompraSerieSequence.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any(PedidoCompra.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        PedidoCompra guardado = service.save(pedido);

        assertEquals("PC-2026-00002", guardado.getNumero());
        verify(repository).save(pedido);
    }
}
