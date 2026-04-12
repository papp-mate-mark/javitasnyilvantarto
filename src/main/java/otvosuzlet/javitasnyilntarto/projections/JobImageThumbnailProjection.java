package otvosuzlet.javitasnyilntarto.projections;

public interface JobImageThumbnailProjection {
    Integer getId();
    String getThumbnailContentType();
    byte[] getThumbnail();
}
