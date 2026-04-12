package otvosuzlet.javitasnyilntarto.projections;

import java.util.Set;

public interface PersonFullInfoProjection {
    Integer getId();
    String getName();
    String getPhone();
    String getAddress();
    Set<JobGroupFullInfoProjection> getJobGroups(); 

}
