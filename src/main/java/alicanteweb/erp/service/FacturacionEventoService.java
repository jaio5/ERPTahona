package alicanteweb.erp.service;

import alicanteweb.erp.entities.FacturacionEvento;
import alicanteweb.erp.repository.FacturacionEventoRepository;
import alicanteweb.erp.util.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    private String ambitoNormalizado(String ambito) {
        return ambito == null || ambito.isBlank() ? AMBITO_GLOBAL : ambito.trim().toUpperCase();
    }

    private String valor(String value) {
        return value != null ? value : "";
    }
}
