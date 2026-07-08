package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Tipo de IVA configurable (catálogo de Ajustes). Alimenta el desplegable de IVA al crear
 * facturas/albaranes; las líneas guardan el valor aplicado, no una referencia a esta fila,
 * por lo que editar el catálogo no altera documentos ya emitidos.
 */
@Getter
@Setter
@Entity
@Table(name = "tipos_impositivos")
public class TipoImpositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotNull
    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    /** Recargo de equivalencia asociado (opcional). */
    @Column(name = "recargo_equivalencia", precision = 5, scale = 2)
    private BigDecimal recargoEquivalencia;

    @ColumnDefault("TRUE")
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ColumnDefault("0")
    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    /** Tipo propuesto por defecto en las líneas nuevas. Solo uno debe estar marcado. */
    @ColumnDefault("FALSE")
    @Column(name = "es_defecto", nullable = false)
    private Boolean esDefecto = false;

    @PrePersist
    void onCreate() {
        if (activo == null) activo = true;
        if (orden == null) orden = 0;
        if (esDefecto == null) esDefecto = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoImpositivo that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
