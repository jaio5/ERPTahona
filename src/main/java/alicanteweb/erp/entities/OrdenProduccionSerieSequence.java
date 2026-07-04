package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "orden_produccion_series", uniqueConstraints = {
    @UniqueConstraint(name = "uk_orden_produccion_series_serie_ejercicio", columnNames = {"serie", "ejercicio"})
})
public class OrdenProduccionSerieSequence {
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
