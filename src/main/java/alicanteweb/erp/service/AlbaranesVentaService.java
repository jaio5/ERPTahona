package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranesVenta;
import alicanteweb.erp.repository.AlbaranesVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AlbaranesVentaService {

    private final AlbaranesVentaRepository repository;

    public AlbaranesVentaService(AlbaranesVentaRepository repository) {
        this.repository = repository;
    }

    public List<AlbaranesVenta> findAll() {
        return repository.findAll();
    }

    public Optional<AlbaranesVenta> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<AlbaranesVenta> findByNumero(String numero) {
        return repository.findByNumero(numero);
    }

    @Transactional
    public AlbaranesVenta save(AlbaranesVenta albaran) {
        return repository.save(albaran);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void imprimirAlbaran(AlbaranesVenta albaran) {
        // Aquí deberías implementar la lógica real de impresión (PDF, JasperReports, etc.)
        System.out.println("Imprimiendo albarán: " + albaran.getNumero());
        // Ejemplo: Generar PDF, abrir diálogo de impresión, etc.
    }
}
