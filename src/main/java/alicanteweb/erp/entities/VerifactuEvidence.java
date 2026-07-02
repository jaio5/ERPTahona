package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "verifactu_evidence")
public class VerifactuEvidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 128)
    @Column(name = "cert_fingerprint", length = 128)
    private String certFingerprint;

    @Column(name = "created_at")
    private Instant createdAt;

    @Size(max = 100)
    @NotNull
    @Column(name = "factura_id", nullable = false, length = 100)
    private String facturaId;

    @Column(name = "fecha_emision")
    private Instant fechaEmision;

    @Size(max = 128)
    @NotNull
    @Column(name = "hash", nullable = false, length = 128)
    private String hash;

    @Size(max = 128)
    @Column(name = "hash_anterior", length = 128)
    private String hashAnterior;

    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @Size(max = 255)
    @Column(name = "numero")
    private String numero;

    @Size(max = 255)
    @Column(name = "serie")
    private String serie;

    @Column(name = "signature")
    private byte[] signature;

    @Size(max = 50)
    @Column(name = "estado", length = 50)
    private String estado; // PENDIENTE, ENVIADO, ERROR, VERIFICADO

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "fecha_envio")
    private Instant fechaEnvio;

    @Size(max = 100)
    @Column(name = "codigo_respuesta_aeat", length = 100)
    private String codigoRespuestaAEAT;

    @Size(max = 20)
    @Column(name = "tipo_registro", length = 20)
    private String tipoRegistro;

    @Lob
    @Column(name = "xml_generado", columnDefinition = "LONGTEXT")
    private String xmlGenerado;

    @Column(name = "fecha_generacion_registro")
    private Instant fechaGeneracionRegistro;

    @Size(max = 128)
    @Column(name = "huella_registro", length = 128)
    private String huellaRegistro;

    @Size(max = 20)
    @Column(name = "nif_emisor", length = 20)
    private String nifEmisor;

    @Column(name = "fecha_expedicion_factura")
    private java.time.LocalDate fechaExpedicionFactura;

}
