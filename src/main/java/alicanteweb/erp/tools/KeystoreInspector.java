package alicanteweb.erp.tools;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;

public class KeystoreInspector {
    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "src/main/resources/certs/mi_certificado.p12";
        String pass = args.length > 1 ? args[1] : "changeit";
        System.out.println("Keystore path: " + path);
        System.out.println("Keystore password: " + (pass != null && !pass.isEmpty() ? "(provided)" : "(empty)"));

        System.out.println("Available security providers:");
        for (Provider p : Security.getProviders()) {
            System.out.println(" - " + p.getName() + " " + p.getVersionStr());
        }

        try (FileInputStream fis = new FileInputStream(path)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(fis, pass.toCharArray());
            System.out.println("Keystore loaded successfully. Type: " + ks.getType());
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String a = aliases.nextElement();
                System.out.println("Alias: " + a + ", isKeyEntry=" + ks.isKeyEntry(a) + ", isCertificateEntry=" + ks.isCertificateEntry(a));
            }
        } catch (Throwable t) {
            System.err.println("Error loading keystore: " + t);
            t.printStackTrace(System.err);
            Throwable cause = t.getCause();
            while (cause != null) {
                System.err.println("Caused by: " + cause);
                cause.printStackTrace(System.err);
                cause = cause.getCause();
            }
        }
    }
}
