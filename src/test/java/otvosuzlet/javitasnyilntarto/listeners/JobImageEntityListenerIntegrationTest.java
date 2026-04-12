package otvosuzlet.javitasnyilntarto.listeners;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.repository.JobImageRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JobImageEntityListenerIntegrationTest {

    @Autowired
    private JobImageRepository jobImageRepository;

    @TempDir
    private static Path tempDirectory;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) throws IOException {
        // Override the property for the image directory to point to the temp dir
        registry.add("app.image.directory", () -> tempDirectory.toString());
    }

    @Test
    @Transactional
    void testListenerDeletesFilesWhenEntityIsDeleted() throws IOException {
        JobImage jobImage = new JobImage();
        
        Path imageFile = Files.createFile(tempDirectory.resolve("test-image-" + System.nanoTime() + ".jpg"));
        Path thumbnailFile = Files.createFile(tempDirectory.resolve("test-thumbnail-" + System.nanoTime() + ".jpg"));

        jobImage.setImageFilename(imageFile.getFileName().toString());
        jobImage.setThumbnailFilename(thumbnailFile.getFileName().toString());

        JobImage savedImage = jobImageRepository.save(jobImage);
        jobImageRepository.flush(); 

        assertTrue(Files.exists(imageFile));
        assertTrue(Files.exists(thumbnailFile));

        jobImageRepository.delete(savedImage);
        jobImageRepository.flush();

        assertFalse(Files.exists(imageFile));
        assertFalse(Files.exists(thumbnailFile));
    }

    @Test
    @Transactional
    void testIfFileDeletionThrowsIOException() throws IOException {
        JobImage jobImage = new JobImage();
        Path imageFile = Files.createFile(tempDirectory.resolve("test-image-" + System.nanoTime() + ".jpg"));
        jobImage.setImageFilename(imageFile.getFileName().toString());
        jobImage.setThumbnailFilename(tempDirectory.resolve("non-existent-thumbnail.jpg").getFileName().toString());

        JobImage savedImage = jobImageRepository.save(jobImage);
        jobImageRepository.flush();

        imageFile.toFile().setReadOnly();

        jobImageRepository.delete(savedImage);
        jobImageRepository.flush();

        // The files should still exist since deletion should have failed, but error shouldn't have been thrown
        assertTrue(Files.exists(imageFile));
    }
}
