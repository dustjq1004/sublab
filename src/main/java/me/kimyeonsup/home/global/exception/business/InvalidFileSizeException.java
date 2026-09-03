package me.kimyeonsup.home.global.exception.business;

public class InvalidFileSizeException extends BusinessException {
    private final static String ERROR_CODE = "E400";
    private final static org.springframework.http.HttpStatus HTTP_STATUS = org.springframework.http.HttpStatus.BAD_REQUEST;

    public InvalidFileSizeException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
