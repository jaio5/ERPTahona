package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "rutas_reparto")
public class RutaReparto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Size(max = 255)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @Size(max = 100)
    @Column(name = "conductor", length = 100)
    private String conductor;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "distancia_total_km", precision = 10, scale = 2)
    private java.math.BigDecimal distanciaTotalKm;

    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private Set<RutaParada> paradas = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RutaReparto)) return false;
        return Objects.equals(id, ((RutaReparto) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "RutaReparto{id=" + id + ", nombre='" + nombre + "'}"; }
}
