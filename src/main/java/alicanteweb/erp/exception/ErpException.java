package alicanteweb.erp.exception;

public class ErpException extends RuntimeException {

    public ErpException(String message) {
        super(message);
    }

    public ErpException(String message, Throwable cause) {
        super(message, cause);
    }
}
