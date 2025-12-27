package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * Solicitudes de ejercicio de derechos RGPD (ARCO)
 * - Acceso
 * - Rectificación
 * - Cancelación / Supresión
 * - Oposición
 * - Portabilidad
 * - Limitación del tratamiento
 */
@Getter
@Setter
@Entity
@Table(name = "rgpd_solicitudes")
public class RgpdSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Cliente relacionado
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * Email del solicitante
     */
    @NotNull
    @Email
    @Size(max = 100)
    @Column(name = "email_solicitante", nullable = false, length = 100)
    private String emailSolicitante;

    /**
     * Nombre del solicitante
     */
    @NotNull
    @Size(max = 200)
    @Column(name = "nombre_solicitante", nullable = false, length = 200)
    private String nombreSolicitante;

    /**
     * Tipo de derecho ejercido
     */
    @NotNull
    @Size(max = 50)
    @Column(name = "tipo_derecho", nullable = false, length = 50)
    private String tipoDerecho; // ACCESO, RECTIFICACION, SUPRESION, OPOSICION, PORTABILIDAD, LIMITACION

    /**
     * Estado de la solicitud
     */
    @NotNull
    @Size(max = 30)
    @ColumnDefault("'PENDIENTE'")
    @Column(name = "estado", nullable = false, length = 30)
    private String estado; // PENDIENTE, EN_PROCESO, COMPLETADA, RECHAZADA

    /**
     * Fecha de la solicitud
     */
    @NotNull
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    /**
     * Fecha límite de respuesta (máximo 1 mes según RGPD)
     */
    @NotNull
    @Column(name = "fecha_limite_respuesta", nullable = false)
    private LocalDateTime fechaLimiteRespuesta;

    /**
     * Fecha de respuesta real
     */
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    /**
     * Descripción de la solicitud
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Respuesta dada al solicitante
     */
    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;

    /**
     * Usuario responsable de tramitar la solicitud
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id")
    private Usuario usuarioResponsable;

    /**
     * IP desde la que se realizó la solicitud
     */
    @Size(max = 45)
    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    /**
     * Canal por el que se recibió la solicitud
     */
    @Size(max = 50)
    @Column(name = "canal", length = 50)
    private String canal; // WEB, EMAIL, PRESENCIAL, TELEFONO, CORREO_POSTAL

    /**
     * Documento de identificación verificado
     */
    @ColumnDefault("false")
    @Column(name = "identidad_verificada")
    private Boolean identidadVerificada;

    /**
     * Ruta al archivo de respuesta (PDF, ZIP con datos, etc.)
     */
    @Size(max = 500)
    @Column(name = "ruta_archivo_respuesta", length = 500)
    private String rutaArchivoRespuesta;

    /**
     * Notas internas
     */
    @Column(name = "notas_internas", columnDefinition = "TEXT")
    private String notasInternas;

    @PrePersist
    protected void onCreate() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
        if (fechaLimiteRespuesta == null) {
            // RGPD establece máximo 1 mes (30 días)
            fechaLimiteRespuesta = fechaSolicitud.plusDays(30);
        }
        if (estado == null || estado.isEmpty()) {
            estado = "PENDIENTE";
        }
        if (identidadVerificada == null) {
            identidadVerificada = false;
        }
    }
}


