package forum.hub.service.exception;

public class SHA256HashFailException extends RuntimeException{
    public SHA256HashFailException() {
        super();
    }

    public SHA256HashFailException(String message) {
        super(message);
    }

    public SHA256HashFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public SHA256HashFailException(Throwable cause) {
        super(cause);
    }

    protected SHA256HashFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
