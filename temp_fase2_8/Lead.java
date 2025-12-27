package alicanteweb.erp.entities;
}
    }
        if (estado == null) estado = "NUEVO";
        if (activo == null) activo = true;
        if (fechaCreacion == null) fechaCreacion = LocalDate.now();
    protected void onCreate() {
    @PrePersist

    private Boolean activo;
    @Column(name = "activo")

    private String notas;
    @Column(name = "notas", columnDefinition = "TEXT")

    private alicanteweb.erp.entities.Usuario usuarioAsignado;
    @JoinColumn(name = "usuario_asignado_id")
    @ManyToOne(fetch = FetchType.LAZY)

    private LocalDate fechaUltimaInteraccion;
    @Column(name = "fecha_ultima_interaccion")

    private LocalDate fechaCreacion;
    @Column(name = "fecha_creacion")

    private Integer probabilidadCierre; // 0-100%
    @Column(name = "probabilidad_cierre")

    private BigDecimal presupuestoEstimado;
    @Column(name = "presupuesto_estimado", precision = 12, scale = 2)

    private String interes;
    @Column(name = "interes", length = 200)

    private String origen; // WEB, TELEFONO, EMAIL, REFERIDO, REDES_SOCIALES
    @Column(name = "origen", length = 50)

    private String estado; // NUEVO, CONTACTADO, CALIFICADO, OPORTUNIDAD, PERDIDO, CLIENTE
    @Column(name = "estado", nullable = false, length = 30)
    @NotNull

    private String telefono;
    @Column(name = "telefono", length = 20)

    private String email;
    @Column(name = "email", length = 100)

    private String empresa;
    @Column(name = "empresa", length = 200)

    private String nombre;
    @Column(name = "nombre", nullable = false, length = 200)
    @NotNull

    private Long id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

public class Lead {
@Table(name = "leads")
@Entity
@Setter
@Getter
 */
 * FASE 5: CRM
 * Lead - Cliente potencial
/**

import java.time.LocalDate;
import java.math.BigDecimal;

import lombok.Setter;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;


