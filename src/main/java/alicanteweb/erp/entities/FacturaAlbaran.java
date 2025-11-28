package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "factura_albaran")
public class FacturaAlbaran {
    @EmbeddedId
    private FacturaAlbaranId id;

    @MapsId("facturaId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    // El campo albaran es necesario para los métodos del repositorio
    @MapsId("albaranId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "albaran_id", nullable = false)
    private AlbaranVenta albaran;

}