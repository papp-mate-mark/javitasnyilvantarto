package otvosuzlet.javitasnyilntarto.model;

import lombok.Data;

@Data
public class ValidationFieldError {
    private String fieldName;
    private String messageKey;
}
