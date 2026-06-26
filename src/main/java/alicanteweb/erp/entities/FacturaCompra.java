package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad FacturaCompra - Facturas recibidas de proveedores
 * OBLIGATORIO según Art. 164 Ley del IVA
 */
@Getter
@Setter
@Entity
@Table(name = "facturas_compra")
public class FacturaCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "numero", nullable = false, unique = true, length = 100)
    private String numero; // Número de la factura del proveedor

    @Size(max = 100)
    @Column(name = "numero_serie", length = 100)
    private String numeroSerie; // Serie de la factura

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    // Importes
    @NotNull
    @Column(name = "base_imponible", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseImponible;

    @Column(name = "tipo_iva", precision = 5, scale = 2)
    private BigDecimal tipoIva;

    @NotNull
    @Column(name = "importe_iva", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeIva;

    @Column(name = "tipo_recargo", precision = 5, scale = 2)
    private BigDecimal tipoRecargo;

    @Column(name = "importe_recargo", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal importeRecargo;

    @Column(name = "tipo_retencion", precision = 5, scale = 2)
    private BigDecimal tipoRetencion;

    @Column(name = "importe_retencion", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal importeRetencion;

    @NotNull
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    // Estado
    @Size(max = 20)
    @ColumnDefault("'PENDIENTE'")
    @Column(name = "estado", length = 20)
    private String estado; // PENDIENTE, CONTABILIZADA, PAGADA

    // Control de pago
    @Column(name = "pagada")
    @ColumnDefault("FALSE")
    private Boolean pagada;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Size(max = 50)
    @Column(name = "forma_pago", length = 50)
    private String formaPago;

    // Relación con pedido de compra (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_compra_id")
    private PedidoCompra pedidoCompra;

    // Líneas de la factura
    @OneToMany(mappedBy = "facturaCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacturaCompraLinea> lineas = new ArrayList<>();

    // Observaciones
    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @Lob
    @Column(name = "notas_internas")
    private String notasInternas;

    // Control
    @Column(name = "contabilizada")
    @ColumnDefault("FALSE")
    private Boolean contabilizada;

    @Column(name = "fecha_contabilizacion")
    private LocalDateTime fechaContabilizacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (estado == null) {
            estado = "PENDIENTE";
        }
        if (pagada == null) {
            pagada = false;
        }
        if (contabilizada == null) {
            contabilizada = false;
        }
        if (importeRecargo == null) {
            importeRecargo = BigDecimal.ZERO;
        }
        if (importeRetencion == null) {
            importeRetencion = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    /**
     * Calcula el total de la factura
     */
    public void calcularTotal() {
        if (baseImponible == null) {
            baseImponible = BigDecimal.ZERO;
        }
        if (importeIva == null) {
            importeIva = BigDecimal.ZERO;
        }
        if (importeRecargo == null) {
            importeRecargo = BigDecimal.ZERO;
        }
        if (importeRetencion == null) {
            importeRetencion = BigDecimal.ZERO;
        }

        total = baseImponible
                .add(importeIva)
                .add(importeRecargo)
                .subtract(importeRetencion);
    }

    /**
     * Marca la factura como pagada
     */
    public void marcarComoPagada(LocalDate fechaPago) {
        this.pagada = true;
        this.fechaPago = fechaPago;
        this.estado = "PAGADA";
    }

    /**
     * Marca la factura como contabilizada
     */
    public void marcarComoContabilizada() {
        this.contabilizada = true;
        this.fechaContabilizacion = LocalDateTime.now();
        if ("PENDIENTE".equals(this.estado)) {
            this.estado = "CONTABILIZADA";
        }
    }

    @Override
    public String toString() {
        return numero + " - " + (proveedor != null ? proveedor.getNombre() : "") + " - " + total + "€";
    }
}

