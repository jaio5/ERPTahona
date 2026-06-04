package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "hojas_ruta_entregas")
public class HojaRutaEntrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoja_ruta_id", nullable = false)
    private HojaRuta hojaRuta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "albaran_id")
    private AlbaranVenta albaran;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "entregado")
    private Boolean entregado = false;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Size(max = 50)
    @Column(name = "estado_entrega", length = 50)
    private String estadoEntrega;

    @Size(max = 100)
    @Column(name = "persona_recepcion", length = 100)
    private String personaRecepcion;

    @Column(name = "firma", length = 1000)
    private String firma;

    @Column(name = "incidencia", length = 500)
    private String incidencia;

    @Column(name = "importe_cobrado", precision = 10, scale = 2)
    private BigDecimal importeCobrado;

    @Size(max = 30)
    @Column(name = "medio_cobro", length = 30)
    private String medioCobro;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HojaRutaEntrega)) return false;
        return Objects.equals(id, ((HojaRutaEntrega) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
