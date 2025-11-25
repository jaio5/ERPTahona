package alicanteweb.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AlbaranesVentaFacturaId implements Serializable {
    private static final long serialVersionUID = -822507999709587557L;
    @NotNull
    @Column(name = "albaranesVentas_id", nullable = false)
    private Long albaranesventasId;

    @NotNull
    @Column(name = "facturas_id", nullable = false)
    private Long facturasId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AlbaranesVentaFacturaId entity = (AlbaranesVentaFacturaId) o;
        return Objects.equals(this.facturasId, entity.facturasId) &&
                Objects.equals(this.albaranesventasId, entity.albaranesventasId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(facturasId, albaranesventasId);
    }

}