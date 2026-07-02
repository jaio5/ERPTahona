package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "hojas_ruta")
public class HojaRuta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private RutaReparto ruta;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Size(max = 100)
    @Column(name = "conductor", length = 100)
    private String conductor;

    @Size(max = 30)
    @Column(name = "estado", length = 30)
    private String estado = "PLANIFICADA";

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Column(name = "hora_llegada")
    private LocalDateTime horaLlegada;

    @Column(name = "km_inicio")
    private java.math.BigDecimal kmInicio;

    @Column(name = "km_fin")
    private java.math.BigDecimal kmFin;

    @Column(name = "incidencias", length = 2000)
    private String incidencias;

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "hojaRuta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private Set<HojaRutaEntrega> entregas = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = "PLANIFICADA";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HojaRuta)) return false;
        return Objects.equals(id, ((HojaRuta) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "HojaRuta{id=" + id + ", fecha=" + fecha + "}"; }

    public alicanteweb.erp.entities.enums.EstadoHojaRutaEnum getEstadoEnum() {
        if (estado == null) return null;
        try {
            return alicanteweb.erp.entities.enums.EstadoHojaRutaEnum.valueOf(estado);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
