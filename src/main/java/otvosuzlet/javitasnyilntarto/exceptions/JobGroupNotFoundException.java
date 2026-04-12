package otvosuzlet.javitasnyilntarto.exceptions;

public class JobGroupNotFoundException extends RuntimeExceptionWithCode {
    public JobGroupNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
