package otvosuzlet.javitasnyilntarto.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class RuntimeExceptionWithCode extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public RuntimeExceptionWithCode(String message, String errorCode) {
        this(message, errorCode, HttpStatus.NOT_FOUND);
    }

    public RuntimeExceptionWithCode(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

}