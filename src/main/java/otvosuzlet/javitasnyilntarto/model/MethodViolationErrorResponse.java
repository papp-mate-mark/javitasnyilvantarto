package otvosuzlet.javitasnyilntarto.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MethodViolationErrorResponse extends ErrorResponse {
    private List<String> violationErrors;
}
