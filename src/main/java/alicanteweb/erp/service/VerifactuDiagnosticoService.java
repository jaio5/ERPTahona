package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.EmpresaConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
public class VerifactuDiagnosticoService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuDiagnosticoService.class);

    @Value("${verifactu.keystore.path}")
    private String keystorePath;

    @Value("${verifactu.keystore.password}")
    private String keystorePassword;

    @Value("${verifactu.key.alias}")
    private String keyAlias;

    @Value("${verifactu.aeat.enabled:false}")
    private boolean aeatEnabled;

    @Value("${verifactu.aeat.endpoint}")
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
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("   DIAGNÓSTICO DE VERIFACTU");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("");

        // 1. Verificar servicio Verifactu
        verificarServicio();

        // 2. Verificar certificado
        verificarCertificado();

        // 3. Verificar configuración de empresa
        verificarEmpresa();

        // 4. Verificar endpoint AEAT
        verificarEndpoint();

        log.info("");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("   FIN DEL DIAGNÓSTICO");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("");
    }

    private void verificarServicio() {
        log.info("1️⃣  SERVICIO VERIFACTU");
        log.info("────────────────────────────────────────────────────────────");

        if (verifactuService.isEnabled()) {
            log.info("✅ Servicio Verifactu: HABILITADO");
            log.info("   El servicio está listo para generar evidencias firmadas");
        } else {
            log.warn("⚠️  Servicio Verifactu: DESHABILITADO");
            log.warn("   Razón: No se pudo cargar el certificado digital");
            log.warn("   Las evidencias se guardarán pero sin firma digital");
        }
        log.info("");
    }

    private void verificarCertificado() {
        log.info("2️⃣  CERTIFICADO DIGITAL");
        log.info("────────────────────────────────────────────────────────────");
        log.info("   Ruta configurada: {}", keystorePath);
        log.info("   Alias configurado: {}", keyAlias);

        try {
            // Intentar cargar el certificado
            InputStream is = getClass().getResourceAsStream(keystorePath);
            if (is == null) {
                // Intentar desde el sistema de archivos
                try {
                    is = new java.io.FileInputStream(keystorePath);
                } catch (Exception e) {
                    log.error("❌ Certificado NO encontrado");
                    log.error("   Ubicación buscada: {}", keystorePath);
                    log.error("   El certificado debe estar en src/main/resources/certs/");
                    log.info("");
                    log.info("   📝 PARA OBTENER UN CERTIFICADO:");
                    log.info("   ────────────────────────────────────────────────────");
                    log.info("   1. CERTIFICADO REAL (Producción):");
                    log.info("      - Web: https://www.sede.fnmt.gob.es/certificados");
                    log.info("      - Solicitar: Certificado de Persona Jurídica");
                    log.info("      - Empresa: GRUPO BABO, S.Coop.V.L.");
                    log.info("      - Exportar como .p12 y copiar a: {}", keystorePath);
                    log.info("");
                    log.info("   2. CERTIFICADO DE PRUEBA (Desarrollo):");
                    log.info("      - Web: https://www.agenciatributaria.es");
                    log.info("      - Buscar: Verifactu → Entorno de preproducción");
                    log.info("");
                    log.info("   3. CONTINUAR SIN CERTIFICADO:");
                    log.info("      - La app funcionará pero solo guardará evidencias locales");
                    log.info("      - NO enviará facturas a la AEAT");
                    log.info("");
                    return;
                }
            }

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(is, keystorePassword.toCharArray());
            is.close();

            Certificate cert = ks.getCertificate(keyAlias);
            if (cert instanceof X509Certificate) {
                X509Certificate x509 = (X509Certificate) cert;
                log.info("✅ Certificado encontrado y cargado correctamente");
                log.info("   Titular: {}", x509.getSubjectX500Principal().getName());
                log.info("   Emisor: {}", x509.getIssuerX500Principal().getName());
                log.info("   Válido desde: {}", x509.getNotBefore().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                log.info("   Válido hasta: {}", x509.getNotAfter().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                // Verificar si está caducado
                try {
                    x509.checkValidity();
                    log.info("   Estado: ✅ VÁLIDO");
                } catch (Exception e) {
                    log.error("   Estado: ❌ CADUCADO o NO VÁLIDO");
                    log.error("   Necesitas renovar el certificado");
                }

                // Obtener fingerprint
                try {
                    String fingerprint = verifactuService.getCertificateFingerprint();
                    log.info("   Fingerprint (SHA-256): {}", fingerprint != null ? fingerprint.substring(0, 20) + "..." : "N/A");
                } catch (Exception e) {
                    log.warn("   No se pudo obtener fingerprint: {}", e.getMessage());
                }

            } else {
                log.error("❌ El certificado no es de tipo X509");
            }

        } catch (Exception e) {
            log.error("❌ Error al verificar certificado: {}", e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Stack trace:", e);
            }
        }
        log.info("");
    }

    private void verificarEmpresa() {
        log.info("3️⃣  CONFIGURACIÓN DE EMPRESA");
        log.info("────────────────────────────────────────────────────────────");

        try {
            Optional<EmpresaConfig> empresaOpt = empresaConfigRepository.findActive();

            if (empresaOpt.isEmpty()) {
                log.error("❌ NO hay empresa configurada");
                log.error("   Necesitas configurar los datos de GRUPO BABO");
                log.info("");
                log.info("   📝 EJECUTA ESTE SCRIPT SQL:");
                log.info("   ────────────────────────────────────────────────────");
                log.info("   mysql -u root -p -e \"USE tahona; INSERT INTO empresa_config ...");
                log.info("   O ejecuta: scripts/configurar_verifactu.bat");
                log.info("");
                return;
            }

            EmpresaConfig empresa = empresaOpt.get();
            log.info("✅ Empresa configurada:");
            log.info("   Nombre: {}", empresa.getNombreEmpresa());
            log.info("   CIF: {}", empresa.getCif());
            log.info("   Dirección: {}", empresa.getDireccion());
            log.info("   Ciudad: {}, {}", empresa.getCiudad(), empresa.getProvincia());

            log.info("");
            log.info("   Configuración Verifactu:");
            if (Boolean.TRUE.equals(empresa.getVerifactuHabilitado())) {
                log.info("   ✅ Verifactu: HABILITADO");
                log.info("      NIF Emisor: {}", empresa.getVerifactuNifEmisor());
                log.info("      Sistema: {} v{}",
                    empresa.getVerifactuNombreSistema(),
                    empresa.getVerifactuVersionSistema());
                if (empresa.getVerifactuIdDispositivo() != null) {
                    log.info("      ID Dispositivo: {}", empresa.getVerifactuIdDispositivo());
                }
            } else {
                log.warn("   ⚠️  Verifactu: DESHABILITADO en configuración de empresa");
                log.warn("      Para habilitar, ejecuta:");
                log.warn("      UPDATE empresa_config SET verifactu_habilitado=TRUE WHERE activo=TRUE;");
            }

        } catch (Exception e) {
            log.error("❌ Error al verificar configuración de empresa: {}", e.getMessage());
        }
        log.info("");
    }

    private void verificarEndpoint() {
        log.info("4️⃣  CONEXIÓN A LA AEAT");
        log.info("────────────────────────────────────────────────────────────");
        log.info("   Endpoint: {}", aeatEndpoint);

        if (aeatEnabled) {
            log.info("   Estado: ✅ HABILITADO (verifactu.aeat.enabled=true)");

            // Determinar si es producción o preproducción
            if (aeatEndpoint.contains("prewww")) {
                log.info("   Entorno: 🧪 PREPRODUCCIÓN (desarrollo/pruebas)");
                log.info("   ✅ Correcto para pruebas iniciales");
            } else if (aeatEndpoint.contains("www2.agenciatributaria")) {
                log.warn("   Entorno: 🚀 PRODUCCIÓN");
                log.warn("   ⚠️  CUIDADO: Las facturas se enviarán a la AEAT REAL");
                log.warn("   Solo usa este entorno cuando estés 100% seguro");
            } else {
                log.warn("   Entorno: ❓ DESCONOCIDO");
            }

            log.info("");
            log.info("   📤 Las facturas se enviarán a la AEAT cuando:");
            log.info("      1. Crees una factura");
            log.info("      2. La pases a estado REVISION");
            log.info("      3. Apruebes y emitas la factura");

        } else {
            log.info("   Estado: ⚠️  DESHABILITADO (verifactu.aeat.enabled=false)");
            log.info("   Las facturas NO se enviarán a la AEAT");
            log.info("   Solo se guardarán evidencias locales");
            log.info("");
            log.info("   💡 PARA HABILITAR:");
            log.info("   ────────────────────────────────────────────────────");
            log.info("   1. Edita: src/main/resources/application.properties");
            log.info("   2. Cambia: verifactu.aeat.enabled=true");
            log.info("   3. Usa PREPRODUCCIÓN primero:");
            log.info("      verifactu.aeat.endpoint=https://prewww2.aeat.es/...");
            log.info("   4. Reinicia la aplicación");
        }
        log.info("");
    }

    /**
     * Genera un resumen ejecutivo del estado de Verifactu
     */
    public String generarResumenEstado() {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("   ESTADO DE VERIFACTU\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        // Servicio
        if (verifactuService.isEnabled()) {
            sb.append("✅ Servicio Verifactu: OPERATIVO\n");
        } else {
            sb.append("❌ Servicio Verifactu: DESHABILITADO (sin certificado)\n");
        }

        // Empresa
        Optional<EmpresaConfig> empresaOpt = empresaConfigRepository.findActive();
        if (empresaOpt.isPresent() && Boolean.TRUE.equals(empresaOpt.get().getVerifactuHabilitado())) {
            sb.append("✅ Empresa configurada con Verifactu habilitado\n");
        } else {
            sb.append("❌ Empresa no configurada o Verifactu deshabilitado\n");
        }

        // AEAT
        if (aeatEnabled) {
            sb.append("✅ Envío a AEAT: HABILITADO\n");
            if (aeatEndpoint.contains("prewww")) {
                sb.append("   🧪 Entorno: PREPRODUCCIÓN\n");
            } else {
                sb.append("   🚀 Entorno: PRODUCCIÓN\n");
            }
        } else {
            sb.append("⚠️  Envío a AEAT: DESHABILITADO (solo evidencias locales)\n");
        }

        sb.append("\n═══════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }
}

