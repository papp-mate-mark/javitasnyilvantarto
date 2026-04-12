package otvosuzlet.javitasnyilntarto.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import otvosuzlet.javitasnyilntarto.enums.ImageType;
import otvosuzlet.javitasnyilntarto.exceptions.ImageAlreadyLinkedToJobException;
import otvosuzlet.javitasnyilntarto.exceptions.ImageIdNotFoundForAttachException;
import otvosuzlet.javitasnyilntarto.exceptions.JobImageNotFoundException;
import otvosuzlet.javitasnyilntarto.exceptions.UnsupportedFileExtensionError;
import otvosuzlet.javitasnyilntarto.model.Job;
import otvosuzlet.javitasnyilntarto.model.JobImage;
import otvosuzlet.javitasnyilntarto.repository.JobImageRepository;

@Service
public class JobImageServiceImpl implements JobImageService {
    private static final Logger logger = LoggerFactory.getLogger("fileLogger");

    @Value("${app.image.directory}")
    private String imageDirectory;

    @Autowired
    private JobImageRepository jobImageRepository;



    /** {@inheritDoc} */
    @Override
    @Transactional
    public JobImage uploadImage(MultipartFile file) throws IOException {
        File dir = new File(imageDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String extension;
        switch (file.getContentType()) {
            case "image/jpeg":
                extension = "jpg";
                break;
            case "image/png":
                extension = "png";
                break;
            case "image/gif":
                extension = "gif";
                break;
            default:
                throw new UnsupportedFileExtensionError("Unsupported file type.", "error.unsupported.filetype");
        }

        String filename;
        int limit = 10;
        do {
            filename = UUID.randomUUID().toString() + "." + extension;
            limit--;
        } while (new File(imageDirectory, filename).exists() && limit != 0);

        File targetFile = new File(imageDirectory, filename);
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(file.getBytes());

        }

        String thumbFilename = "thumb_" + filename;

        JobImage image = new JobImage();
        image.setImageFilename(filename);
        image.setType(null); // type will be set later when attaching to a job
        image.setCreateTime(java.time.LocalDateTime.now());
       
        try {
            Thumbnails.of(targetFile)
                .size(300, 300)
                .crop(Positions.CENTER)
                .toFile(new File(imageDirectory, thumbFilename));
            image.setThumbnailFilename(thumbFilename);            
        } catch (IOException e) {
            logger.error("Error generating thumbnail", e);
        }

        return jobImageRepository.save(image);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Set<JobImage> attachImagesByIds(Collection<Integer> ids, Job job, ImageType type) {
        Set<JobImage> attached = new HashSet<>();
        if (ids == null) {
            return attached;
        }

        for (Integer id : ids) {
            Optional<JobImage> imageOpt = jobImageRepository.findById(id);
            if (imageOpt.isEmpty()) {
                throw new ImageIdNotFoundForAttachException(id);
            }
            JobImage image = imageOpt.get();
            if (image.getJob() != null) {
                throw new ImageAlreadyLinkedToJobException(id);
            }
            image.setJob(job);
            image.setType(type);
            attached.add(image);
        }

        return attached;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] getThumbnailById(Integer id) {
        File file = new File(imageDirectory, getJobImage(id).getThumbnailFilename());
        if (!file.exists() || !file.canRead()) {
            throw new JobImageNotFoundException("Image record found, but file not found: " + file.getAbsolutePath());
        }
        byte[] imgBytes = null;
        try {
            imgBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new JobImageNotFoundException("Image found but couldn't be read at: " + file.getAbsolutePath());
        }
        return imgBytes;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] getFullImageById(Integer id) {
        File file = new File(imageDirectory, getJobImage(id).getImageFilename());
        if (!file.exists() || !file.canRead()) {
            throw new JobImageNotFoundException("Image record found, but file not found: " + file.getAbsolutePath());
        }
        byte[] imgBytes = null;
        try {
            imgBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new JobImageNotFoundException("Image found but couldn't be read at: " + file.getAbsolutePath());
        }
        return imgBytes;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public File getImageById(Integer id) {
        File file = new File(imageDirectory, getJobImage(id).getImageFilename());
        if (!file.exists() || !file.canRead()) {
            throw new JobImageNotFoundException("Image record found, but file not found: " + file.getAbsolutePath());
        }

        return file;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteImage(Integer id) {
        JobImage image = getJobImage(id);
        jobImageRepository.delete(image);
    }

    private JobImage getJobImage(Integer id) {
        return jobImageRepository.findById(id).orElseThrow(() -> new JobImageNotFoundException("Image not found"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public File getFullImageFile(Integer id) {
        return new File(imageDirectory, getJobImage(id).getImageFilename());
    }
}
