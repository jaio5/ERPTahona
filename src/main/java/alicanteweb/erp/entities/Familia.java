package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "familias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Familia {

    @Id
    @Column(name = "CodigoFamilia", length = 500)
    private String codigoFamilia;

    @Column(name = "DescripcionFamilia", length = 500)
    private String descripcionFamilia;
}

