package otvosuzlet.javitasnyilntarto.exceptions;

import org.springframework.http.HttpStatus;

public class RequiredSystemSettingMissingException extends RuntimeExceptionWithCode {
    public RequiredSystemSettingMissingException() {
        super(
            "Required system setting is missing. Please reload the site and contact the maintainer.",
            "error.system.setting.required.missing",
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
