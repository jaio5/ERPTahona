package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tipos de IVA configurables
 * Permite definir los tipos de IVA aplicables y su recargo de equivalencia
 */
@Getter
@Setter
@Entity
@Table(name = "tipos_iva")
public class TipoIva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre; // General, Reducido, Superreducido, Exento

    @NotNull
    @Column(name = "tipo_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tipoIva; // 21, 10, 4, 0

    @Column(name = "recargo_equivalencia", precision = 5, scale = 2)
    private BigDecimal recargoEquivalencia; // 5.2, 1.4, 0.5, 0

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @NotNull
    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "es_por_defecto")
    private Boolean esPorDefecto;

    @Column(name = "codigo_aeat", length = 10)
    private String codigoAeat; // Para SII

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "orden")
    private Integer orden; // Para ordenar en listados

    @PrePersist
    protected void onCreate() {
        if (activo == null) activo = true;
        if (esPorDefecto == null) esPorDefecto = false;
        if (fechaInicio == null) fechaInicio = LocalDate.now();
    }

    /**
     * Verifica si el tipo estÃ¡ vigente en una fecha
     */
    public boolean esVigente(LocalDate fecha) {
        if (!activo) return false;
        if (fecha.isBefore(fechaInicio)) return false;
        if (fechaFin != null && fecha.isAfter(fechaFin)) return false;
        return true;
    }
}


