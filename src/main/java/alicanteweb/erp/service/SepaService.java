package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.RemesaSepa;
import alicanteweb.erp.entities.RemesaSepaLinea;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.RemesaSepaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Remesas SEPA de adeudos directos CORE (fichero pain.008.001.02, aceptado por
 * la banca española como Cuaderno 19-14 XML).
 *
 * Flujo: facturas emitidas pendientes de clientes con IBAN y mandato →
 * crearRemesa genera el XML para subir al banco → al confirmarse el cargo,
 * marcarCobrada registra los cobros en cartera.
 */
@Service
public class SepaService {

    private static final Logger log = LoggerFactory.getLogger(SepaService.class);

    private final RemesaSepaRepository remesaRepository;
    private final FacturaRepository facturaRepository;
    private final CarteraService carteraService;
    private final EmpresaConfigService empresaConfigService;
    private final AuditoriaService auditoriaService;

    public SepaService(RemesaSepaRepository remesaRepository,
                       FacturaRepository facturaRepository,
                       CarteraService carteraService,
                       EmpresaConfigService empresaConfigService,
                       AuditoriaService auditoriaService) {
        this.remesaRepository = remesaRepository;
        this.facturaRepository = facturaRepository;
        this.carteraService = carteraService;
        this.empresaConfigService = empresaConfigService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<RemesaSepa> listar() {
        return remesaRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<RemesaSepa> buscarPorId(Long id) {
        return remesaRepository.findById(id);
    }

    /**
     * Facturas emitidas, con pendiente, de clientes con IBAN y mandato, y que
     * no estén ya incluidas en otra remesa viva.
     */
    @Transactional(readOnly = true)
    public List<Factura> facturasElegibles() {
        Set<Long> enRemesa = new HashSet<>(remesaRepository.facturasEnRemesasVivas());
        return facturaRepository.findByEstado("EMITIDA").stream()
                .filter(f -> !f.isPagada())
                .filter(f -> pendiente(f).compareTo(BigDecimal.ZERO) > 0)
                .filter(f -> !enRemesa.contains(f.getId()))
                .filter(f -> tieneMandato(f.getCliente()))
                .toList();
    }

    @Transactional
    public RemesaSepa crearRemesa(List<Long> facturaIds, LocalDate fechaCobro, String concepto, Usuario usuario) {
        if (facturaIds == null || facturaIds.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos una factura para la remesa");
        }
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActiva()
                .orElseThrow(() -> new IllegalStateException("No hay configuración de empresa activa"));
        if (esBlank(empresa.getIban()) || esBlank(empresa.getSepaCreditorId())) {
            throw new IllegalStateException("Configura el IBAN y el identificador de acreedor SEPA de la empresa "
                    + "(pantalla Empresa) antes de generar remesas");
        }
        LocalDate cobro = fechaCobro != null ? fechaCobro : LocalDate.now().plusDays(3);

        Set<Long> enRemesa = new HashSet<>(remesaRepository.facturasEnRemesasVivas());

        RemesaSepa remesa = new RemesaSepa();
        remesa.setFechaCobro(cobro);
        remesa.setConcepto(concepto);
        remesa.setUsuarioCreacion(usuario != null ? usuario.getUsername() : null);

        BigDecimal total = BigDecimal.ZERO;
        List<RemesaSepaLinea> lineas = new ArrayList<>();
        for (Long facturaId : facturaIds) {
            Factura factura = facturaRepository.findById(facturaId)
                    .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + facturaId));
            if (enRemesa.contains(facturaId)) {
                throw new IllegalArgumentException("La factura " + factura.getNumero() + " ya está en otra remesa");
            }
            if (!tieneMandato(factura.getCliente())) {
                throw new IllegalArgumentException("El cliente de la factura " + factura.getNumero()
                        + " no tiene IBAN y mandato SEPA configurados");
            }
            BigDecimal importe = pendiente(factura);
            if (importe.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La factura " + factura.getNumero() + " no tiene importe pendiente");
            }
            Cliente cliente = factura.getCliente();
            RemesaSepaLinea linea = new RemesaSepaLinea();
            linea.setRemesa(remesa);
            linea.setFactura(factura);
            linea.setImporte(importe);
            linea.setIbanDeudor(normalizarIban(cliente.getIban()));
            linea.setNombreDeudor(cliente.getNombre());
            linea.setMandatoReferencia(cliente.getMandatoSepaReferencia());
            linea.setMandatoFecha(cliente.getMandatoSepaFecha());
            lineas.add(linea);
            total = total.add(importe);
        }
        remesa.setLineas(lineas);
        remesa.setTotal(total);
        remesa.setNumRecibos(lineas.size());

        RemesaSepa guardada = remesaRepository.save(remesa);
        guardada.setXml(generarPain008(guardada, empresa));
        guardada = remesaRepository.save(guardada);

        auditoriaService.registrarAccion(usuario, "REMESA_SEPA_CREADA", "RemesaSepa",
                String.valueOf(guardada.getId()),
                "Remesa de " + lineas.size() + " recibos por " + total + " EUR, cobro " + cobro);
        log.info("Remesa SEPA {} creada: {} recibos, total {}", guardada.getId(), lineas.size(), total);
        return guardada;
    }

