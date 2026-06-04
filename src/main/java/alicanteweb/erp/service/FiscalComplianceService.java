package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.SifModalidad;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FiscalComplianceService {

    public static final LocalDate LIMITE_SOCIEDADES = LocalDate.of(2026, 1, 1);
    public static final LocalDate LIMITE_RESTO_OBLIGADOS = LocalDate.of(2026, 7, 1);

    private final EmpresaConfigService empresaConfigService;
    private final VerifactuService verifactuService;
    private final FacturacionEventoService facturacionEventoService;

    public FiscalComplianceService(EmpresaConfigService empresaConfigService,
                                   VerifactuService verifactuService,
                                   FacturacionEventoService facturacionEventoService) {
        this.empresaConfigService = empresaConfigService;
        this.verifactuService = verifactuService;
        this.facturacionEventoService = facturacionEventoService;
    }

    public FiscalComplianceReport diagnosticar() {
        List<FiscalComplianceCheck> checks = new ArrayList<>();
        EmpresaConfig config = empresaConfigService.getConfiguracionActiva().orElse(null);

        checks.add(check(config != null, "EMPRESA_CONFIG", "Existe una configuracion de empresa activa."));
        if (config == null) {
            return new FiscalComplianceReport(false, null, checks);
        }

        SifModalidad modalidad = config.getSifModalidad() != null ? config.getSifModalidad() : SifModalidad.VERIFACTU;
        checks.add(check(noVacio(config.getCif()), "NIF_EMISOR", "La empresa tiene NIF/CIF configurado."));
        checks.add(check(noVacio(config.getNombreEmpresa()), "NOMBRE_EMISOR", "La empresa tiene razon social configurada."));
        checks.add(check(noVacio(config.getVerifactuNombreSistema()), "NOMBRE_SISTEMA", "El SIF tiene nombre identificativo."));
        checks.add(check(noVacio(config.getVerifactuVersionSistema()), "VERSION_SISTEMA", "El SIF tiene version identificativa."));
        checks.add(check(noVacio(config.getVerifactuIdDispositivo()), "ID_DISPOSITIVO", "El SIF tiene identificador de dispositivo o instalacion."));
        checks.add(check(Boolean.TRUE.equals(config.getDeclaracionResponsableEmitida()), "DECLARACION_RESPONSABLE", "La declaracion responsable del sistema esta emitida."));
        checks.add(check(noVacio(config.getDeclaracionResponsableVersion()), "DECLARACION_VERSION", "La declaracion responsable identifica la version declarada."));
        checks.add(check(noVacio(config.getDeclaracionResponsableHash()), "DECLARACION_HUELLA", "La declaracion responsable tiene huella SHA-256 registrada."));
        checks.add(check(facturacionEventoService.validarCadena(FacturacionEventoService.AMBITO_FACTURAS), "CADENA_EVENTOS_FACTURAS", "La cadena de eventos fiscales de facturas es valida."));

        if (modalidad == SifModalidad.VERIFACTU) {
            checks.add(check(Boolean.TRUE.equals(config.getVerifactuHabilitado()), "VERIFACTU_HABILITADO", "La modalidad VERIFACTU esta habilitada en empresa."));
            checks.add(check(noVacio(config.getVerifactuNifEmisor()), "VERIFACTU_NIF_EMISOR", "El NIF emisor usado en registros VERIFACTU esta configurado."));
            checks.add(check(verifactuService.isAeatAvailable(), "AEAT_DISPONIBLE", "AEAT, certificado y cliente SOAP estan disponibles para envio inmediato."));
        } else {
            checks.add(check(verifactuService.isEnabled(), "FIRMA_LOCAL", "La firma local del sistema esta disponible para modalidad NO VERIFACTU."));
            checks.add(check(facturacionEventoService.validarCadena(FacturacionEventoService.AMBITO_GLOBAL), "CADENA_EVENTOS_GLOBAL", "La cadena global de eventos del sistema es valida."));
        }

        boolean listo = checks.stream().allMatch(FiscalComplianceCheck::ok);
        return new FiscalComplianceReport(listo, modalidad, checks);
    }

    public void exigirListoParaEmision() {
        FiscalComplianceReport report = diagnosticar();
        if (!report.listoProduccion()) {
            String pendientes = report.checks().stream()
                .filter(check -> !check.ok())
                .map(check -> check.codigo() + ": " + check.descripcion())
                .reduce((a, b) -> a + "; " + b)
                .orElse("diagnostico fiscal incompleto");
            throw new IllegalStateException("No se puede emitir la factura: cumplimiento fiscal incompleto. Pendiente: " + pendientes);
        }
    }

    private FiscalComplianceCheck check(boolean ok, String codigo, String descripcion) {
        return new FiscalComplianceCheck(codigo, ok, descripcion);
    }

    private boolean noVacio(String value) {
        return value != null && !value.isBlank();
    }

    public record FiscalComplianceReport(boolean listoProduccion,
                                         SifModalidad modalidad,
                                         List<FiscalComplianceCheck> checks) {
    }

    public record FiscalComplianceCheck(String codigo, boolean ok, String descripcion) {
    }
}
