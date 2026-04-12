package otvosuzlet.javitasnyilntarto.exceptions;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public ValidationException(String field, String defaultMessage) {
        super("Validation failed");
        this.errors = new ArrayList<>();
        this.errors.add(new ValidationError(field, defaultMessage));
    }

    @Getter
    @AllArgsConstructor
    public static class ValidationError {
        private final String field;
        private final String defaultMessage;
    }
}
