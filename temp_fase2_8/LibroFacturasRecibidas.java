package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad para el Libro de Facturas Recibidas
 * SegÃƒÂºn RD 1619/2012 y Reglamento IVA
 */
@Getter
@Setter
@Entity
@Table(name = "libro_facturas_recibidas")
public class LibroFacturasRecibidas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factura_compra_id")
    private Long facturaCompraId; // Referencia a factura de compra si existe

    @NotNull
    @Column(name = "fecha_expedicion", nullable = false)
    private LocalDate fechaExpedicion;

    @Column(name = "fecha_operacion")
    private LocalDate fechaOperacion;

    @Column(name = "fecha_contable")
    private LocalDate fechaContable;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @NotNull
    @Column(name = "numero_factura", nullable = false, length = 100)
    private String numeroFactura;

    @Column(name = "tipo_factura", length = 30)
    private String tipoFactura;

    @NotNull
    @Column(name = "cif_emisor", nullable = false, length = 20)
    private String cifEmisor;

    @NotNull
    @Column(name = "nombre_emisor", nullable = false, length = 200)
    private String nombreEmisor;

    @Column(name = "pais_emisor", length = 2)
    private String paisEmisor; // ISO 3166-1 alpha-2

    @NotNull
    @Column(name = "base_imponible", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseImponible;

    @NotNull
    @Column(name = "tipo_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tipoIva;

    @NotNull
    @Column(name = "cuota_iva_soportada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cuotaIvaSoportada;

    @Column(name = "cuota_iva_deducible", precision = 10, scale = 2)
    private BigDecimal cuotaIvaDeducible;

    @Column(name = "porcentaje_deduccion", precision = 5, scale = 2)
    private BigDecimal porcentajeDeduccion;

    @NotNull
    @Column(name = "total_factura", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFactura;

    @Column(name = "tipo_gasto", length = 50)
    private String tipoGasto; // Corriente, InversiÃƒÂ³n

    @Column(name = "inversion_sujeto_pasivo")
    private Boolean inversionSujetoPasivo;

    @Column(name = "regimen_especial", length = 50)
    private String regimenEspecial;

    @Column(name = "factura_rectificativa")
    private Boolean facturaRectificativa;

    @Column(name = "factura_simplificada")
    private Boolean facturaSimplificada;

    @Column(name = "numero_registro")
    private Integer numeroRegistro;

    @Column(name = "ejercicio")
    private Integer ejercicio;

    @Column(name = "periodo")
    private Integer periodo;

    @Column(name = "intracomunitaria")
    private Boolean intracomunitaria;

    @Column(name = "importacion")
    private Boolean importacion;

    @Column(name = "clave_operacion", length = 2)
    private String claveOperacion;

    @Column(name = "deducible_pro_rata")
    private Boolean deducibleProRata;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @PrePersist
    protected void onCreate() {
        if (ejercicio == null && fechaExpedicion != null) {
            ejercicio = fechaExpedicion.getYear();
        }
        if (periodo == null && fechaExpedicion != null) {
            periodo = (fechaExpedicion.getMonthValue() - 1) / 3 + 1;
        }
        if (intracomunitaria == null) intracomunitaria = false;
        if (importacion == null) importacion = false;
        if (inversionSujetoPasivo == null) inversionSujetoPasivo = false;
        if (facturaRectificativa == null) facturaRectificativa = false;
        if (facturaSimplificada == null) facturaSimplificada = false;
        if (deducibleProRata == null) deducibleProRata = false;
        if (porcentajeDeduccion == null) porcentajeDeduccion = new BigDecimal("100");
        if (cuotaIvaDeducible == null && cuotaIvaSoportada != null) {
            cuotaIvaDeducible = cuotaIvaSoportada.multiply(porcentajeDeduccion).divide(new BigDecimal("100"));
        }
    }
}



