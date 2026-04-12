package otvosuzlet.javitasnyilntarto.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobCompleteDTO {
    @Min(message = "validation.non.negative", value = 0)
    private Integer price;
    private String note;
    private LocalDateTime date;
    private List<Integer> imagesAfter;
}
