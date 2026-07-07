package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "facturacion_eventos", indexes = {
    @Index(name = "idx_fact_eventos_fecha", columnList = "fecha"),
    @Index(name = "idx_fact_eventos_ambito", columnList = "ambito"),
    @Index(name = "idx_fact_eventos_tipo", columnList = "tipo_evento")
})
public class FacturacionEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "ambito", nullable = false, length = 50)
    private String ambito;

    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "hash_anterior", length = 128)
    private String hashAnterior;

    @Column(name = "hash_actual", nullable = false, length = 128)
    private String hashActual;

    @Column(name = "payload_hash", length = 128)
    private String payloadHash;

    @Column(name = "version_normativa", length = 50)
    private String versionNormativa;

    @Column(name = "modalidad_sif", length = 20)
    private String modalidadSif;

    @Column(name = "origen_sistema", length = 100)
    private String origenSistema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
