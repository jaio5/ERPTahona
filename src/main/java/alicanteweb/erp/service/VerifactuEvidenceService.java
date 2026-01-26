package alicanteweb.erp.service;

import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Servicio que gestiona el almacenamiento y consulta de las evidencias Verifactu
 * (registros que contienen hash, firma y metadatos del envío a la AEAT).
 */
@Service
@Transactional(readOnly = true)
public class VerifactuEvidenceService {

    private static final Logger log = LoggerFactory.getLogger(VerifactuEvidenceService.class);
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_ENVIADO = "ENVIADO";
    private static final String ESTADO_ERROR = "ERROR";
    private static final String ESTADO_VERIFICADO = "VERIFICADO";

    private final VerifactuEvidenceRepository repository;
    private final VerifactuAEATService aeatService;
    private final VerifactuService verifactuService;

    public VerifactuEvidenceService(VerifactuEvidenceRepository repository,
                                   VerifactuAEATService aeatService,
                                   VerifactuService verifactuService) {
        this.repository = repository;
        this.aeatService = aeatService;
        this.verifactuService = verifactuService;
    }

    public List<VerifactuEvidence> findAll() {
        return repository.findAll();
    }

    public Optional<VerifactuEvidence> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<VerifactuEvidence> findByFacturaId(String facturaId) {
        return repository.findByFacturaId(facturaId);
    }

    public Optional<VerifactuEvidence> findByHash(String hash) {
        return repository.findByHash(hash);
    }

    public List<VerifactuEvidence> findBySerie(String serie) {
        return repository.findBySerieContainingIgnoreCase(serie);
    }

    public List<VerifactuEvidence> findByEstado(String estado) {
        return repository.findByEstado(estado);
    }

    public long countByEstado(String estado) {
        return repository.countByEstado(estado);
    }

