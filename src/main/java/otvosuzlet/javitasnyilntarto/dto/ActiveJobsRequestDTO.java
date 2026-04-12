package otvosuzlet.javitasnyilntarto.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActiveJobsRequestDTO {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobGroup {
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class InProgressJob {
            private Integer id;
            private String description;
            private String objectname;
            private Integer pricemin;
            private Integer pricemax;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DoneJob {
            private Integer id;
            private String description;
            private String objectname;
            private Integer finalPrice;
            private LocalDateTime finishTime;
        }

        private Integer groupId;
        private Integer personId;
        private String personname;
        private LocalDateTime uploadDate;
        private LocalDateTime deadline;
        private Set<InProgressJob> inProgressJobs;
        private Set<DoneJob> doneJobs;
    }

    private Set<JobGroup> groups;
}
