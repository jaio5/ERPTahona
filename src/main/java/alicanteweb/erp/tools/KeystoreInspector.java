package alicanteweb.erp.tools;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;

// BouncyCastle provider may be available on the classpath
// We'll try to use it if default loading fails
// Note: if BC is present but unregistered, we will register it programmatically

public class KeystoreInspector {
    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "src/main/resources/certs/mi_certificado.p12";
        String pass = args.length > 1 ? args[1] : "changeit";
        System.out.println("Keystore path: " + path);
        System.out.println("Keystore password: " + (pass != null && !pass.isEmpty() ? "(provided)" : "(empty)"));

        System.out.println("Available security providers before any change:");
        for (Provider p : Security.getProviders()) {
            System.out.println(" - " + p.getName() + " " + p.getVersionStr());
        }

        // First attempt: default provider
        try (FileInputStream fis = new FileInputStream(path)) {
            System.out.println("Trying default KeyStore.getInstance(\"PKCS12\")");
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(fis, pass.toCharArray());
            System.out.println("Keystore loaded successfully with default provider. Type: " + ks.getType());
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String a = aliases.nextElement();
                System.out.println("Alias: " + a + ", isKeyEntry=" + ks.isKeyEntry(a) + ", isCertificateEntry=" + ks.isCertificateEntry(a));
            }
            return;
        } catch (Throwable t) {
            System.err.println("Default provider failed: " + t);
            t.printStackTrace(System.err);
        }

        // Second attempt: try BouncyCastle if present
        try {
            System.out.println("Attempting to register BouncyCastle provider and load with provider 'BC'");
            try {
                // Try to load BC provider reflectively to avoid hard compile dependency if not present
                Class<?> bcClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                Provider bc = (Provider) bcClass.getDeclaredConstructor().newInstance();
                Security.insertProviderAt(bc, 1);
                System.out.println("BouncyCastle provider registered: " + bc.getName());
            } catch (ClassNotFoundException cnfe) {
                System.err.println("BouncyCastle not found on classpath: " + cnfe);
            }

            System.out.println("Available security providers after possible BC registration:");
            for (Provider p : Security.getProviders()) {
                System.out.println(" - " + p.getName() + " " + p.getVersionStr());
            }

            try (FileInputStream fis2 = new FileInputStream(path)) {
                KeyStore ks2 = KeyStore.getInstance("PKCS12", "BC");
                ks2.load(fis2, pass.toCharArray());
                System.out.println("Keystore loaded successfully with BouncyCastle provider. Type: " + ks2.getType());
                Enumeration<String> aliases2 = ks2.aliases();
                while (aliases2.hasMoreElements()) {
                    String a = aliases2.nextElement();
                    System.out.println("Alias: " + a + ", isKeyEntry=" + ks2.isKeyEntry(a) + ", isCertificateEntry=" + ks2.isCertificateEntry(a));
                }
                return;
            }
        } catch (Throwable t) {
            System.err.println("BouncyCastle attempt failed: " + t);
            t.printStackTrace(System.err);
            Throwable cause = t.getCause();
            while (cause != null) {
                System.err.println("Caused by: " + cause);
                cause.printStackTrace(System.err);
                cause = cause.getCause();
            }
        }

        System.err.println("Both attempts failed. If you have a custom PKCS#12 format, try converting it to a standard one using openssl or ensure BouncyCastle is on the classpath.");
    }
}
