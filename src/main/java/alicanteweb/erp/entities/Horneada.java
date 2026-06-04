package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "horneadas")
public class Horneada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_produccion_id")
    private OrdenProduccion ordenProduccion;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "temperatura_inicial")
    private Integer temperaturaInicial;

    @Column(name = "temperatura_final")
    private Integer temperaturaFinal;

    @Size(max = 30)
    @Column(name = "tipo_horneada", length = 30)
    private String tipoHorneada;

    @Column(name = "cantidad_producida", precision = 10, scale = 2)
    private BigDecimal cantidadProducida;

    @Column(name = "merma", precision = 10, scale = 2)
    private BigDecimal merma;

    @Size(max = 30)
    @Column(name = "resultado", length = 30)
    private String resultado;

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    @Column(name = "humedad_inicial")
    private Integer humedadInicial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Horneada)) return false;
        return Objects.equals(id, ((Horneada) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
