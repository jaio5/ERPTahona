package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Asiento contable
 */
@Getter
@Setter
@Entity
@Table(name = "asientos_contables")
public class AsientoContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "numero", nullable = false)
    private Integer numero;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "concepto", length = 500)
    private String concepto;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "tipo", length = 20)
    private String tipo; // APERTURA, OPERACION, AJUSTE, REGULARIZACION, CIERRE

    @Column(name = "asiento_apertura")
    private Boolean asientoApertura = false;

    @Column(name = "asiento_cierre")
    private Boolean asientoCierre = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_compra_id")
    private FacturaCompra facturaCompra;

    @OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsientoContableLinea> lineas = new ArrayList<>();

    @Column(name = "descuadre", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal descuadre;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (descuadre == null) descuadre = BigDecimal.ZERO;
    }

    /**
     * Calcula el descuadre del asiento (debe - haber)
     */
    public void calcularDescuadre() {
        BigDecimal totalDebe = lineas.stream()
            .map(l -> l.getDebe() != null ? l.getDebe() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHaber = lineas.stream()
            .map(l -> l.getHaber() != null ? l.getHaber() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        descuadre = totalDebe.subtract(totalHaber);
    }

    /**
     * Verifica si el asiento está cuadrado
     */
    public boolean estaCuadrado() {
        calcularDescuadre();
        return descuadre.compareTo(BigDecimal.ZERO) == 0;
    }
}

