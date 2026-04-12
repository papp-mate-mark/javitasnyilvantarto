package otvosuzlet.javitasnyilntarto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import otvosuzlet.javitasnyilntarto.constrainsts.ValidJobGroupTiming;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidJobGroupTiming
public class JobGroupDto {
    private LocalDateTime bringin;

    @NotNull(message = "validation.required")
    private LocalDateTime deadline;
    private List<@Valid JobDto> jobs;
}
