package otvosuzlet.javitasnyilntarto.projections;

import java.time.LocalDateTime;
import java.util.Set;

public interface JobFullInfoProjection {
    Integer getId();

    String getDescription();

    Integer getFinalprice();

    String getObjectname();

    String getMaterial();

    Double getWeight();

    Integer getPricemin();

    Integer getPricemax();

    String getUploadnote();

    LocalDateTime getDone();

    String getFinishnote();

    LocalDateTime getPickup();

    Set<JobImageIdProjection> getBeforeImage();

    Set<JobImageIdProjection> getAfterImages();
}