    /**
     * Marca la remesa como cobrada y registra en cartera el cobro de cada factura.
     */
    @Transactional
    public RemesaSepa marcarCobrada(Long remesaId, Usuario usuario) {
        RemesaSepa remesa = remesaRepository.findById(remesaId)
                .orElseThrow(() -> new IllegalArgumentException("Remesa no encontrada"));
        if (!"GENERADA".equals(remesa.getEstado())) {
            throw new IllegalStateException("Solo se pueden marcar como cobradas remesas en estado GENERADA");
        }
        for (RemesaSepaLinea linea : remesa.getLineas()) {
            carteraService.registrarCobro(linea.getFactura().getId(), LocalDate.now(), linea.getImporte(),
                    "RECIBO", "Remesa SEPA " + remesa.getId(), null, usuario);
        }
        remesa.setEstado("COBRADA");
        RemesaSepa guardada = remesaRepository.save(remesa);
        auditoriaService.registrarAccion(usuario, "REMESA_SEPA_COBRADA", "RemesaSepa",
                String.valueOf(remesaId), "Remesa cobrada: " + remesa.getNumRecibos() + " recibos");
        return guardada;
    }

    @Transactional
    public RemesaSepa anular(Long remesaId, Usuario usuario) {
        RemesaSepa remesa = remesaRepository.findById(remesaId)
                .orElseThrow(() -> new IllegalArgumentException("Remesa no encontrada"));
        if (!"GENERADA".equals(remesa.getEstado())) {
            throw new IllegalStateException("Solo se pueden anular remesas en estado GENERADA");
        }
        remesa.setEstado("ANULADA");
        auditoriaService.registrarAccion(usuario, "REMESA_SEPA_ANULADA", "RemesaSepa",
                String.valueOf(remesaId), "Remesa anulada");
        return remesaRepository.save(remesa);
    }

    // ───────────────────────── pain.008.001.02 ─────────────────────────

