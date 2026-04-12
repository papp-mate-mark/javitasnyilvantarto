package otvosuzlet.javitasnyilntarto.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WSResponse<T> {
    private String reason;
    private WSAction action;
    private T payload;
    private String actor;
}
