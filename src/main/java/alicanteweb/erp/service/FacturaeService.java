package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.util.FinancialMath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generación de facturas electrónicas Facturae 3.2.2 (formato exigido por las
 * administraciones públicas españolas y base de la futura e-factura B2B de la
 * Ley Crea y Crece).
 *
 * El XML se genera sin firmar: la presentación en FACe exige firma XAdES con
 * certificado cualificado (puede firmarse externamente, p.ej. con AutoFirma).
 */
@Service
public class FacturaeService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final FacturaRepository facturaRepository;
    private final EmpresaConfigService empresaConfigService;

    public FacturaeService(FacturaRepository facturaRepository,
                           EmpresaConfigService empresaConfigService) {
        this.facturaRepository = facturaRepository;
        this.empresaConfigService = empresaConfigService;
    }

    @Transactional(readOnly = true)
    public String generarFacturaeXml(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        if (!"EMITIDA".equals(factura.getEstado())) {
            throw new IllegalStateException("Solo se puede generar Facturae de facturas emitidas");
        }
        Cliente cliente = factura.getCliente();
        if (cliente == null || cliente.getCif() == null || cliente.getCif().isBlank()) {
            throw new IllegalStateException("Facturae requiere un cliente con NIF/CIF");
        }
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActiva()
                .orElseThrow(() -> new IllegalStateException("No hay configuración de empresa activa"));

        Map<BigDecimal, BigDecimal[]> desglose = desglosePorTipo(factura);
        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalCuota = BigDecimal.ZERO;
        for (BigDecimal[] valores : desglose.values()) {
            totalBase = totalBase.add(valores[0]);
            totalCuota = totalCuota.add(valores[1]);
        }
        BigDecimal retencion = nvl(factura.getRetencionIrpf());
        BigDecimal total = nvl(factura.getTotal());

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<fe:Facturae xmlns:fe=\"http://www.facturae.es/Facturae/2014/v3.2.1/Facturae\" ")
           .append("xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n");
        xml.append("  <FileHeader>\n");
        xml.append("    <SchemaVersion>3.2.2</SchemaVersion>\n");
        xml.append("    <Modality>I</Modality>\n");
        xml.append("    <InvoiceIssuerType>EM</InvoiceIssuerType>\n");
        xml.append("    <Batch>\n");
        xml.append("      <BatchIdentifier>").append(esc(nifLimpio(empresa.getCif()) + numeroCompleto(factura))).append("</BatchIdentifier>\n");
        xml.append("      <InvoicesCount>1</InvoicesCount>\n");
        xml.append("      <TotalInvoicesAmount><TotalAmount>").append(dec(total)).append("</TotalAmount></TotalInvoicesAmount>\n");
        xml.append("      <TotalOutstandingAmount><TotalAmount>").append(dec(total)).append("</TotalAmount></TotalOutstandingAmount>\n");
        xml.append("      <TotalExecutableAmount><TotalAmount>").append(dec(total)).append("</TotalAmount></TotalExecutableAmount>\n");
        xml.append("      <InvoiceCurrencyCode>EUR</InvoiceCurrencyCode>\n");
        xml.append("    </Batch>\n");
        xml.append("  </FileHeader>\n");
        xml.append("  <Parties>\n");
        xml.append(parte("SellerParty", nifLimpio(empresa.getCif()), empresa.getNombreEmpresa(),
                empresa.getDireccion(), empresa.getCodigoPostal(), empresa.getCiudad(), empresa.getProvincia()));
        xml.append(parte("BuyerParty", nifLimpio(cliente.getCif()), cliente.getNombre(),
                cliente.getDireccion(), cliente.getCodigoPostal(), cliente.getPoblacion(), cliente.getProvincia()));
        xml.append("  </Parties>\n");
        xml.append("  <Invoices>\n");
        xml.append("    <Invoice>\n");
        xml.append("      <InvoiceHeader>\n");
        xml.append("        <InvoiceNumber>").append(esc(factura.getNumero())).append("</InvoiceNumber>\n");
        if (factura.getSerie() != null && !factura.getSerie().isBlank()) {
            xml.append("        <InvoiceSeriesCode>").append(esc(factura.getSerie())).append("</InvoiceSeriesCode>\n");
        }
        xml.append("        <InvoiceDocumentType>FC</InvoiceDocumentType>\n");
        xml.append("        <InvoiceClass>").append("RECTIFICATIVA".equals(factura.getTipoFactura()) ? "OR" : "OO").append("</InvoiceClass>\n");
        xml.append("      </InvoiceHeader>\n");
        xml.append("      <InvoiceIssueData>\n");
        xml.append("        <IssueDate>").append(factura.getFecha().format(ISO)).append("</IssueDate>\n");
        xml.append("        <InvoiceCurrencyCode>EUR</InvoiceCurrencyCode>\n");
        xml.append("        <TaxCurrencyCode>EUR</TaxCurrencyCode>\n");
        xml.append("        <LanguageName>es</LanguageName>\n");
        xml.append("      </InvoiceIssueData>\n");
        xml.append("      <TaxesOutputs>\n");
        for (Map.Entry<BigDecimal, BigDecimal[]> entry : desglose.entrySet()) {
            xml.append("        <Tax>\n");
            xml.append("          <TaxTypeCode>01</TaxTypeCode>\n");
            xml.append("          <TaxRate>").append(dec(entry.getKey())).append("</TaxRate>\n");
            xml.append("          <TaxableBase><TotalAmount>").append(dec(entry.getValue()[0])).append("</TotalAmount></TaxableBase>\n");
            xml.append("          <TaxAmount><TotalAmount>").append(dec(entry.getValue()[1])).append("</TotalAmount></TaxAmount>\n");
            xml.append("        </Tax>\n");
        }
        xml.append("      </TaxesOutputs>\n");
        xml.append("      <InvoiceTotals>\n");
        xml.append("        <TotalGrossAmount>").append(dec(totalBase)).append("</TotalGrossAmount>\n");
        xml.append("        <TotalGrossAmountBeforeTaxes>").append(dec(totalBase)).append("</TotalGrossAmountBeforeTaxes>\n");
        xml.append("        <TotalTaxOutputs>").append(dec(totalCuota)).append("</TotalTaxOutputs>\n");
        xml.append("        <TotalTaxesWithheld>").append(dec(retencion)).append("</TotalTaxesWithheld>\n");
        xml.append("        <InvoiceTotal>").append(dec(total)).append("</InvoiceTotal>\n");
        xml.append("        <TotalOutstandingAmount>").append(dec(total)).append("</TotalOutstandingAmount>\n");
        xml.append("        <TotalExecutableAmount>").append(dec(total)).append("</TotalExecutableAmount>\n");
        xml.append("      </InvoiceTotals>\n");
        xml.append("      <Items>\n");
        for (FacturaLinea linea : factura.getFacturaLineas()) {
            if (linea.getCantidad() == null || linea.getPrecioUnitario() == null) {
                continue;
            }
            BigDecimal base = FinancialMath.subtotalConDescuento(linea.getCantidad(), linea.getPrecioUnitario(), linea.getDescuento());
            BigDecimal tipo = linea.getIva() != null ? linea.getIva() : BigDecimal.ZERO;
            xml.append("        <InvoiceLine>\n");
            xml.append("          <ItemDescription>").append(esc(linea.getDescripcion() != null ? linea.getDescripcion() : "Artículo")).append("</ItemDescription>\n");
            xml.append("          <Quantity>").append(dec(linea.getCantidad())).append("</Quantity>\n");
            xml.append("          <UnitPriceWithoutTax>").append(dec6(linea.getPrecioUnitario())).append("</UnitPriceWithoutTax>\n");
            xml.append("          <TotalCost>").append(dec(base)).append("</TotalCost>\n");
            xml.append("          <GrossAmount>").append(dec(base)).append("</GrossAmount>\n");
            xml.append("          <TaxesOutputs>\n");
            xml.append("            <Tax>\n");
            xml.append("              <TaxTypeCode>01</TaxTypeCode>\n");
            xml.append("              <TaxRate>").append(dec(tipo)).append("</TaxRate>\n");
            xml.append("              <TaxableBase><TotalAmount>").append(dec(base)).append("</TotalAmount></TaxableBase>\n");
            xml.append("              <TaxAmount><TotalAmount>").append(dec(FinancialMath.porcentaje(base, tipo))).append("</TotalAmount></TaxAmount>\n");
            xml.append("            </Tax>\n");
            xml.append("          </TaxesOutputs>\n");
            xml.append("        </InvoiceLine>\n");
        }
        xml.append("      </Items>\n");
        xml.append("    </Invoice>\n");
        xml.append("  </Invoices>\n");
        xml.append("</fe:Facturae>\n");
        return xml.toString();
    }

    private String parte(String elemento, String nif, String nombre, String direccion,
                         String cp, String poblacion, String provincia) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <").append(elemento).append(">\n");
        sb.append("      <TaxIdentification>\n");
        sb.append("        <PersonTypeCode>J</PersonTypeCode>\n");
        sb.append("        <ResidenceTypeCode>R</ResidenceTypeCode>\n");
        sb.append("        <TaxIdentificationNumber>").append(esc(nif)).append("</TaxIdentificationNumber>\n");
        sb.append("      </TaxIdentification>\n");
        sb.append("      <LegalEntity>\n");
        sb.append("        <CorporateName>").append(esc(nombre)).append("</CorporateName>\n");
        sb.append("        <AddressInSpain>\n");
        sb.append("          <Address>").append(esc(valor(direccion, "-"))).append("</Address>\n");
        sb.append("          <PostCode>").append(esc(valor(cp, "00000"))).append("</PostCode>\n");
        sb.append("          <Town>").append(esc(valor(poblacion, "-"))).append("</Town>\n");
        sb.append("          <Province>").append(esc(valor(provincia, "-"))).append("</Province>\n");
        sb.append("          <CountryCode>ESP</CountryCode>\n");
        sb.append("        </AddressInSpain>\n");
        sb.append("      </LegalEntity>\n");
        sb.append("    </").append(elemento).append(">\n");
        return sb.toString();
    }

    private Map<BigDecimal, BigDecimal[]> desglosePorTipo(Factura factura) {
        Map<BigDecimal, BigDecimal[]> porTipo = new TreeMap<>();
        for (FacturaLinea linea : factura.getFacturaLineas()) {
            if (linea.getCantidad() == null || linea.getPrecioUnitario() == null) {
                continue;
            }
            BigDecimal tipo = (linea.getIva() != null ? linea.getIva() : BigDecimal.ZERO)
                    .setScale(FinancialMath.SCALE, FinancialMath.ROUND);
            BigDecimal base = FinancialMath.subtotalConDescuento(linea.getCantidad(), linea.getPrecioUnitario(), linea.getDescuento());
            BigDecimal cuota = FinancialMath.porcentaje(base, tipo);
            porTipo.merge(tipo, new BigDecimal[]{base, cuota},
                    (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])});
        }
        return porTipo;
    }

    private static String numeroCompleto(Factura factura) {
        return factura.getSerie() != null && !factura.getSerie().isBlank()
                ? factura.getSerie() + factura.getNumero() : factura.getNumero();
    }

    private static String nifLimpio(String nif) {
        return nif != null ? nif.replaceAll("[\\s-]", "").toUpperCase() : "";
    }

    private static String valor(String value, String defecto) {
        return value != null && !value.isBlank() ? value : defecto;
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String dec(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).setScale(2, FinancialMath.ROUND).toPlainString();
    }

    private static String dec6(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).setScale(6, FinancialMath.ROUND).toPlainString();
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
