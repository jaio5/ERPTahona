package alicanteweb.erp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "verifactu")
@Getter
@Setter
public class VerifactuProperties {

    private AeatProperties aeat = new AeatProperties();
    private KeystoreProperties keystore = new KeystoreProperties();
    private KeyProperties key = new KeyProperties();
    private CertProperties cert = new CertProperties();

    // Legacy getters to avoid breaking existing code that expects flat names
    public String getKeystorePath() { return keystore != null ? keystore.getPath() : null; }
    public void setKeystorePath(String p) { if (this.keystore == null) this.keystore = new KeystoreProperties(); this.keystore.setPath(p); }

    public String getKeystorePassword() { return keystore != null ? keystore.getPassword() : null; }
    public void setKeystorePassword(String p) { if (this.keystore == null) this.keystore = new KeystoreProperties(); this.keystore.setPassword(p); }

    public String getKeyAlias() { return key != null ? key.getAlias() : null; }
    public void setKeyAlias(String a) { if (this.key == null) this.key = new KeyProperties(); this.key.setAlias(a); }

    public String getKeyPassword() { return key != null ? key.getPassword() : null; }
    public void setKeyPassword(String p) { if (this.key == null) this.key = new KeyProperties(); this.key.setPassword(p); }

    public String getCertPath() { return cert != null ? cert.getPath() : null; }
    public void setCertPath(String p) { if (this.cert == null) this.cert = new CertProperties(); this.cert.setPath(p); }

    public String getCertPassword() { return cert != null ? cert.getPassword() : null; }
    public void setCertPassword(String p) { if (this.cert == null) this.cert = new CertProperties(); this.cert.setPassword(p); }

    @Getter
    @Setter
    public static class AeatProperties {
        private boolean enabled;
        private String endpoint;
        private int timeout;
        private int maxRetries;
        private int retryDelay;
    }

    @Getter
    @Setter
    public static class KeystoreProperties {
        private String path;
        private String password;
    }

    @Getter
    @Setter
    public static class KeyProperties {
        private String alias;
        private String password;
    }

    @Getter
    @Setter
    public static class CertProperties {
        private String path;
        private String password;
    }
}
