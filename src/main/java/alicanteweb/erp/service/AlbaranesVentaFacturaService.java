package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranesVentaFactura;
import alicanteweb.erp.entities.AlbaranesVentaFacturaId;
import alicanteweb.erp.repository.AlbaranesVentaFacturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbaranesVentaFacturaService {
    private final AlbaranesVentaFacturaRepository repo;
    public AlbaranesVentaFacturaService(AlbaranesVentaFacturaRepository repo) { this.repo = repo; }
    public List<AlbaranesVentaFactura> findAll() { return repo.findAll(); }
    public AlbaranesVentaFactura save(AlbaranesVentaFactura avf) { return repo.save(avf); }
    public void deleteById(AlbaranesVentaFacturaId id) { repo.deleteById(id); }
}

