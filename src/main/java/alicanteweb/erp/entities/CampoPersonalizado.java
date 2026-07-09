package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Campo del formato de impresión que aparece en una zona de la factura o el albarán.
 *
 * <p>Puede tener dos orígenes:
 * <ul>
 *   <li>{@link #ORIGEN_SISTEMA}: el valor lo aporta automáticamente el ERP (empresa o cliente)
 *       a partir de una {@link #claveSistema} del catálogo {@code CampoSistema}.</li>
 *   <li>{@link #ORIGEN_PROPIO}: par etiqueta/valor de texto libre escrito por el usuario.</li>
 * </ul>
 *
 * <p>La visibilidad decide a qué clientes se imprime: a todos ({@link #VIS_TODOS}),
 * a todos salvo un conjunto ({@link #VIS_EXCEPTO}) o solo a un conjunto ({@link #VIS_SOLO}).
 * El conjunto de clientes se guarda en {@link #clientes}.
 */
@Getter
@Setter
@Entity
@Table(name = "campos_personalizados")
public class CampoPersonalizado {

    public static final String ORIGEN_SISTEMA = "SISTEMA";
    public static final String ORIGEN_PROPIO = "PROPIO";

    public static final String DOC_FACTURA = "FACTURA";
    public static final String DOC_ALBARAN = "ALBARAN";
    public static final String DOC_AMBOS = "AMBOS";

    // Zonas del documento donde puede pintarse el campo.
    public static final String UB_CABECERA = "CABECERA";
    public static final String UB_CLIENTE = "CLIENTE";
    public static final String UB_OBSERVACIONES = "OBSERVACIONES";
    public static final String UB_PIE = "PIE";

    // Visibilidad respecto a los clientes.
    public static final String VIS_TODOS = "TODOS";
    public static final String VIS_EXCEPTO = "EXCEPTO";
    public static final String VIS_SOLO = "SOLO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 10)
    @ColumnDefault("'PROPIO'")
    @Column(name = "origen", nullable = false, length = 10)
    private String origen = ORIGEN_PROPIO;

    /** Clave del catálogo de datos del sistema (obligatoria si origen = SISTEMA). */
    @Size(max = 60)
    @Column(name = "clave_sistema", length = 60)
    private String claveSistema;

    /**
     * Etiqueta que se imprime. Obligatoria para campos propios; opcional para los de sistema
     * (si se deja vacía, se usa la etiqueta del catálogo).
     */
    @Size(max = 100)
    @Column(name = "etiqueta", length = 100)
    private String etiqueta;

    /** Valor de texto libre (solo para campos propios; los de sistema lo resuelven al imprimir). */
    @Size(max = 500)
    @Column(name = "valor", length = 500)
    private String valor;

    @NotNull
    @Size(max = 20)
    @ColumnDefault("'PIE'")
    @Column(name = "ubicacion", nullable = false, length = 20)
    private String ubicacion = UB_PIE;

    @NotNull
    @Size(max = 10)
    @ColumnDefault("'AMBOS'")
    @Column(name = "documento", nullable = false, length = 10)
    private String documento = DOC_AMBOS;

    @NotNull
    @Size(max = 10)
    @ColumnDefault("'TODOS'")
    @Column(name = "visibilidad", nullable = false, length = 10)
    private String visibilidad = VIS_TODOS;

    /** Conjunto de clientes al que aplica la visibilidad EXCEPTO/SOLO. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "campo_impresion_clientes",
            joinColumns = @JoinColumn(name = "campo_id"))
    @Column(name = "cliente_id")
    private Set<Long> clientes = new HashSet<>();

    @ColumnDefault("0")
    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @ColumnDefault("TRUE")
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public boolean esSistema() {
        return ORIGEN_SISTEMA.equals(origen);
    }

    @PrePersist
    void onCreate() {
        if (origen == null) origen = ORIGEN_PROPIO;
        if (ubicacion == null) ubicacion = UB_PIE;
        if (documento == null) documento = DOC_AMBOS;
        if (visibilidad == null) visibilidad = VIS_TODOS;
        if (orden == null) orden = 0;
        if (activo == null) activo = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampoPersonalizado that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
