package alicanteweb.erp.service;

import alicanteweb.erp.entities.FacturacionEvento;
import alicanteweb.erp.repository.FacturacionEventoRepository;
import alicanteweb.erp.util.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FacturacionEventoService {

    public static final String AMBITO_GLOBAL = "GLOBAL";
    public static final String AMBITO_FACTURAS = "FACTURAS";
    public static final String AMBITO_BACKUP = "BACKUP";
    public static final String NORMATIVA_SIF = "RD1007_2023_HAC1177_2024";

    private final FacturacionEventoRepository repository;

    public FacturacionEventoService(FacturacionEventoRepository repository) {
        this.repository = repository;
    }

    public List<FacturacionEvento> findByAmbito(String ambito) {
        return repository.findByAmbitoOrderByFechaAscIdAsc(ambitoNormalizado(ambito));
    }

    @Transactional
    public FacturacionEvento registrarEvento(String ambito, String tipoEvento, String referencia, Map<String, Object> metadata) {
        String ambitoNormalizado = ambitoNormalizado(ambito);
        String hashAnterior = repository.findFirstByAmbitoOrderByFechaDescIdDesc(ambitoNormalizado)
                .map(FacturacionEvento::getHashActual)
                .orElse(null);

        Map<String, Object> metadataSegura = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        metadataSegura.put("timestamp", LocalDateTime.now().toString());
        metadataSegura.putIfAbsent("versionNormativa", NORMATIVA_SIF);

        String payload = ambitoNormalizado + "|" + tipoEvento + "|" + valor(referencia) + "|" + metadataSegura;
        String payloadHash;
        String hashActual;
        try {
            payloadHash = HashUtils.sha256Hex(payload);
            hashActual = HashUtils.sha256Hex(payloadHash + "|" + valor(hashAnterior));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash del evento de facturacion", e);
        }

        FacturacionEvento evento = new FacturacionEvento();
        evento.setFecha(LocalDateTime.now());
        evento.setAmbito(ambitoNormalizado);
        evento.setTipoEvento(tipoEvento);
        evento.setReferencia(referencia);
        evento.setHashAnterior(hashAnterior);
        evento.setPayloadHash(payloadHash);
        evento.setHashActual(hashActual);
        evento.setVersionNormativa(valorMetadata(metadataSegura, "versionNormativa"));
        evento.setModalidadSif(valorMetadata(metadataSegura, "modalidadSif"));
        evento.setOrigenSistema(valorMetadata(metadataSegura, "origenSistema"));
        evento.setMetadata(metadataSegura);
        return repository.save(evento);
    }

    public boolean validarCadena(String ambito) {
        List<FacturacionEvento> eventos = repository.findByAmbitoOrderByFechaAscIdAsc(ambitoNormalizado(ambito));
        String anterior = null;
        for (FacturacionEvento evento : eventos) {
            if (anterior != null && !anterior.equals(evento.getHashAnterior())) {
                return false;
            }
            anterior = evento.getHashActual();
        }
        return true;
    }

    public Path exportarEventosCsv(String ambito, Path destino) {
        try {
            String ambitoNormalizado = ambitoNormalizado(ambito);
            List<FacturacionEvento> eventos = repository.findByAmbitoOrderByFechaAscIdAsc(ambitoNormalizado);
            Path archivo = destino != null ? destino : Path.of("impresiones", "eventos-fiscales-" + ambitoNormalizado.toLowerCase() + ".csv");
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }

            StringBuilder csv = new StringBuilder();
            csv.append("id;fecha;ambito;tipo_evento;referencia;hash_anterior;hash_actual;payload_hash;version_normativa;modalidad_sif;origen_sistema\n");
            for (FacturacionEvento evento : eventos) {
                csv.append(valor(evento.getId())).append(';')
                    .append(valor(evento.getFecha())).append(';')
                    .append(csv(evento.getAmbito())).append(';')
                    .append(csv(evento.getTipoEvento())).append(';')
                    .append(csv(evento.getReferencia())).append(';')
                    .append(csv(evento.getHashAnterior())).append(';')
                    .append(csv(evento.getHashActual())).append(';')
                    .append(csv(evento.getPayloadHash())).append(';')
                    .append(csv(evento.getVersionNormativa())).append(';')
                    .append(csv(evento.getModalidadSif())).append(';')
                    .append(csv(evento.getOrigenSistema())).append('\n');
            }
            Files.writeString(archivo, csv.toString(), StandardCharsets.UTF_8);
            return archivo;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo exportar el registro fiscal de eventos", e);
        }
    }

    private String ambitoNormalizado(String ambito) {
        return ambito == null || ambito.isBlank() ? AMBITO_GLOBAL : ambito.trim().toUpperCase();
    }

    private String valor(String value) {
        return value != null ? value : "";
    }

    private String valor(Object value) {
        return value != null ? value.toString() : "";
    }

    private String csv(String value) {
        String clean = valor(value).replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }

    private String valorMetadata(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : null;
    }
}
