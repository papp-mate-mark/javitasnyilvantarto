package otvosuzlet.javitasnyilntarto.exceptions;

public class SystemSettingNotFoundException extends RuntimeExceptionWithCode {
    public SystemSettingNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
