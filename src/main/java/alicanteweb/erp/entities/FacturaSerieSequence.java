package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "factura_series", uniqueConstraints = {
    @UniqueConstraint(name = "uk_factura_series_serie_ejercicio", columnNames = {"serie", "ejercicio"})
})
public class FacturaSerieSequence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "serie", nullable = false, length = 20)
    private String serie;

    @Column(name = "ejercicio", nullable = false)
    private Integer ejercicio;

    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero;
}
