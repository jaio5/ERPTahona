package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Modelo 347 - Declaración anual de operaciones con terceros
 * Art. 93 RGAT - Obligatorio para operaciones >3.005,06€ anuales
 */
@Getter
@Setter
@Entity
@Table(name = "modelo347_registros")
public class Modelo347Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ejercicio", nullable = false)
    private Integer ejercicio; // Año fiscal

    @Column(name = "nif_declarado", nullable = false, length = 20)
    private String nifDeclarado; // NIF del cliente o proveedor

    @Column(name = "nombre_declarado", nullable = false, length = 255)
    private String nombreDeclarado;

    @Column(name = "tipo_operacion", nullable = false, length = 1)
    private String tipoOperacion; // A, B, C, D, E, F, G

    @Column(name = "clave_operacion", length = 1)
    private String claveOperacion; // A=Adquisiciones, B=Entregas, etc.

    @Column(name = "es_cliente")
    private Boolean esCliente; // true=cliente, false=proveedor

    @Column(name = "es_proveedor")
    private Boolean esProveedor;

    // Importes trimestrales
    @Column(name = "importe_t1", precision = 13, scale = 2)
    private BigDecimal importeT1 = BigDecimal.ZERO;

    @Column(name = "importe_t2", precision = 13, scale = 2)
    private BigDecimal importeT2 = BigDecimal.ZERO;

    @Column(name = "importe_t3", precision = 13, scale = 2)
    private BigDecimal importeT3 = BigDecimal.ZERO;

    @Column(name = "importe_t4", precision = 13, scale = 2)
    private BigDecimal importeT4 = BigDecimal.ZERO;

    @Column(name = "importe_total", precision = 13, scale = 2)
    private BigDecimal importeTotal = BigDecimal.ZERO;

    @Column(name = "importe_anual", precision = 13, scale = 2)
    private BigDecimal importeAnual = BigDecimal.ZERO;

    // Importes en metálico (>6.000€ debe declararse)
    @Column(name = "importe_metalico_t1", precision = 13, scale = 2)
    private BigDecimal importeMetalicoT1 = BigDecimal.ZERO;

    @Column(name = "importe_metalico_t2", precision = 13, scale = 2)
    private BigDecimal importeMetalicoT2 = BigDecimal.ZERO;

    @Column(name = "importe_metalico_t3", precision = 13, scale = 2)
    private BigDecimal importeMetalicoT3 = BigDecimal.ZERO;

    @Column(name = "importe_metalico_t4", precision = 13, scale = 2)
    private BigDecimal importeMetalicoT4 = BigDecimal.ZERO;

    @Column(name = "importe_metalico_total", precision = 13, scale = 2)
    private BigDecimal importeMetalicoTotal = BigDecimal.ZERO;

    // Datos adicionales
    @Column(name = "provincia", length = 2)
    private String provincia;

    @Column(name = "pais", length = 2)
    private String pais = "ES";

    @Column(name = "numero_operaciones")
    private Integer numeroOperaciones = 0;

    @Column(name = "generado")
    private Boolean generado = false;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /**
     * Calcula el importe total anual
     */
    public void calcularTotales() {
        this.importeTotal = importeT1.add(importeT2).add(importeT3).add(importeT4);
        this.importeMetalicoTotal = importeMetalicoT1.add(importeMetalicoT2)
                .add(importeMetalicoT3).add(importeMetalicoT4);
    }

    /**
     * Verifica si supera el límite de declaración (3.005,06€)
     */
    public boolean superaLimite() {
        return importeTotal.compareTo(new BigDecimal("3005.06")) > 0;
    }

    /**
     * Determina el tipo de operación según normativa
     */
    public String determinarTipoOperacion() {
        if (esProveedor != null && esProveedor) {
            return "A"; // Compras
        } else if (esCliente != null && esCliente) {
            return "B"; // Ventas
        }
        return "B"; // Por defecto
    }
}

