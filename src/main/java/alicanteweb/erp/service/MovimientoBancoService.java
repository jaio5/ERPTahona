package alicanteweb.erp.service;

import alicanteweb.erp.entities.Banco;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.repository.BancoRepository;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.MovimientoBancoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MovimientoBancoService {

    private static final Logger log = LoggerFactory.getLogger(MovimientoBancoService.class);
    private static final DateTimeFormatter FMT_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MovimientoBancoRepository repository;
    private final BancoRepository bancoRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaCompraRepository facturaCompraRepository;

    public MovimientoBancoService(MovimientoBancoRepository repository,
                                  BancoRepository bancoRepository,
                                  FacturaRepository facturaRepository,
                                  FacturaCompraRepository facturaCompraRepository) {
        this.repository = repository;
        this.bancoRepository = bancoRepository;
        this.facturaRepository = facturaRepository;
        this.facturaCompraRepository = facturaCompraRepository;
    }

    public List<MovimientoBanco> findAll() {
        return repository.findAllWithBanco();
    }

    public List<Banco> findBancosActivos() {
        return bancoRepository.findByActivoTrue();
    }

    public Optional<MovimientoBanco> findById(Long id) {
        return repository.findById(id);
    }

    public List<MovimientoBanco> findByFechas(LocalDate desde, LocalDate hasta) {
        return repository.findByFechaBetween(desde, hasta);
    }

    public List<MovimientoBanco> findNoConciliados() {
        return repository.findByConciliado(false);
    }

    public List<MovimientoBanco> buscarPorConcepto(String concepto) {
        return repository.findByConceptoContainingIgnoreCase(concepto);
    }

    @Transactional
    public MovimientoBanco save(MovimientoBanco movimiento) {
        MovimientoBanco guardado = repository.save(movimiento);
        recalcularSaldosBanco(guardado.getBanco().getId());
        return guardado;
    }

    @Transactional
    public void deleteById(Long id) {
        Long bancoId = repository.findById(id)
            .map(m -> m.getBanco() != null ? m.getBanco().getId() : null)
            .orElse(null);
        repository.deleteById(id);
        if (bancoId != null) {
            recalcularSaldosBanco(bancoId);
        }
    }

    @Transactional
    public MovimientoBanco conciliar(Long id) {
        MovimientoBanco m = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento bancario no encontrado: " + id));
        m.setConciliado(true);
        m.setFechaConciliacion(LocalDate.now());
        return repository.save(m);
    }

    @Transactional
    public MovimientoBanco conciliarConFactura(Long movimientoId, Long facturaId) {
        MovimientoBanco m = repository.findById(movimientoId)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado: " + movimientoId));
        Factura f = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + facturaId));
        m.setFactura(f);
        m.setConciliado(true);
        m.setFechaConciliacion(LocalDate.now());
        return repository.save(m);
    }

    @Transactional
    public MovimientoBanco conciliarConFacturaCompra(Long movimientoId, Long facturaCompraId) {
        MovimientoBanco m = repository.findById(movimientoId)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado: " + movimientoId));
        FacturaCompra fc = facturaCompraRepository.findById(facturaCompraId)
                .orElseThrow(() -> new IllegalArgumentException("Factura compra no encontrada: " + facturaCompraId));
        m.setFacturaCompra(fc);
        m.setConciliado(true);
        m.setFechaConciliacion(LocalDate.now());
        return repository.save(m);
    }

    /** Matching automático por importe exacto (±0.01€) en ±30 días */
    @Transactional(readOnly = true)
    public List<CandidatoConciliacion> buscarCandidatos(MovimientoBanco mov) {
        List<CandidatoConciliacion> candidatos = new ArrayList<>();
        BigDecimal importe = mov.getImporte() != null ? mov.getImporte().abs() : BigDecimal.ZERO;
        BigDecimal tolerancia = new BigDecimal("0.01");
        LocalDate desde = mov.getFecha().minusDays(30);
        LocalDate hasta = mov.getFecha().plusDays(30);

        if ("INGRESO".equals(mov.getTipo())) {
            facturaRepository.findByFechaBetween(desde, hasta).stream()
                    .filter(f -> !f.isPagada() && f.getTotal() != null
                            && f.getTotal().subtract(importe).abs().compareTo(tolerancia) <= 0)
                    .forEach(f -> candidatos.add(new CandidatoConciliacion("FACTURA", f.getId(),
                            f.getNumero(), f.getTotal())));
        } else if ("GASTO".equals(mov.getTipo())) {
            facturaCompraRepository.findByEstado("PENDIENTE").stream()
                    .filter(fc -> fc.getTotal() != null
                            && fc.getTotal().subtract(importe).abs().compareTo(tolerancia) <= 0)
                    .forEach(fc -> candidatos.add(new CandidatoConciliacion("FACTURA_COMPRA", fc.getId(),
                            fc.getNumero(), fc.getTotal())));
        }
        return candidatos;
    }

    public record CandidatoConciliacion(String tipo, Long id, String referencia, BigDecimal importe) {}

    public record ResultadoImportacionCSV(int importados, List<String> errores) {}

    @Transactional
    public ResultadoImportacionCSV importarDesdeCSV(Long bancoId, MultipartFile archivo) {
        Banco banco = bancoRepository.findById(bancoId)
                .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado con id: " + bancoId));
        List<String> errores = new ArrayList<>();
        int importados = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // saltar cabecera
            String linea;
            int numeroLinea = 1;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                String[] cols = linea.split(";|,");
                if (cols.length < 3) continue;
                try {
                    MovimientoBanco mb = parsearLineaCSV(banco, cols);
                    save(mb);
                    importados++;
                } catch (RuntimeException e) {
                    log.warn("Error en línea {} del CSV: {}", numeroLinea, e.getMessage());
                    errores.add("Línea " + numeroLinea + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error leyendo archivo CSV de extracto bancario: {}", e.getMessage(), e);
            errores.add("Error leyendo archivo: " + e.getMessage());
        }
        return new ResultadoImportacionCSV(importados, errores);
    }

    private MovimientoBanco parsearLineaCSV(Banco banco, String[] cols) {
        MovimientoBanco mb = new MovimientoBanco();
        mb.setBanco(banco);
        mb.setFecha(LocalDate.parse(cols[0].trim(), FMT_CSV));
        mb.setConcepto(cols.length > 1 ? cols[1].trim() : "");
        double importe = Double.parseDouble(cols[2].trim().replace(",", "."));
        mb.setImporte(BigDecimal.valueOf(Math.abs(importe)));
        mb.setTipo(importe >= 0 ? "INGRESO" : "GASTO");
        mb.setConciliado(false);
        return mb;
    }

    private void recalcularSaldosBanco(Long bancoId) {
        Banco banco = bancoRepository.findById(bancoId)
            .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado: " + bancoId));
        BigDecimal saldo = BigDecimal.ZERO;
        for (MovimientoBanco movimiento : repository.findByBancoIdOrderByFechaAscIdAsc(bancoId)) {
            BigDecimal importe = movimiento.getImporte() != null ? movimiento.getImporte() : BigDecimal.ZERO;
            saldo = "GASTO".equals(movimiento.getTipo()) ? saldo.subtract(importe) : saldo.add(importe);
            movimiento.setSaldoResultante(saldo);
            repository.save(movimiento);
        }
        banco.setSaldoActual(saldo);
        bancoRepository.save(banco);
    }
}
