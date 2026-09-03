package me.kimyeonsup.home.global.exception.business;

public class InvalidTokenException extends BusinessException {
    private final static String ERROR_CODE = "E401";
    private final static org.springframework.http.HttpStatus HTTP_STATUS = org.springframework.http.HttpStatus.UNAUTHORIZED;

    public InvalidTokenException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
