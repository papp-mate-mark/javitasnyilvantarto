package otvosuzlet.javitasnyilntarto.model;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {
    private List<ValidationFieldError> fieldErrors;
}