    @Transactional
    public VerifactuEvidence save(VerifactuEvidence e) {
        return repository.save(e);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Crea y registra una evidencia en la BD y la envía a la AEAT.
     */
    @Transactional
    public VerifactuEvidence registrarEvidenciaAEAT(String datosFactura, String serie, String numero) throws Exception {
        log.info("Registrando evidencia VeriFactu para factura {}/{}", serie, numero);

        VerifactuEvidence evidencia = new VerifactuEvidence();
        evidencia.setFacturaId(datosFactura);
        evidencia.setSerie(serie);
        evidencia.setNumero(numero);
        evidencia.setFechaEmision(Instant.now());
        evidencia.setCreatedAt(Instant.now());
        evidencia.setEstado(ESTADO_PENDIENTE);

        try {
            // Obtener hash anterior para encadenar
            String hashAnterior = verifactuService.obtenerHashAnterior(serie);
            evidencia.setHashAnterior(hashAnterior);

            // Generar hash encadenado
            String hash = verifactuService.generarHashEncadenado(datosFactura, hashAnterior);
            evidencia.setHash(hash);

            // Firmar datos
            byte[] firma = aeatService.firmarDatos(datosFactura.getBytes(StandardCharsets.UTF_8));
            evidencia.setSignature(firma);

            // Obtener fingerprint del certificado
            String fingerprint = aeatService.getCertFingerprint();
            evidencia.setCertFingerprint(fingerprint);

            // Construir JSON de evidencia
            String jsonEvidencia = construirJsonEvidencia(evidencia, datosFactura);

            // Enviar a AEAT
            String respuesta = aeatService.enviarAEAT(jsonEvidencia);

            // Procesar respuesta
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("respuesta", respuesta);
            metadata.put("fechaEnvio", Instant.now().toString());
            evidencia.setMetadata(metadata);

            // Verificar si fue exitoso
            if (respuesta.contains("\"resultado\":\"OK\"")) {
                evidencia.setEstado(ESTADO_ENVIADO);
                evidencia.setFechaEnvio(Instant.now());
                log.info("Evidencia enviada exitosamente a AEAT");
            } else {
                evidencia.setEstado(ESTADO_ERROR);
                evidencia.setErrorMessage("Error en respuesta de AEAT");
                log.warn("Error en respuesta de AEAT: {}", respuesta);
            }

            // Guardar y retornar
            VerifactuEvidence saved = repository.save(evidencia);
            log.info("Evidencia guardada con ID: {}", saved.getId());
            return saved;

        } catch (Exception e) {
            log.error("Error registrando evidencia VeriFactu", e);
            evidencia.setEstado(ESTADO_ERROR);
            evidencia.setErrorMessage(e.getMessage());
            evidencia.setMetadata(Collections.singletonMap("error", e.getMessage()));
            repository.save(evidencia);
            throw e;
        }
    }

    /**
     * Reenvía una evidencia que falló previamente
     */
    @Transactional
    public VerifactuEvidence reenviarEvidencia(Long id) {
        log.info("Reenviando evidencia ID: {}", id);

        Optional<VerifactuEvidence> opt = repository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Evidencia no encontrada con ID: " + id);
        }

        VerifactuEvidence evidencia = opt.get();

        try {
            // Reconstruir datos para reenvío
            String datosFactura = evidencia.getFacturaId();
            String jsonEvidencia = construirJsonEvidencia(evidencia, datosFactura);

            // Reenviar a AEAT
            String respuesta = aeatService.reenviarAEAT(jsonEvidencia);

            // Actualizar metadata
            Map<String, Object> metadata = evidencia.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }
            metadata.put("respuestaReenvio", respuesta);
            metadata.put("fechaReenvio", Instant.now().toString());
            evidencia.setMetadata(metadata);

            // Actualizar estado
            if (respuesta.contains("\"resultado\":\"OK\"")) {
                evidencia.setEstado(ESTADO_ENVIADO);
                evidencia.setFechaEnvio(Instant.now());
                evidencia.setErrorMessage(null);
                log.info("Evidencia reenviada exitosamente");
            } else {
                evidencia.setEstado(ESTADO_ERROR);
                evidencia.setErrorMessage("Error en reenvío");
                log.warn("Error en reenvío: {}", respuesta);
            }

            return repository.save(evidencia);

        } catch (Exception e) {
            log.error("Error reenviando evidencia", e);
            evidencia.setErrorMessage(e.getMessage());
            repository.save(evidencia);
            return evidencia;
        }
    }

    /**
     * Verifica el estado de una evidencia en la AEAT
     */
    @Transactional
    public VerifactuEvidence verificarEstadoAEAT(Long id) {
        Optional<VerifactuEvidence> opt = repository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Evidencia no encontrada con ID: " + id);
        }

        VerifactuEvidence evidencia = opt.get();

        try {
            String respuesta = aeatService.verificarEstadoAEAT(evidencia.getHash());

            Map<String, Object> metadata = evidencia.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }
            metadata.put("verificacion", respuesta);
            metadata.put("fechaVerificacion", Instant.now().toString());
            evidencia.setMetadata(metadata);

            if (respuesta.contains("\"resultado\":\"OK\"")) {
                evidencia.setEstado(ESTADO_VERIFICADO);
            }

            return repository.save(evidencia);

        } catch (Exception e) {
            log.error("Error verificando estado en AEAT", e);
            // Registrar el error y devolver la evidencia sin lanzar la excepción
            evidencia.setErrorMessage(e.getMessage());
            repository.save(evidencia);
            return evidencia;
        }
    }

    /**
     * Valida la integridad de la cadena de evidencias
     */
    public boolean validarCadenaIntegridad(String serie) {
        return verifactuService.validarCadenaIntegridad(serie);
    }

    /**
     * Construye el JSON de evidencia para enviar a AEAT
     */
    private String construirJsonEvidencia(VerifactuEvidence evidencia, String datosFactura) {
        byte[] sig = evidencia.getSignature();
        String firmaB64 = java.util.Base64.getEncoder().encodeToString(sig == null ? new byte[0] : sig);

        String serie = evidencia.getSerie() != null ? evidencia.getSerie() : "";
        String numero = evidencia.getNumero() != null ? evidencia.getNumero() : "";
        String hash = evidencia.getHash() != null ? evidencia.getHash() : "";
        String hashAnterior = evidencia.getHashAnterior() != null ? evidencia.getHashAnterior() : "";
        String certFp = evidencia.getCertFingerprint() != null ? evidencia.getCertFingerprint() : "";
        String fecha = evidencia.getFechaEmision() != null ? evidencia.getFechaEmision().toString() : Instant.now().toString();

        return String.format("""
            {
                "facturaId": "%s",
                "serie": "%s",
                "numero": "%s",
                "hash": "%s",
                "hashAnterior": "%s",
                "firma": "%s",
                "certFingerprint": "%s",
                "fechaEmision": "%s"
            }
            """,
            datosFactura,
            serie,
            numero,
            hash,
            hashAnterior,
            firmaB64,
            certFp,
            fecha
        );
    }
}
