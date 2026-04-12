package otvosuzlet.javitasnyilntarto.projections;

import java.time.LocalDateTime;
import java.util.Set;


public interface JobGroupFullInfoProjection {
    Integer getId();
    Set<JobFullInfoProjection> getJobs();
    LocalDateTime getDeadline();
    LocalDateTime getBringedin();
}
