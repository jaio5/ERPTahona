package alicanteweb.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "albaran_series", uniqueConstraints = {
    @UniqueConstraint(name = "uk_albaran_series_serie_ejercicio", columnNames = {"serie", "ejercicio"})
})
public class AlbaranSerieSequence {
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
