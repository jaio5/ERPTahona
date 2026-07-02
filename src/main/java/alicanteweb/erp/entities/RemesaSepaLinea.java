package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Recibo individual dentro de una remesa SEPA (un adeudo por factura).
 */
@Getter
@Setter
@Entity
@Table(name = "remesa_sepa_lineas")
public class RemesaSepaLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remesa_id", nullable = false)
    private RemesaSepa remesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "iban_deudor", length = 34)
    private String ibanDeudor;

    @Column(name = "nombre_deudor")
    private String nombreDeudor;

    @Column(name = "mandato_referencia", length = 35)
    private String mandatoReferencia;

    @Column(name = "mandato_fecha")
    private LocalDate mandatoFecha;
}
