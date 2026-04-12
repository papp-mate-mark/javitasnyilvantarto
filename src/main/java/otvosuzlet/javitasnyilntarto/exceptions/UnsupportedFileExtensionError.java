package otvosuzlet.javitasnyilntarto.exceptions;

public class UnsupportedFileExtensionError extends RuntimeExceptionWithCode {
    public UnsupportedFileExtensionError(String message, String errorCode) {
        super(message, errorCode);
    }
    
}
