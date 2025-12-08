package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "pedidos_compra")
public class PedidoCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private Proveedor idProveedor;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Size(max = 50)
    @Column(name = "estado", length = 50)
    private String estado;

    @Size(max = 50)
    @Column(name = "numero", length = 50)
    private String numero;

    @Column(name = "total")
    private Float total;

    // Lombok genera los getters y setters para numero y total
    public String getProveedor() {
        return idProveedor != null ? idProveedor.getNombre() : null;
    }

}