package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class UnexpectedResultSizeException extends BusinessException {

    private static final String ERROR_CODE = "E400";
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

    public UnexpectedResultSizeException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
