package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Movimientos de stock (entradas, salidas, traspasos, ajustes)
 */
@Getter
@Setter
@Entity
@Table(name = "movimientos_stock")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // ENTRADA, SALIDA, TRASPASO, AJUSTE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_origen_id")
    private Almacen almacenOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_destino_id")
    private Almacen almacenDestino;

    @NotNull
    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "importe", precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "concepto", length = 500)
    private String concepto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_compra_id")
    private FacturaCompra facturaCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "albaran_id")
    private AlbaranVenta albaran;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    public void calcularImporte() {
        if (cantidad != null && precioUnitario != null) {
            importe = cantidad.multiply(precioUnitario);
        }
    }
}

