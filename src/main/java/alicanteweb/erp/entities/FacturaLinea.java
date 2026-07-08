package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import alicanteweb.erp.util.DesgloseFiscal;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "factura_lineas")
public class FacturaLinea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio;

    // DECIMAL(10,2): el descuento puede ser % (0-100) o un importe fijo en euros (descuento_tipo).
    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento;

    /** Interpreta {@link #descuento}: PORCENTAJE (por defecto) o IMPORTE (€ fijo por línea). */
    @Column(name = "descuento_tipo", length = 10)
    private String descuentoTipo;

    @Column(name = "iva", precision = 5, scale = 2)
    private BigDecimal iva;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Obtener precio unitario (alias para precio si no existe precioUnitario)
     */
    public BigDecimal getPrecioUnitario() {
        return precioUnitario != null ? precioUnitario : precio;
    }

    /**
     * Total (base) de la línea: cantidad·precio menos su descuento (% o importe fijo).
     * Si hay un total persistido lo respeta; si no, lo calcula con la fuente única
     * {@link DesgloseFiscal#baseLinea}.
     */
    public BigDecimal getTotal() {
        if (total != null) return total;
        if (cantidad == null || getPrecioUnitario() == null) {
            return BigDecimal.ZERO;
        }
        return DesgloseFiscal.baseLinea(
                new DesgloseFiscal.Linea(cantidad, getPrecioUnitario(), iva, descuentoTipo, descuento));
    }

    /**
     * Inalterabilidad RRSIF: las líneas de una factura ya emitida no se modifican,
     * añaden ni borran (las correcciones van por factura rectificativa). Se compara
     * contra el estado persistido de la factura para no interferir con la propia
     * transacción de emisión.
     */
    @PreUpdate
    @PreRemove
    @PrePersist
    private void protegerLineaDeFacturaEmitida() {
        if (factura == null) {
            return;
        }
        String estado = factura.getEstadoPersistido();
        if (estado != null && java.util.Set.of("EMITIDA", "PAGADA", "VENCIDA", "ANULADA", "RECTIFICADA").contains(estado)) {
            throw new IllegalStateException("La factura " + factura.getNumero()
                    + " está " + estado + ": sus líneas son inalterables (RRSIF). Emita una rectificativa.");
        }
    }
}