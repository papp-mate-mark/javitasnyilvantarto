package otvosuzlet.javitasnyilntarto.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchResponseWithItemCountDTO {
    private Long count;
    private List<JobSearchJobDataDTO> jobList;
}