    String generarPain008(RemesaSepa remesa, EmpresaConfig empresa) {
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
        String msgId = "REM-" + remesa.getId() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreEmpresa = xml(empresa.getNombreEmpresa());
        String ibanAcreedor = normalizarIban(empresa.getIban());
        String creditorId = empresa.getSepaCreditorId().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.008.001.02\" ")
          .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        sb.append("  <CstmrDrctDbtInitn>\n");
        sb.append("    <GrpHdr>\n");
        sb.append("      <MsgId>").append(msgId).append("</MsgId>\n");
        sb.append("      <CreDtTm>").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</CreDtTm>\n");
        sb.append("      <NbOfTxs>").append(remesa.getNumRecibos()).append("</NbOfTxs>\n");
        sb.append("      <CtrlSum>").append(remesa.getTotal().toPlainString()).append("</CtrlSum>\n");
        sb.append("      <InitgPty><Nm>").append(nombreEmpresa).append("</Nm></InitgPty>\n");
        sb.append("    </GrpHdr>\n");
        sb.append("    <PmtInf>\n");
        sb.append("      <PmtInfId>").append(msgId).append("-P1</PmtInfId>\n");
        sb.append("      <PmtMtd>DD</PmtMtd>\n");
        sb.append("      <NbOfTxs>").append(remesa.getNumRecibos()).append("</NbOfTxs>\n");
        sb.append("      <CtrlSum>").append(remesa.getTotal().toPlainString()).append("</CtrlSum>\n");
        sb.append("      <PmtTpInf>\n");
        sb.append("        <SvcLvl><Cd>SEPA</Cd></SvcLvl>\n");
        sb.append("        <LclInstrm><Cd>CORE</Cd></LclInstrm>\n");
        sb.append("        <SeqTp>RCUR</SeqTp>\n");
        sb.append("      </PmtTpInf>\n");
        sb.append("      <ReqdColltnDt>").append(remesa.getFechaCobro().format(iso)).append("</ReqdColltnDt>\n");
        sb.append("      <Cdtr><Nm>").append(nombreEmpresa).append("</Nm></Cdtr>\n");
        sb.append("      <CdtrAcct><Id><IBAN>").append(ibanAcreedor).append("</IBAN></Id></CdtrAcct>\n");
        sb.append("      <CdtrAgt><FinInstnId/></CdtrAgt>\n");
        sb.append("      <ChrgBr>SLEV</ChrgBr>\n");
        sb.append("      <CdtrSchmeId><Id><PrvtId><Othr>\n");
        sb.append("        <Id>").append(xml(creditorId)).append("</Id>\n");
        sb.append("        <SchmeNm><Prtry>SEPA</Prtry></SchmeNm>\n");
        sb.append("      </Othr></PrvtId></Id></CdtrSchmeId>\n");
        for (RemesaSepaLinea linea : remesa.getLineas()) {
            String numeroFactura = linea.getFactura() != null ? linea.getFactura().getNumero() : "";
            sb.append("      <DrctDbtTxInf>\n");
            sb.append("        <PmtId><EndToEndId>").append(xml("FRA-" + numeroFactura)).append("</EndToEndId></PmtId>\n");
            sb.append("        <InstdAmt Ccy=\"EUR\">").append(linea.getImporte().toPlainString()).append("</InstdAmt>\n");
            sb.append("        <DrctDbtTx><MndtRltdInf>\n");
            sb.append("          <MndtId>").append(xml(linea.getMandatoReferencia())).append("</MndtId>\n");
            sb.append("          <DtOfSgntr>").append(linea.getMandatoFecha() != null ? linea.getMandatoFecha().format(iso) : remesa.getFechaCobro().format(iso)).append("</DtOfSgntr>\n");
            sb.append("        </MndtRltdInf></DrctDbtTx>\n");
            sb.append("        <DbtrAgt><FinInstnId/></DbtrAgt>\n");
            sb.append("        <Dbtr><Nm>").append(xml(linea.getNombreDeudor())).append("</Nm></Dbtr>\n");
            sb.append("        <DbtrAcct><Id><IBAN>").append(xml(linea.getIbanDeudor())).append("</IBAN></Id></DbtrAcct>\n");
            sb.append("        <RmtInf><Ustrd>").append(xml(descripcionRecibo(remesa, numeroFactura))).append("</Ustrd></RmtInf>\n");
            sb.append("      </DrctDbtTxInf>\n");
        }
        sb.append("    </PmtInf>\n");
        sb.append("  </CstmrDrctDbtInitn>\n");
        sb.append("</Document>\n");
        return sb.toString();
    }

    private static String descripcionRecibo(RemesaSepa remesa, String numeroFactura) {
        String base = remesa.getConcepto() != null && !remesa.getConcepto().isBlank()
                ? remesa.getConcepto() : "Factura";
        return base + " " + numeroFactura;
    }

    private BigDecimal pendiente(Factura f) {
        BigDecimal total = f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO;
        BigDecimal pagado = f.getPagado() != null ? f.getPagado() : BigDecimal.ZERO;
        return total.subtract(pagado);
    }

    private static boolean tieneMandato(Cliente cliente) {
        return cliente != null && !esBlank(cliente.getIban()) && !esBlank(cliente.getMandatoSepaReferencia());
    }

    private static String normalizarIban(String iban) {
        return iban != null ? iban.replaceAll("\\s+", "").toUpperCase() : "";
    }

    private static boolean esBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String xml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
