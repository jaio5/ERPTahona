package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "albaranes_venta_facturas")
public class AlbaranesVentaFactura {
    @EmbeddedId
    private AlbaranesVentaFacturaId id;

    @MapsId("albaranesventasId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "albaranesVentas_id", nullable = false)
    private AlbaranesVenta albaranesVentas;

    @MapsId("facturasId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facturas_id", nullable = false)
    private Factura facturas;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "albaranes_ventas_id", nullable = false)
    private AlbaranesVenta albaranesVentas1;

}