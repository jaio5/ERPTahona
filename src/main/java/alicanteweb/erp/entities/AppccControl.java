package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "appcc_controles")
public class AppccControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora", nullable = false)
    private LocalDateTime hora;

    @Size(max = 50)
    @NotNull
    @Column(name = "punto_critico", nullable = false, length = 50)
    private String puntoCritico;

    @Size(max = 100)
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Column(name = "temperatura", precision = 5, scale = 1)
    private BigDecimal temperatura;

    @Size(max = 20)
    @Column(name = "unidad_temperatura", length = 20)
    private String unidadTemperatura = "°C";

    @Column(name = "limite_critico", precision = 5, scale = 1)
    private BigDecimal limiteCritico;

    @Size(max = 30)
    @Column(name = "resultado", length = 30)
    private String resultado;

    @Size(max = 500)
    @Column(name = "accion_correctiva", length = 500)
    private String accionCorrectiva;

    @Size(max = 100)
    @Column(name = "responsable", length = 100)
    private String responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (resultado == null) resultado = "CONFORME";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppccControl)) return false;
        return Objects.equals(id, ((AppccControl) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
