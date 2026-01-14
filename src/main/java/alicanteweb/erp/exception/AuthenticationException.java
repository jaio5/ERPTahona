package alicanteweb.erp.exception;

/**
 * Excepcion para errores de autenticacion y autorizacion
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}

