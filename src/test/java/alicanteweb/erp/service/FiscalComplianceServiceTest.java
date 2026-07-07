package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.SifModalidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalComplianceServiceTest {

    @Mock
    EmpresaConfigService empresaConfigService;

    @Mock
    VerifactuService verifactuService;

    @Mock
    FacturacionEventoService facturacionEventoService;

    @InjectMocks
    FiscalComplianceService service;

    @Test
    void diagnosticar_verifactuListoMarcaProduccionLista() {
        EmpresaConfig config = configBase(SifModalidad.VERIFACTU);
        config.setVerifactuHabilitado(true);
        config.setVerifactuNifEmisor("B12345678");

        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(config));
        when(facturacionEventoService.validarCadena(FacturacionEventoService.AMBITO_FACTURAS)).thenReturn(true);
        when(verifactuService.isAeatAvailable()).thenReturn(true);

        FiscalComplianceService.FiscalComplianceReport report = service.diagnosticar();

        assertTrue(report.listoProduccion());
        assertTrue(report.checks().stream().allMatch(FiscalComplianceService.FiscalComplianceCheck::ok));
    }

    @Test
    void diagnosticar_noVerifactuSinFirmaNiDeclaracionMarcaPendiente() {
        EmpresaConfig config = configBase(SifModalidad.NO_VERIFACTU);
        config.setDeclaracionResponsableEmitida(false);

        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(config));
        when(facturacionEventoService.validarCadena(FacturacionEventoService.AMBITO_FACTURAS)).thenReturn(true);
        when(facturacionEventoService.validarCadena(FacturacionEventoService.AMBITO_GLOBAL)).thenReturn(true);
        when(verifactuService.isEnabled()).thenReturn(false);

        FiscalComplianceService.FiscalComplianceReport report = service.diagnosticar();

        assertFalse(report.listoProduccion());
        assertTrue(report.checks().stream().anyMatch(check -> "FIRMA_LOCAL".equals(check.codigo()) && !check.ok()));
        assertTrue(report.checks().stream().anyMatch(check -> "DECLARACION_RESPONSABLE".equals(check.codigo()) && !check.ok()));
    }

    private EmpresaConfig configBase(SifModalidad modalidad) {
        EmpresaConfig config = new EmpresaConfig();
        config.setNombreEmpresa("ERP Tahona");
        config.setCif("B12345678");
        config.setSifModalidad(modalidad);
        config.setVerifactuNombreSistema("ERP Tahona");
        config.setVerifactuVersionSistema("0.0.1");
        config.setVerifactuIdDispositivo("INST-001");
        config.setDeclaracionResponsableEmitida(true);
        config.setDeclaracionResponsableFecha(LocalDateTime.now());
        config.setDeclaracionResponsableVersion("0.0.1");
        config.setDeclaracionResponsableHash("abc123");
        return config;
    }
}
