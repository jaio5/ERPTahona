package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVentaFactura;
import alicanteweb.erp.entities.AlbaranVentaFacturaId;
import alicanteweb.erp.repository.AlbaranVentaFacturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlbaranVentaFacturaService {
    private final AlbaranVentaFacturaRepository repo;
    public AlbaranVentaFacturaService(AlbaranVentaFacturaRepository repo) { this.repo = repo; }
    public List<AlbaranVentaFactura> findAll() { return repo.findAll(); }
    public AlbaranVentaFactura save(AlbaranVentaFactura avf) { return repo.save(avf); }
    public void deleteById(AlbaranVentaFacturaId id) { repo.deleteById(id); }
}

