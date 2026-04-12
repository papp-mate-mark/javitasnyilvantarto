package otvosuzlet.javitasnyilntarto.exceptions;

public class PersonNotFoundException extends RuntimeExceptionWithCode {
    public PersonNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
    