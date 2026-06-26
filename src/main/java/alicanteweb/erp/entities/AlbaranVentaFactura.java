package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "albaranes_venta_facturas")
public class AlbaranVentaFactura {
    @EmbeddedId
    private AlbaranVentaFacturaId id;

    @MapsId("facturasId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facturas_id", nullable = false)
    private Factura facturas;

    // Relación al albarán: se asigna al campo embebido albaranesventasId
    @MapsId("albaranesventasId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "albaranes_ventas_id", nullable = false)
    private AlbaranVenta albaran;

}