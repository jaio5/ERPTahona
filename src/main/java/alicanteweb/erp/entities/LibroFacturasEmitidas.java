package alicanteweb.erp.entities;
}
    }
        if (exportacion == null) exportacion = false;
        if (intracomunitaria == null) intracomunitaria = false;
        }
            periodo = (fechaExpedicion.getMonthValue() - 1) / 3 + 1;
        if (periodo == null && fechaExpedicion != null) {
        }
            ejercicio = fechaExpedicion.getYear();
        if (ejercicio == null && fechaExpedicion != null) {
    protected void onCreate() {
    @PrePersist

    private String observaciones;
    @Column(name = "observaciones", columnDefinition = "TEXT")

    private String claveOperacion; // SII: F1, F2, F3, etc.
    @Column(name = "clave_operacion", length = 2)

    private Boolean exportacion;
    @Column(name = "exportacion")

    private Boolean intracomunitaria;
    @Column(name = "intracomunitaria")

    private Integer periodo; // Trimestre (1-4)
    @Column(name = "periodo")

    private Integer ejercicio;
    @Column(name = "ejercicio")

    private Integer numeroRegistro;
    @Column(name = "numero_registro")

    private BigDecimal facturaRectificativaCuota;
    @Column(name = "factura_rectificativa_cuota", precision = 10, scale = 2)

    private BigDecimal facturaRectificativaBase;
    @Column(name = "factura_rectificativa_base", precision = 10, scale = 2)

    private String facturaRectificativaTipo; // S (SustituciÃƒÂ³n), I (Diferencias)
    @Column(name = "factura_rectificativa_tipo", length = 2)

    private String regimenEspecial; // Criterio de caja, REAGYP, etc.
    @Column(name = "regimen_especial", length = 50)

    private String tipoNoExenta; // S1 (Sin inversiÃƒÂ³n), S2 (Con inversiÃƒÂ³n)
    @Column(name = "tipo_no_exenta", length = 30)

    private BigDecimal totalFactura;
    @Column(name = "total_factura", nullable = false, precision = 10, scale = 2)
    @NotNull

    private BigDecimal cuotaRecargo;
    @Column(name = "cuota_recargo", precision = 10, scale = 2)

    private BigDecimal tipoRecargo;
    @Column(name = "tipo_recargo", precision = 5, scale = 2)

    private BigDecimal cuotaIva;
    @Column(name = "cuota_iva", nullable = false, precision = 10, scale = 2)
    @NotNull

    private BigDecimal tipoIva;
    @Column(name = "tipo_iva", nullable = false, precision = 5, scale = 2)
    @NotNull

    private BigDecimal baseImponible;
    @Column(name = "base_imponible", nullable = false, precision = 10, scale = 2)
    @NotNull

    private String nombreDestinatario;
    @Column(name = "nombre_destinatario", length = 200)

    private String cifDestinatario;
    @Column(name = "cif_destinatario", length = 20)

    private String tipoFactura; // Ordinaria, Simplificada, Rectificativa
    @Column(name = "tipo_factura", length = 30)

    private String numeroFacturaCompleto;
    @Column(name = "numero_factura_completo", length = 100)

    private String numero;
    @Column(name = "numero", nullable = false, length = 50)
    @NotNull

    private String serie;
    @Column(name = "serie", nullable = false, length = 10)
    @NotNull

    private LocalDate fechaOperacion;
    @Column(name = "fecha_operacion")

    private LocalDate fechaExpedicion;
    @Column(name = "fecha_expedicion", nullable = false)
    @NotNull

    private Factura factura;
    @JoinColumn(name = "factura_id")
    @ManyToOne(fetch = FetchType.LAZY)

    private Long id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

public class LibroFacturasEmitidas {
@Table(name = "libro_facturas_emitidas")
@Entity
@Setter
@Getter
 */
 * SegÃƒÂºn RD 1619/2012 y Reglamento IVA
 * Entidad para el Libro de Facturas Emitidas
/**

import java.time.LocalDate;
import java.math.BigDecimal;

import lombok.Setter;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;




