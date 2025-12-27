package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Registro de accesos a datos personales
 * Para cumplir con el derecho de información del RGPD
 */
@Getter
@Setter
@Entity
@Table(name = "rgpd_accesos_datos", indexes = {
    @Index(name = "idx_acceso_fecha", columnList = "fecha_acceso"),
    @Index(name = "idx_acceso_usuario", columnList = "usuario_id"),
    @Index(name = "idx_acceso_cliente", columnList = "cliente_id")
})
public class RgpdAccesoDatos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Usuario que accedió a los datos
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Cliente cuyos datos fueron accedidos
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * Tipo de acceso
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "tipo_acceso", nullable = false, length = 50)
    private String tipoAcceso; // LECTURA, MODIFICACION, EXPORTACION, BORRADO

    /**
     * Fecha y hora del acceso
     */
    @NotNull
    @Column(name = "fecha_acceso", nullable = false)
    private LocalDateTime fechaAcceso;

    /**
     * IP desde la que se accedió
     */
    @Size(max = 45)
    @Column(name = "ip", length = 45)
    private String ip;

    /**
     * Módulo o pantalla desde donde se accedió
     */
    @Size(max = 100)
    @Column(name = "modulo", length = 100)
    private String modulo;

    /**
     * Motivo del acceso (obligatorio en algunos casos)
     */
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    /**
     * Campos específicos accedidos (JSON)
     */
    @Column(name = "campos_accedidos")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> camposAccedidos;

    /**
     * Metadata adicional
     */
    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @PrePersist
    protected void onCreate() {
        if (fechaAcceso == null) {
            fechaAcceso = LocalDateTime.now();
        }
    }
}


