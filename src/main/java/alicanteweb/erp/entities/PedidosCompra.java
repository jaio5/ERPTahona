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
public class PedidosCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private Proveedore idProveedor;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Size(max = 50)
    @Column(name = "estado", length = 50)
    private String estado;

}