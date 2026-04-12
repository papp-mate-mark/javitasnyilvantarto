package otvosuzlet.javitasnyilntarto.exceptions;

public class JobNotFoundException extends RuntimeExceptionWithCode {
    public JobNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
