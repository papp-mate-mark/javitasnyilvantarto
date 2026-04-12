package otvosuzlet.javitasnyilntarto.exceptions;

import org.springframework.http.HttpStatus;

public class ImageIdNotFoundForAttachException extends RuntimeExceptionWithCode {
    public ImageIdNotFoundForAttachException(Integer imageId) {
        super(
            "Image id not found: " + imageId + ". Please reload the site and contact the maintainer.",
            "error.image.id.not.found.for.attach",
            HttpStatus.BAD_REQUEST
        );
    }
}
