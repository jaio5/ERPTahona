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

    // ========== ESTADOS DE REMISIÓN ==========
    // Solo ACEPTADO y ACEPTADO_CON_ERRORES reflejan una respuesta real de la AEAT que
    // registra el registro de facturación. El resto son estados honestos de "no remitido".
    // ENVIADO y VERIFICADO son históricos: los ponían el modo local y las acciones
    // "Reenviar"/"Verificar" sin llamar a la AEAT, así que NO prueban una remisión.

    /** Registro generado, aún sin resultado de remisión. */
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    /** Registro generado en local con la remisión a la AEAT desactivada: no se ha enviado. */
    public static final String ESTADO_NO_REMITIDO = "NO_REMITIDO";
    /** Se llamó a la AEAT pero la respuesta no confirma el registro (no se pudo interpretar). */
    public static final String ESTADO_RESPUESTA_NO_CONFIRMADA = "RESPUESTA_NO_CONFIRMADA";
    /** La AEAT respondió Correcto. */
    public static final String ESTADO_ACEPTADO = "ACEPTADO";
    /** La AEAT respondió AceptadoConErrores: el registro consta en la AEAT, con avisos. */
    public static final String ESTADO_ACEPTADO_CON_ERRORES = "ACEPTADO_CON_ERRORES";
    /** Error de comunicación o rechazo. */
    public static final String ESTADO_ERROR = "ERROR";

    /** Indica si el estado prueba que la AEAT registró de verdad el registro de facturación. */
    public static boolean esRemisionConfirmada(String estado) {
        return ESTADO_ACEPTADO.equals(estado) || ESTADO_ACEPTADO_CON_ERRORES.equals(estado);
    }

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

    // BLOB: una firma RSA-2048 ocupa 256 bytes y no cabe en el varbinary(255) por defecto
    @Column(name = "signature", columnDefinition = "BLOB")
    private byte[] signature;

    @Size(max = 50)
    @Column(name = "estado", length = 50)
    private String estado; // ver constantes ESTADO_* (ENVIADO/VERIFICADO: solo datos históricos)

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
