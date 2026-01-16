package alicanteweb.erp.repository;

import alicanteweb.erp.entities.Almacen;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AlmacenRepositoryIntegrationTest {

    @Autowired
    private AlmacenRepository almacenRepository;

    @Test
    @Rollback
    void saveAndFind_persistsAlmacenWithNewFields() {
        Almacen a = new Almacen();
        a.setCodigo("INT01");
        a.setNombre("Integracion");
        a.setDescripcion("Desc test");
        a.setCapacidad(new BigDecimal("1234.56"));
        a.setDisponible(new BigDecimal("1000.00"));
        a.setLocalidad("TestCity");
        a.setResponsable("Tester");

        Almacen saved = almacenRepository.save(a);
        assertNotNull(saved.getId());

        Optional<Almacen> opt = almacenRepository.findById(saved.getId());
        assertTrue(opt.isPresent());
        Almacen found = opt.get();
        assertEquals("INT01", found.getCodigo());
        assertEquals("Integracion", found.getNombre());
        assertEquals("Desc test", found.getDescripcion());
        assertEquals(0, found.getCapacidad().compareTo(new BigDecimal("1234.56")));
        assertEquals(0, found.getDisponible().compareTo(new BigDecimal("1000.00")));
        assertEquals("TestCity", found.getLocalidad());
        assertEquals("Tester", found.getResponsable());
    }
}

