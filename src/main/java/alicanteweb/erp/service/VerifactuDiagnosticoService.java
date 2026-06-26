package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Servicio de diagnóstico para Verifactu
 * Verifica que toda la configuración esté correcta
 */
@Service
@ConditionalOnProperty(name = "verifactu.diagnostics.enabled", havingValue = "true")
public class VerifactuDiagnosticoService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuDiagnosticoService.class);

    @Value("${verifactu.keystore.path:}")
    private String keystorePath;

    @Value("${verifactu.keystore.password:}")
    private String keystorePassword;

    @Value("${verifactu.key.alias:}")
    private String keyAlias;

    @Value("${verifactu.aeat.enabled:false}")
    private boolean aeatEnabled;

    @Value("${verifactu.aeat.endpoint:}")
    private String aeatEndpoint;

    private final EmpresaConfigRepository empresaConfigRepository;
    private final VerifactuService verifactuService;

    public VerifactuDiagnosticoService(
            EmpresaConfigRepository empresaConfigRepository,
            VerifactuService verifactuService) {
        this.empresaConfigRepository = empresaConfigRepository;
        this.verifactuService = verifactuService;
    }

    @PostConstruct
    public void diagnosticar() {
        log.info("--- DIAGNÓSTICO DE VERIFACTU ---");
        verificarServicio();
        verificarCertificado();
        verificarEmpresa();
        verificarEndpoint();
        log.info("--- FIN DIAGNÓSTICO VERIFACTU ---");
    }

    private void verificarServicio() {
        if (verifactuService.isEnabled()) {
            log.info("Verifactu: servicio HABILITADO y listo para generar evidencias firmadas");
        } else {
            log.warn("Verifactu: servicio DESHABILITADO (no se pudo cargar el certificado). Las evidencias se guardarán sin firma digital.");
        }
    }

    private void verificarCertificado() {
        log.info("Certificado: ruta={}, alias={}", keystorePath, keyAlias);

        InputStream is = null;
        try {
            is = VerifactuService.openKeystoreStream(keystorePath);
            if (is == null) {
                log.warn("Certificado NO encontrado: {}. La app funcionará sin firma digital.", keystorePath);
                return;
            }

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(is, keystorePassword == null ? new char[0] : keystorePassword.toCharArray());

            Certificate cert = ks.getCertificate(keyAlias);
            if (cert instanceof X509Certificate x509) {
                log.info("Certificado cargado - Titular: {}, Válido hasta: {}",
                    x509.getSubjectX500Principal().getName(), x509.getNotAfter());
                try {
                    x509.checkValidity();
                    log.info("Certificado: VÁLIDO");
                } catch (Exception e) {
                    log.warn("Certificado: CADUCADO o NO VÁLIDO - {}", e.getMessage());
                }
            } else {
                log.warn("Alias de certificado incorrecto o no es X509: {}", keyAlias);
            }
        } catch (Exception e) {
            log.warn("Error al verificar certificado: {}", e.getMessage());
            if (log.isDebugEnabled()) log.debug("Stack trace:", e);
        } finally {
            if (is != null) try { is.close(); } catch (Exception ignored) {}
        }
    }

    private void verificarEmpresa() {
        try {
            Optional<EmpresaConfig> empresaOpt = empresaConfigRepository.findFirstByActivoTrue();
            if (empresaOpt.isEmpty()) {
                log.error("No hay empresa configurada. Ejecuta el script scripts/configurar_verifactu.bat o inserta los datos en empresa_config.");
                return;
            }
            EmpresaConfig empresa = empresaOpt.get();
            if (Boolean.TRUE.equals(empresa.getVerifactuHabilitado())) {
                log.info("Empresa '{}' (CIF: {}) con Verifactu HABILITADO - NIF emisor: {}",
                    empresa.getNombreEmpresa(), empresa.getCif(), empresa.getVerifactuNifEmisor());
            } else {
                log.warn("Empresa '{}' encontrada pero Verifactu DESHABILITADO. Actualiza verifactu_habilitado=TRUE en empresa_config.",
                    empresa.getNombreEmpresa());
            }
        } catch (Exception e) {
            log.error("Error al verificar configuración de empresa: {}", e.getMessage());
        }
    }

    private void verificarEndpoint() {
        if (aeatEnabled) {
            String entorno = aeatEndpoint.contains("prewww") ? "PREPRODUCCION" : "PRODUCCION";
            log.info("Envío a AEAT: HABILITADO - Endpoint: {} [{}]", aeatEndpoint, entorno);
            if ("PRODUCCION".equals(entorno)) {
                log.warn("ATENCION: Modo PRODUCCION activo. Las facturas se enviarán a la AEAT real.");
            }
        } else {
            log.info("Envío a AEAT: DESHABILITADO (verifactu.aeat.enabled=false). Solo evidencias locales.");
        }
    }

    /**
     * Genera un resumen del estado de Verifactu para mostrar en la UI.
     */
    public String generarResumenEstado() {
        StringBuilder sb = new StringBuilder("ESTADO DE VERIFACTU\n\n");

        sb.append(verifactuService.isEnabled()
            ? "Servicio: OPERATIVO\n"
            : "Servicio: DESHABILITADO (sin certificado)\n");

        Optional<EmpresaConfig> empresaOpt = empresaConfigRepository.findFirstByActivoTrue();
        sb.append(empresaOpt.isPresent() && Boolean.TRUE.equals(empresaOpt.get().getVerifactuHabilitado())
            ? "Empresa: configurada con Verifactu habilitado\n"
            : "Empresa: no configurada o Verifactu deshabilitado\n");

        if (aeatEnabled) {
            String entorno = aeatEndpoint.contains("prewww") ? "PREPRODUCCION" : "PRODUCCION";
            sb.append("Envío AEAT: HABILITADO [").append(entorno).append("]\n");
        } else {
            sb.append("Envío AEAT: DESHABILITADO (solo evidencias locales)\n");
        }

        return sb.toString();
    }
}

