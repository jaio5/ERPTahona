package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Entidad para auditoría de todas las acciones en el sistema.
 * Cumplimiento RGPD y trazabilidad completa.
 * Nota: se usa @Getter/@Setter en lugar de @Data para evitar equals/hashCode
 * inadecuado en entidades JPA.
 */
@Getter
@Setter
@Entity
@Table(name = "auditoria_acciones", indexes = {
    @Index(name = "idx_auditoria_fecha", columnList = "fecha"),
    @Index(name = "idx_auditoria_usuario", columnList = "usuario_id"),
    @Index(name = "idx_auditoria_entidad", columnList = "entidad_tipo, entidad_id")
})
public class AuditoriaAccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Usuario que realizó la acción
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Nombre de usuario (guardado por si el usuario se elimina)
     */
    @Size(max = 50)
    @Column(name = "usuario_nombre", length = 50)
    private String usuarioNombre;

    /**
     * Tipo de acción
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "tipo_accion", nullable = false, length = 50)
    private String tipoAccion; // CREAR, LEER, ACTUALIZAR, ELIMINAR, LOGIN, LOGOUT, EXPORTAR, IMPRIMIR

    /**
     * Fecha y hora de la acción
     */
    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /**
     * Tipo de entidad afectada
     */
    @Size(max = 100)
    @Column(name = "entidad_tipo", length = 100)
    private String entidadTipo; // Cliente, Factura, Articulo, Usuario, etc.

    /**
     * ID de la entidad afectada
     */
    @Size(max = 50)
    @Column(name = "entidad_id", length = 50)
    private String entidadId;

    /**
     * Descripción de la acción
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Módulo de la aplicación
     */
    @Size(max = 100)
    @Column(name = "modulo", length = 100)
    private String modulo; // CLIENTES, FACTURAS, ARTICULOS, CONFIGURACION, etc.

    /**
     * IP desde la que se realizó la acción
     */
    @Size(max = 45)
    @Column(name = "ip", length = 45)
    private String ip;

    /**
     * User Agent (navegador, etc.)
     */
    @Size(max = 255)
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Valores anteriores (JSON) - para operaciones UPDATE
     */
    @Column(name = "valores_anteriores")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> valoresAnteriores;

    /**
     * Valores nuevos (JSON) - para operaciones UPDATE y CREATE
     */
    @Column(name = "valores_nuevos")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> valoresNuevos;

    /**
     * Resultado de la acción
     */
    @Size(max = 20)
    @Column(name = "resultado", length = 20)
    private String resultado; // EXITO, ERROR, DENEGADO

    /**
     * Mensaje de error (si aplica)
     */
    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    /**
     * Metadata adicional
     */
    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditoriaAccion)) return false;
        AuditoriaAccion that = (AuditoriaAccion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AuditoriaAccion{id=" + id + ", tipoAccion='" + tipoAccion + "', fecha=" + fecha + "}";
    }
}


