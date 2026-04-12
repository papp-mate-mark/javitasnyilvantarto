package otvosuzlet.javitasnyilntarto.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchJobDataDTO {
    private Integer personid;
    private Integer jobgroupid;
    private Integer jobid;
    private String personName;
    private String objectName;
    private String description;
    private LocalDateTime bringin;
    private LocalDateTime done;
    private LocalDateTime pickup;
}
