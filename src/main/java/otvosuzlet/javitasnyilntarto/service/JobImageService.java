package otvosuzlet.javitasnyilntarto.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobImage;

@Service
public interface JobImageService {

    /**
     * Retrieves the thumbnail byte array for a given image ID.
     * 
     * @param id The ID of the image.
     * @return The bytes of the thumbnail.
     */
    byte[] getThumbnailById(Integer id);
    
    /**
     * Retrieves the full resolution image byte array for a given image ID.
     * 
     * @param id The ID of the image.
     * @return The bytes of the full image.
     */
    byte[] getFullImageById(Integer id);

    /**
     * Retrieves the physical file of the full image by its ID.
     * 
     * @param id The ID of the image.
     * @return The File object pointing to the full image.
     */
    File getFullImageFile(Integer id);

    /**
     * Retrieves the image file by its ID.
     * 
     * @param id The ID of the image.
     * @return The File object pointing to the image.
     */
    File getImageById(Integer id);

    /**
     * Uploads and saves a new image.
     * 
     * @param file The multipart file containing the image data.
     * @return The saved job image instance.
     * @throws IOException If an I/O error occurs during the upload.
     */
    JobImage uploadImage(MultipartFile file) throws IOException;

    /**
     * Attaches an existing set of images sequentially to a specific job.
     * 
     * @param ids The collection of image IDs to attach.
     * @param job The job the images belong to.
     * @param type The type evaluating whether they are before or after pictures.
     * @return A set of the attached images.
     */
    Set<JobImage> attachImagesByIds(Collection<Integer> ids, Job job, ImageType type);

    /**
     * Deletes an image by its ID.
     * 
     * @param id The ID of the image to be deleted.
     */
    void deleteImage(Integer id);
}
