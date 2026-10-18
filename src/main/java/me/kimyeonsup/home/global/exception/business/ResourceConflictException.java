package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends BusinessException {

    private static final String ERROR_CODE = "E409";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public ResourceConflictException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
