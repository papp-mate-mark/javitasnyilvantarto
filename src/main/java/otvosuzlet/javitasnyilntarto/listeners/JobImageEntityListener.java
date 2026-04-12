package otvosuzlet.javitasnyilntarto.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostRemove;
import otvosuzlet.javitasnyilntarto.model.JobImage;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JobImageEntityListener {
    private static final Logger logger = LoggerFactory.getLogger("fileLogger");

    @Value("${app.image.directory}")
    private String imageDirectory;

    @PostRemove
    public void deleteFiles(JobImage jobImage) {
        if (jobImage.getImageFilename() != null) {
            try {
                Files.deleteIfExists(Path.of(imageDirectory, jobImage.getImageFilename()));
            } catch (Exception e) {
                logger.error("Error occurred while deleting image file: {}. Trace: {}", jobImage.getImageFilename(), e.getMessage(), e);
            }
        }
        if (jobImage.getThumbnailFilename() != null) {
            try {
                Files.deleteIfExists(Path.of(imageDirectory, jobImage.getThumbnailFilename()));
            } catch (Exception e) {
                logger.error("Error occurred while deleting thumbnail file: {}. Trace: {}", jobImage.getThumbnailFilename(), e.getMessage(), e);
            }
        }
    }
}
