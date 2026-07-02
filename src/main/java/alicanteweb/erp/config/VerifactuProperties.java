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
