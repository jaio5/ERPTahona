package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Oportunidad de venta
 * FASE 5: CRM
 */
@Getter
@Setter
@Entity
@Table(name = "oportunidades")
public class Oportunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @NotNull
    @Column(name = "etapa", nullable = false, length = 30)
    private String etapa; // PROSPECTO, CALIFICACION, PROPUESTA, NEGOCIACION, CERRADA_GANADA, CERRADA_PERDIDA

    @NotNull
    @Column(name = "valor_estimado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "probabilidad")
    private Integer probabilidad; // 0-100%

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "fecha_cierre_estimada")
    private LocalDate fechaCierreEstimada;

    @Column(name = "fecha_cierre_real")
    private LocalDate fechaCierreReal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id")
    private alicanteweb.erp.entities.Usuario usuarioResponsable;

    @Column(name = "motivo_perdida", length = 200)
    private String motivoPerdida;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDate.now();
        if (etapa == null) etapa = "PROSPECTO";
        if (probabilidad == null) probabilidad = 10;
    }
}

