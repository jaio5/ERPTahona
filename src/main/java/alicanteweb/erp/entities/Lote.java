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
@Table(name = "lotes")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @NotNull
    @Column(name = "fecha_produccion", nullable = false)
    private LocalDate fechaProduccion;

    @NotNull
    @Column(name = "fecha_caducidad", nullable = false)
    private LocalDate fechaCaducidad;

    @Column(name = "cantidad_inicial", precision = 10, scale = 2)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", precision = 10, scale = 2)
    private BigDecimal cantidadActual;

    @Size(max = 30)
    @Column(name = "estado", length = 30)
    private String estado = "ACTIVO";

    @Size(max = 100)
    @Column(name = "origen", length = 100)
    private String origen;

    @Size(max = 50)
    @Column(name = "numero_registro_sanitario", length = 50)
    private String numeroRegistroSanitario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_produccion_id")
    private OrdenProduccion ordenProduccion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = "ACTIVO";
        if (cantidadActual == null) cantidadActual = cantidadInicial;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lote)) return false;
        return Objects.equals(id, ((Lote) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "Lote{id=" + id + ", codigo='" + codigo + "'}"; }

    public alicanteweb.erp.entities.enums.EstadoLoteEnum getEstadoEnum() {
        if (estado == null) return null;
        try {
            return alicanteweb.erp.entities.enums.EstadoLoteEnum.valueOf(estado);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
