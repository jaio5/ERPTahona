package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Registro para Modelo 347 - DeclaraciÃ³n anual de operaciones con terceros
 * Operaciones superiores a 3.005,06 euros anuales
 */
@Getter
@Setter
@Entity
@Table(name = "modelo_347_registro")
public class Modelo347Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "ejercicio", nullable = false)
    private Integer ejercicio;

    @NotNull
    @Column(name = "nif_declarante", nullable = false, length = 20)
    private String nifDeclarante;

    @NotNull
    @Column(name = "nif_tercero", nullable = false, length = 20)
    private String nifTercero;

    @NotNull
    @Column(name = "nombre_tercero", nullable = false, length = 200)
    private String nombreTercero;

    @Column(name = "provincia_tercero", length = 2)
    private String provinciaTercero;

    @Column(name = "pais_tercero", length = 2)
    private String paisTercero;

    @NotNull
    @Column(name = "clave_operacion", nullable = false, length = 1)
    private String claveOperacion; // A=Adquisiciones, B=Entregas

    @NotNull
    @Column(name = "importe_operaciones", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeOperaciones;

    @Column(name = "importe_primer_trimestre", precision = 12, scale = 2)
    private BigDecimal importePrimerTrimestre;

    @Column(name = "importe_segundo_trimestre", precision = 12, scale = 2)
    private BigDecimal importeSegundoTrimestre;

    @Column(name = "importe_tercer_trimestre", precision = 12, scale = 2)
    private BigDecimal importeTercerTrimestre;

    @Column(name = "importe_cuarto_trimestre", precision = 12, scale = 2)
    private BigDecimal importeCuartoTrimestre;

    @Column(name = "seguros_y_capitalizacion")
    private Boolean segurosYCapitalizacion;

    @Column(name = "arrendamiento_local_negocio")
    private Boolean arrendamientoLocalNegocio;

    @Column(name = "operacion_regimen_simplificado")
    private Boolean operacionRegimenSimplificado;

    @Column(name = "operacion_criterio_caja")
    private Boolean operacionCriterioCaja;

    @Column(name = "operacion_inversion_sujeto_pasivo")
    private Boolean operacionInversionSujetoPasivo;

    @Column(name = "importe_metalico", precision = 12, scale = 2)
    private BigDecimal importeMetalico;

    @Column(name = "numero_facturas")
    private Integer numeroFacturas;

    @Column(name = "generado")
    private Boolean generado;

    @Column(name = "incluido_en_declaracion")
    private Boolean incluidoEnDeclaracion;

    @PrePersist
    protected void onCreate() {
        if (generado == null) generado = false;
        if (incluidoEnDeclaracion == null) incluidoEnDeclaracion = false;
        if (segurosYCapitalizacion == null) segurosYCapitalizacion = false;
        if (arrendamientoLocalNegocio == null) arrendamientoLocalNegocio = false;
        if (operacionRegimenSimplificado == null) operacionRegimenSimplificado = false;
        if (operacionCriterioCaja == null) operacionCriterioCaja = false;
        if (operacionInversionSujetoPasivo == null) operacionInversionSujetoPasivo = false;
    }
}


