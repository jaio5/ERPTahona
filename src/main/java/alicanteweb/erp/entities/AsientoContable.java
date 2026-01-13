package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidad que representa un asiento contable
 */
@Getter
@Setter
@Entity
@Table(name = "asientos_contables")
public class AsientoContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", length = 50, unique = true, nullable = false)
    private String numero;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "concepto", nullable = false, length = 255)
    private String concepto;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo; // APERTURA, CIERRE, TRASPASO, REGULARIZACION, OPERACION

    @Column(name = "asiento_apertura")
    private Boolean asientoApertura = false;

    @Column(name = "asiento_cierre")
    private Boolean asientoCierre = false;

    @Column(name = "debe", precision = 12, scale = 2, nullable = false)
    private BigDecimal debe = BigDecimal.ZERO;

    @Column(name = "haber", precision = 12, scale = 2, nullable = false)
    private BigDecimal haber = BigDecimal.ZERO;

    @Column(name = "descuadre", precision = 12, scale = 2)
    private BigDecimal descuadre = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @Column(name = "factura_compra_id")
    private Long facturaCompraId;

    @OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LineaAsiento> lineas = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (debe == null) debe = BigDecimal.ZERO;
        if (haber == null) haber = BigDecimal.ZERO;
        if (asientoApertura == null) asientoApertura = false;
        if (asientoCierre == null) asientoCierre = false;
    }

    /**
     * Calcular totales del asiento
     */
    public void calcularTotales() {
        debe = BigDecimal.ZERO;
        haber = BigDecimal.ZERO;

        for (LineaAsiento linea : lineas) {
            if (linea.getDebe() != null) {
                debe = debe.add(linea.getDebe());
            }
            if (linea.getHaber() != null) {
                haber = haber.add(linea.getHaber());
            }
        }
    }

    /**
     * Verificar si el asiento está cuadrado
     */
    public boolean estaCuadrado() {
        calcularTotales();
        return debe.compareTo(haber) == 0;
    }

    /**
     * Calcular el descuadre del asiento
     */
    public void calcularDescuadre() {
        calcularTotales();
        descuadre = debe.subtract(haber).abs();
    }

    /**
     * Obtener el descuadre actual
     */
    public BigDecimal getDescuadre() {
        if (descuadre == null) {
            calcularDescuadre();
        }
        return descuadre;
    }
}

