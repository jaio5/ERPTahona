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
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "facturas")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ColumnDefault("0.00")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @ColumnDefault("0.00")
    @Column(name = "pagado", precision = 10, scale = 2)
    private BigDecimal pagado;

    @Column(name = "pagada", nullable = false)
    private boolean pagada;

    // Nuevos campos para gestión de estado y Verifactu
    @Size(max = 20)
    @ColumnDefault("'BORRADOR'")
    @Column(name = "estado", length = 20)
    private String estado; // BORRADOR, REVISION, EMITIDA, ANULADA

    @ColumnDefault("FALSE")
    @Column(name = "verifactu_enviada")
    private Boolean verifactuEnviada;

    @Column(name = "fecha_emision_verifactu")
    private LocalDateTime fechaEmisionVerifactu;

    @Lob
    @Column(name = "observaciones_revision")
    private String observacionesRevision;

    // ========== CAMPOS OBLIGATORIOS RD 1619/2012 ==========

    /**
     * Serie de la factura (obligatorio RD 1619/2012)
     */
    @Size(max = 20)
    @Column(name = "serie", length = 20)
    private String serie;

    /**
     * Fecha de operación (si difiere de fecha de expedición)
     * Art. 6.1.d RD 1619/2012
     */
    @Column(name = "fecha_operacion")
    private LocalDate fechaOperacion;

    /**
     * Tipo de factura según RD 1619/2012
     */
    @Size(max = 30)
    @ColumnDefault("'ORDINARIA'")
    @Column(name = "tipo_factura", length = 30)
    private String tipoFactura; // ORDINARIA, SIMPLIFICADA, RECTIFICATIVA

    /**
     * Medio de cobro (si es distinto al habitual)
     * Art. 6.1.l RD 1619/2012
     */
    @Size(max = 50)
    @Column(name = "medio_cobro", length = 50)
    private String medioCobro; // EFECTIVO, TARJETA, TRANSFERENCIA, BIZUM, CHEQUE, PAGARE, etc.

    /**
     * Retención IRPF (si aplica)
     */
    @ColumnDefault("0.00")
    @Column(name = "retencion_irpf", precision = 10, scale = 2)
    private BigDecimal retencionIrpf;

    /**
     * Porcentaje de retención IRPF
     */
    @ColumnDefault("0.00")
    @Column(name = "porcentaje_retencion", precision = 5, scale = 2)
    private BigDecimal porcentajeRetencion;

    /**
     * Fecha de vencimiento (si hay aplazamiento)
     */
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    /**
     * Referencia del pedido (si existe)
     */
    @Size(max = 50)
    @Column(name = "referencia_pedido", length = 50)
    private String referenciaPedido;

    /**
     * Número de albarán (si existe)
     */
    @Size(max = 50)
    @Column(name = "numero_albaran", length = 50)
    private String numeroAlbaran;

    /**
     * Inversión del sujeto pasivo (Art. 84 Ley IVA)
     */
    @ColumnDefault("false")
    @Column(name = "inversion_sujeto_pasivo")
    private Boolean inversionSujetoPasivo;

    /**
     * Régimen especial de criterio de caja (Art. 163 undecies Ley IVA)
     */
    @ColumnDefault("false")
    @Column(name = "criterio_caja")
    private Boolean criterioCaja;

    /**
     * Operación triangular
     */
    @ColumnDefault("false")
    @Column(name = "operacion_triangular")
    private Boolean operacionTriangular;

    /**
     * Factura rectificativa - Número de factura original
     */
    @Size(max = 50)
    @Column(name = "factura_rectificada_numero", length = 50)
    private String facturaRectificadaNumero;

    /**
     * Factura rectificativa - Fecha de factura original
     */
    @Column(name = "factura_rectificada_fecha")
    private LocalDate facturaRectificadaFecha;

    /**
     * Factura rectificativa - Motivo de rectificación
     */
    @Size(max = 500)
    @Column(name = "motivo_rectificacion", length = 500)
    private String motivoRectificacion;

    /**
     * Factura rectificativa - Tipo de rectificación
     */
    @Size(max = 20)
    @Column(name = "tipo_rectificacion", length = 20)
    private String tipoRectificacion; // SUSTITUCION, DIFERENCIAS

    /**
     * Base imponible
     */
    @ColumnDefault("0.00")
    @Column(name = "base_imponible", precision = 10, scale = 2)
    private BigDecimal baseImponible;

    /**
     * Total IVA
     */
    @ColumnDefault("0.00")
    @Column(name = "total_iva", precision = 10, scale = 2)
    private BigDecimal totalIva;

    /**
     * Total Recargo de Equivalencia
     */
    @ColumnDefault("0.00")
    @Column(name = "total_recargo", precision = 10, scale = 2)
    private BigDecimal totalRecargo;

    /**
     * Observaciones generales de la factura
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * QR Code de VeriFactu (Base64)
     */
    @Column(name = "verifactu_qr", columnDefinition = "TEXT")
    private String verifactuQr;

    /**
     * Hash SHA-256 de VeriFactu
     */
    @Size(max = 128)
    @Column(name = "verifactu_hash", length = 128)
    private String verifactuHash;

    /**
     * Hash anterior de la cadena VeriFactu
     */
    @Size(max = 128)
    @Column(name = "verifactu_hash_anterior", length = 128)
    private String verifactuHashAnterior;

    @OneToMany(mappedBy = "facturas")
    private Set<AlbaranVentaFactura> albaranesVentaFacturas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "factura")
    private Set<FacturaLinea> facturaLineas = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (estado == null || estado.isEmpty()) {
            estado = "BORRADOR";
        }
        if (verifactuEnviada == null) {
            verifactuEnviada = false;
        }
        if (tipoFactura == null || tipoFactura.isEmpty()) {
            tipoFactura = "ORDINARIA";
        }
        if (inversionSujetoPasivo == null) {
            inversionSujetoPasivo = false;
        }
        if (criterioCaja == null) {
            criterioCaja = false;
        }
        if (operacionTriangular == null) {
            operacionTriangular = false;
        }
        if (baseImponible == null) {
            baseImponible = BigDecimal.ZERO;
        }
        if (totalIva == null) {
            totalIva = BigDecimal.ZERO;
        }
        if (totalRecargo == null) {
            totalRecargo = BigDecimal.ZERO;
        }
        if (retencionIrpf == null) {
            retencionIrpf = BigDecimal.ZERO;
        }
        if (porcentajeRetencion == null) {
            porcentajeRetencion = BigDecimal.ZERO;
        }
    }
}
