package otvosuzlet.javitasnyilntarto.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class FullJobGroupRequestDto {
    private Integer id;
    private List<FullJobRequestDto> jobs;
    private LocalDateTime deadline;
    private LocalDateTime bringedin;
}
