package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaConfigServiceVerifactuLegalTest {

    @Mock
    EmpresaConfigRepository repository;

    @InjectMocks
    EmpresaConfigService service;

    @Test
    void iniciarFuncionamientoVerifactuActivaYRegistraFechaInicio() {
        EmpresaConfig config = empresa(false);
        when(repository.findByActivoTrue()).thenReturn(List.of(config));
        when(repository.save(any(EmpresaConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmpresaConfig actual = service.iniciarFuncionamientoVerifactu(" B12345678 ");

        assertTrue(actual.getVerifactuHabilitado());
        assertEquals("B12345678", actual.getVerifactuNifEmisor());
        assertEquals(LocalDate.now(), actual.getVerifactuFechaInicio());
        assertNull(actual.getVerifactuFechaRenuncia());
    }

    @Test
    void saveBloqueaDesactivacionDirectaSiVerifactuEstaVigente() {
        EmpresaConfig actual = empresa(true);
        EmpresaConfig cambio = empresa(false);
        when(repository.findById(1L)).thenReturn(Optional.of(actual));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.save(cambio));

        assertTrue(ex.getMessage().contains("no puede desactivarse directamente"));
    }

    @Test
    void renunciaFinDeAnioMantieneFuncionamientoVigenteHastaElUltimoDia() {
        EmpresaConfig config = empresa(true);
        when(repository.findByActivoTrue()).thenReturn(List.of(config));
        when(repository.save(any(EmpresaConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmpresaConfig actual = service.programarRenunciaVerifactuFinDeAnio();

        assertEquals(LocalDate.of(LocalDate.now().getYear(), 12, 31), actual.getVerifactuFechaRenuncia());
        assertTrue(service.isFuncionamientoVerifactuVigente(actual));
        assertFalse(service.isFuncionamientoVerifactuVigente(empresa(false)));
    }

    private EmpresaConfig empresa(boolean verifactu) {
        EmpresaConfig config = new EmpresaConfig();
        config.setId(1L);
        config.setNombreEmpresa("ERP Tahona");
        config.setCif("B12345678");
        config.setActivo(true);
        config.setVerifactuHabilitado(verifactu);
        if (verifactu) {
            config.setVerifactuFechaInicio(LocalDate.now());
        }
        return config;
    }
}
