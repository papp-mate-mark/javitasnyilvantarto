package otvosuzlet.javitasnyilntarto.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ErrorResponse {
    private ErrorTypes errorType;
    private String path;
    private int status;
    private LocalDateTime timestamp;
    private String message;
    private String errorKey;
    private String errorCode;
}
