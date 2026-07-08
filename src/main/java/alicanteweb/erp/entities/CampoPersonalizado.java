package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;

/**
 * Campo personalizado del formato de impresión: un par etiqueta/valor que aparece en una zona
 * de la factura o el albarán. Puede ser global (ámbito EMPRESA) o exclusivo de un cliente
 * (ámbito CLIENTE). Es la alternativa segura a editar la plantilla: datos, no HTML.
 */
@Getter
@Setter
@Entity
@Table(name = "campos_personalizados")
public class CampoPersonalizado {

    public static final String AMBITO_EMPRESA = "EMPRESA";
    public static final String AMBITO_CLIENTE = "CLIENTE";

    public static final String DOC_FACTURA = "FACTURA";
    public static final String DOC_ALBARAN = "ALBARAN";
    public static final String DOC_AMBOS = "AMBOS";

    // Zonas del documento donde puede pintarse el campo.
    public static final String UB_CABECERA = "CABECERA";
    public static final String UB_CLIENTE = "CLIENTE";
    public static final String UB_OBSERVACIONES = "OBSERVACIONES";
    public static final String UB_PIE = "PIE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 10)
    @ColumnDefault("'EMPRESA'")
    @Column(name = "ambito", nullable = false, length = 10)
    private String ambito = AMBITO_EMPRESA;

    /** Cliente al que pertenece el campo (obligatorio si ámbito = CLIENTE). */
    @Column(name = "cliente_id")
    private Long clienteId;

    @NotNull
    @Size(max = 100)
    @Column(name = "etiqueta", nullable = false, length = 100)
    private String etiqueta;

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

    @ColumnDefault("0")
    @Column(name = "orden", nullable = false)
    private Integer orden = 0;

    @ColumnDefault("TRUE")
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    void onCreate() {
        if (ambito == null) ambito = AMBITO_EMPRESA;
        if (ubicacion == null) ubicacion = UB_PIE;
        if (documento == null) documento = DOC_AMBOS;
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
