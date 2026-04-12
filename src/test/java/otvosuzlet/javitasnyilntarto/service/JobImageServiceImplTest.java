package otvosuzlet.javitasnyilntarto.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import otvosuzlet.javitasnyilntarto.exceptions.JobImageNotFoundException;
import otvosuzlet.javitasnyilntarto.exceptions.UnsupportedFileExtensionError;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.repository.JobImageRepository;
import otvosuzlet.javitasnyilntarto.testutil.TestObjectGenerator;

@ExtendWith(MockitoExtension.class)
public class JobImageServiceImplTest { 
    @InjectMocks
    private JobImageServiceImpl jobImageService;

    @Mock
    private JobImageRepository jobImageRepository;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(jobImageService, "imageDirectory", "job-images/");
    }

    @Test
    public void uploadImageShouldThrowUnsupportedFileExtensionErrorForUnsupportedType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());
        Assertions.assertThrows(UnsupportedFileExtensionError.class, () -> jobImageService.uploadImage(file));
        Mockito.verify(jobImageRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void uploadImageShouldWriteFullImageCreateThumbnailAndSaveMatchingFilenames() throws Exception {
        byte[] imageBytes = Files.readAllBytes(Path.of("src", "test", "resources", "static", "testimage.jpg"));
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", imageBytes);
        jobImageService.uploadImage(file);
        
        ArgumentCaptor<JobImage> requestCaptor = ArgumentCaptor.forClass(JobImage.class);
        Mockito.verify(jobImageRepository).save(requestCaptor.capture());

        Path savedImagePath = Path.of("job-images", requestCaptor.getValue().getImageFilename());
        Path savedThumbnailPath = Path.of("job-images", requestCaptor.getValue().getThumbnailFilename());

        assertTrue(Files.exists(savedImagePath));
        assertTrue(Files.exists(savedThumbnailPath));

        assertArrayEquals(imageBytes, Files.readAllBytes(savedImagePath));
        
        assertTrue(Files.size(savedImagePath) > Files.size(savedThumbnailPath));


    }

    @Test
    public void getThumbnailByIdShouldReturnThumbnailBytesWhenFileExists() throws Exception {
        String thumbnailFileName = "thumbnail-test.jpg";
        Path thumbnailPath = Path.of("job-images", thumbnailFileName);
        Files.createDirectories(thumbnailPath.getParent());

        byte[] thumbnailBytes = new byte[] {1, 2, 3, 4};
        Files.write(thumbnailPath, thumbnailBytes);

        JobImage image = TestObjectGenerator.createJobImage(null, null, null);
        image.setThumbnailFilename(thumbnailFileName);
        Mockito.when(jobImageRepository.findById(42)).thenReturn(Optional.of(image));

        byte[] result = jobImageService.getThumbnailById(42);

        assertArrayEquals(thumbnailBytes, result);
    }

    @Test
    public void getThumbnailByIdShouldThrowJobImageNotFoundExceptionWhenThumbnailFileDoesNotExist() {
        JobImage image = TestObjectGenerator.createJobImage(null, null, null);
        image.setThumbnailFilename("missing-thumbnail.jpg");
        Mockito.when(jobImageRepository.findById(43)).thenReturn(Optional.of(image));

        Assertions.assertThrows(JobImageNotFoundException.class, () -> jobImageService.getThumbnailById(43));
    }

    @Test
    public void getFullImageByIdShouldReturnImageBytesWhenFileExists() throws Exception {
        String imageFileName = "fullimage-test.jpg";
        Path imagePath = Path.of("job-images", imageFileName);
        Files.createDirectories(imagePath.getParent());

        byte[] imageBytes = new byte[] {5, 6, 7, 8};
        Files.write(imagePath, imageBytes);

        JobImage image = TestObjectGenerator.createJobImage(null, null, null);
        image.setImageFilename(imageFileName);
        Mockito.when(jobImageRepository.findById(44)).thenReturn(Optional.of(image));

        byte[] result = jobImageService.getFullImageById(44);

        assertArrayEquals(imageBytes, result);
    }

    @Test
    public void getFullImageByIdShouldThrowJobImageNotFoundExceptionWhenImageFileDoesNotExist() {
        JobImage image = TestObjectGenerator.createJobImage(null, null, null);
        image.setImageFilename("missing-full-image.jpg");
        Mockito.when(jobImageRepository.findById(45)).thenReturn(Optional.of(image));

        Assertions.assertThrows(JobImageNotFoundException.class, () -> jobImageService.getFullImageById(45));
    }

    @Test
    public void deleteImageTest() throws Exception {
        String uniquePart = UUID.randomUUID().toString();
        String fullImageFileName = "full-delete-" + uniquePart + ".jpg";
        String thumbnailFileName = "thumb-delete-" + uniquePart + ".jpg";

        Path fullImagePath = Path.of("job-images", fullImageFileName);
        Path thumbnailPath = Path.of("job-images", thumbnailFileName);
        Files.createDirectories(fullImagePath.getParent());
        Files.write(fullImagePath, new byte[] {9, 10, 11});
        Files.write(thumbnailPath, new byte[] {12, 13, 14});

        JobImage image = TestObjectGenerator.createJobImage(null, null, null);
        image.setImageFilename(fullImageFileName);
        image.setThumbnailFilename(thumbnailFileName);
        Mockito.when(jobImageRepository.findById(46)).thenReturn(Optional.of(image));

        jobImageService.deleteImage(46);
        Mockito.verify(jobImageRepository).delete(image);
    }
}

