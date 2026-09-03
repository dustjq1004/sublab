package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends BusinessException {

    private final static String ERROR_CODE = "E401";
    private final static HttpStatus HTTP_STATUS = HttpStatus.UNAUTHORIZED;

    public AuthenticationFailedException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
