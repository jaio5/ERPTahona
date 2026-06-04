package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerifactuHardeningService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuHardeningService.class);

    private final EmpresaConfigService empresaConfigService;
    private final FacturaRepository facturaRepository;
    private final VerifactuEvidenceRepository evidenceRepository;
    private final VerifactuService verifactuService;

    @Value("${verifactu.keystore.path:}")
    private String keystorePath;

    public VerifactuHardeningService(EmpresaConfigService empresaConfigService,
                                      FacturaRepository facturaRepository,
                                      VerifactuEvidenceRepository evidenceRepository,
                                      VerifactuService verifactuService) {
        this.empresaConfigService = empresaConfigService;
        this.facturaRepository = facturaRepository;
        this.evidenceRepository = evidenceRepository;
        this.verifactuService = verifactuService;
    }

    /**
     * Valida que el certificado VeriFactu existe y es accesible.
     * @return Resultado de la validación
     */
    public ValidacionCertificado validarCertificado() {
        try {
            if (keystorePath == null || keystorePath.isBlank()) {
                return ValidacionCertificado.error("Ruta del keystore no configurada");
            }
            Path path = Paths.get(keystorePath);
            if (!Files.exists(path)) {
                return ValidacionCertificado.error("Archivo de keystore no encontrado: " + keystorePath);
            }
            if (!Files.isReadable(path)) {
                return ValidacionCertificado.error("Archivo de keystore no tiene permisos de lectura: " + keystorePath);
            }
            return ValidacionCertificado.ok("Certificado VeriFactu válido y accesible");
        } catch (Exception e) {
            log.error("Error validando certificado VeriFactu", e);
            return ValidacionCertificado.error("Error: " + e.getMessage());
        }
    }

    /**
     * Verifica la integridad de la cadena de hashes VeriFactu programada (diario a las 3:00 AM).
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void verificarIntegridadCadenaHashes() {
        log.info("Verificando integridad de la cadena de hashes VeriFactu...");
        try {
            EmpresaConfig config = empresaConfigService.getConfiguracionActivaOrThrow();
            if (!empresaConfigService.isFuncionamientoVerifactuVigente(config)) {
                log.info("VeriFactu no está activo o no vigente. Saltando verificación.");
                return;
            }

            List<VerifactuEvidence> evidencias = evidenceRepository.findAll();
            if (evidencias.isEmpty()) {
                log.info("No hay evidencias VeriFactu para verificar.");
                return;
            }

            int errores = 0;
            for (int i = 1; i < evidencias.size(); i++) {
                VerifactuEvidence anterior = evidencias.get(i - 1);
                VerifactuEvidence actual = evidencias.get(i);
                if (actual.getHashAnterior() != null
                        && !actual.getHashAnterior().equals(anterior.getHash())) {
                    log.error("CADENA ROTA: Evidencia {} (hash={}) referencia hash_anterior={} " +
                                    "pero el hash de la evidencia anterior es {}",
                            actual.getId(), actual.getHash(), actual.getHashAnterior(), anterior.getHash());
                    errores++;
                }
            }

            if (errores > 0) {
                log.error("Se encontraron {} errores en la cadena de hashes VeriFactu", errores);
            } else {
                log.info("Cadena de hashes VeriFactu íntegra ({} evidencias verificadas)", evidencias.size());
            }
        } catch (Exception e) {
            log.error("Error verificando cadena de hashes VeriFactu", e);
        }
    }

    /**
     * Verifica que las facturas emitidas tengan su correspondiente evidencia VeriFactu
     * cuando VeriFactu está activo.
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void verificarFacturasSinEvidencia() {
        log.info("Verificando facturas sin evidencia VeriFactu...");
        try {
            EmpresaConfig config = empresaConfigService.getConfiguracionActivaOrThrow();
            if (!empresaConfigService.isFuncionamientoVerifactuVigente(config)) {
                return;
            }

            List<Factura> emitidas = facturaRepository.findAll().stream()
                    .filter(f -> "EMITIDA".equals(f.getEstado()))
                    .toList();

            int pendientes = 0;
            for (Factura f : emitidas) {
                java.util.Optional<VerifactuEvidence> evidencia = evidenceRepository.findByFacturaId(String.valueOf(f.getId()));
                if (evidencia.isEmpty()) {
                    log.warn("Factura {} emitida sin evidencia VeriFactu", f.getNumero());
                    pendientes++;
                }
            }

            if (pendientes > 0) {
                log.warn("{} factura(s) emitida(s) sin evidencia VeriFactu", pendientes);
            } else {
                log.info("Todas las facturas emitidas tienen evidencia VeriFactu.");
            }
        } catch (Exception e) {
            log.error("Error verificando facturas sin evidencia", e);
        }
    }

    /**
     * Resultado de validación del certificado.
     */
    public record ValidacionCertificado(boolean valido, String mensaje, LocalDateTime timestamp) {
        public static ValidacionCertificado ok(String mensaje) {
            return new ValidacionCertificado(true, mensaje, LocalDateTime.now());
        }
        public static ValidacionCertificado error(String mensaje) {
            return new ValidacionCertificado(false, mensaje, LocalDateTime.now());
        }
    }
}
