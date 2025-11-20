package alicanteweb.erp.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @MapsId("albaranId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "albaran_id", nullable = false)
    private AlbaranesVenta albaran;

    public FacturaAlbaranId getId() {
        return id;
    }

    public void setId(FacturaAlbaranId id) {
        this.id = id;
    }

    public Factura getFactura() {
        return factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public AlbaranesVenta getAlbaran() {
        return albaran;
    }

    public void setAlbaran(AlbaranesVenta albaran) {
        this.albaran = albaran;
    }

}