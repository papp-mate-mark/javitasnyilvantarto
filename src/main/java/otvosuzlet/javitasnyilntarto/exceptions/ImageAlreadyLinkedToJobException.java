package otvosuzlet.javitasnyilntarto.exceptions;

import org.springframework.http.HttpStatus;

public class ImageAlreadyLinkedToJobException extends RuntimeExceptionWithCode {
    public ImageAlreadyLinkedToJobException(Integer imageId) {
        super(
            "Image is already linked to a job (image id: " + imageId + "). Please reload the site and contact the maintainer.",
            "error.image.already.linked.to.job",
            HttpStatus.BAD_REQUEST
        );
    }
}
