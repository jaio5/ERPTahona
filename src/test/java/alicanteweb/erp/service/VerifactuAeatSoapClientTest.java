package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class VerifactuAeatSoapClientTest {

    @Test
    public void testFirmaWSSecurityPresent() throws Exception {
        VerifactuAeatSoapClient client = new VerifactuAeatSoapClient();

        // Crear keystore PKCS12 temporal con clave y certificado autofirmado
        String password = "changeit";
        String alias = "mi_certificado";
        Path tempKeystore = Files.createTempFile("test-keystore", ".p12");
        tempKeystore.toFile().deleteOnExit();
        createTestPkcs12(tempKeystore, password, alias);

        // Set required fields (keystore path temporal)
        setField(client, "keystorePath", tempKeystore.toAbsolutePath().toString());
        setField(client, "keystorePassword", password);
        setField(client, "keyAlias", alias);
        setField(client, "keyPassword", password);

        MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage msg = mf.createMessage();
        SOAPBody body = msg.getSOAPBody();
        // Crear elemento sin prefijo para evitar error de namespace
        SOAPElement el = body.addChildElement("Prueba");
        el.addTextNode("contenido prueba");
        msg.saveChanges();

        // Invoke private signing method via reflection
        Method m = VerifactuAeatSoapClient.class.getDeclaredMethod("firmarMensajeSOAP", SOAPMessage.class, byte[].class);
        m.setAccessible(true);
        m.invoke(client, msg, (Object) null);

        // Serialize message
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        msg.writeTo(out);
        byte[] xmlBytes = out.toByteArray();

        // Parse as DOM for XPath assertions
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xmlBytes));

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        Map<String,String> ns = new HashMap<>();
        ns.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        ns.put("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        ns.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        ns.put("ds", "http://www.w3.org/2000/09/xmldsig#");
        xp.setNamespaceContext(new SimpleNamespaceContext(ns));

        // 1) BinarySecurityToken wsu:Id
        XPathExpression bstExpr = xp.compile("string(//wsse:BinarySecurityToken/@wsu:Id)");
        String bstId = (String) bstExpr.evaluate(doc, XPathConstants.STRING);
        assertNotNull(bstId);
        assertFalse(bstId.isEmpty());

        // 2) Payload wsu:Id (primer hijo del Body)
        XPathExpression payloadExpr = xp.compile("string(//soapenv:Body/*/@wsu:Id)");
        String payloadId = (String) payloadExpr.evaluate(doc, XPathConstants.STRING);
        assertNotNull(payloadId);
        assertFalse(payloadId.isEmpty());

        // 3) Signature Reference points to payload Id
        String refUri = xp.compile("string(//ds:Signature//ds:Reference/@URI)").evaluate(doc);
        assertNotNull(refUri);
        assertTrue(refUri.startsWith("#id-payload-"), "Reference URI debe comenzar por '#id-payload-' pero fue: " + refUri);

        // 4) KeyInfo SecurityTokenReference -> Reference URI points to BST
        String keyRef = xp.compile("string(//ds:Signature//ds:KeyInfo//wsse:SecurityTokenReference/wsse:Reference/@URI)").evaluate(doc);
        assertNotNull(keyRef);
        assertEquals("#" + bstId, keyRef);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = VerifactuAeatSoapClient.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static class SimpleNamespaceContext implements javax.xml.namespace.NamespaceContext {
        private final Map<String, String> map;
        SimpleNamespaceContext(Map<String, String> map) { this.map = map; }
        @Override public String getNamespaceURI(String prefix) { return map.get(prefix); }
        @Override public String getPrefix(String namespaceURI) { for (Map.Entry<String,String> e: map.entrySet()) if (e.getValue().equals(namespaceURI)) return e.getKey(); return null; }
        @Override public Iterator<String> getPrefixes(String namespaceURI) { return java.util.Collections.emptyIterator(); }
    }

    // Genera un PKCS12 temporal con una clave RSA y certificado autofirmado
    private static void createTestPkcs12(Path path, String password, String alias) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        // Generar certificado autofirmado simple usando BouncyCastle helper
        X509Certificate cert = SelfSignedCertGenerator.generate("CN=Test", kp, 365);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(alias, (PrivateKey) kp.getPrivate(), password.toCharArray(), new Certificate[]{cert});

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            ks.store(fos, password.toCharArray());
        }
    }
}
