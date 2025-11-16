package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provincias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Provincia {

    @Id
    @Column(name = "CodigoProvincia", length = 500)
    private String codigoProvincia;

    @Column(name = "NombreProvincia", length = 500)
    private String nombreProvincia;

    @Column(name = "Zona", length = 500)
    private String zona;
}

