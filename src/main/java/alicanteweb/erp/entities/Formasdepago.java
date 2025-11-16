package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "formasdepago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formasdepago {

    @Id
    @Column(name = "CodigoFormaPago", length = 500)
    private String codigoFormaPago;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;
}

