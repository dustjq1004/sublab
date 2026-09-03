package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class CredentialsMismatchException extends BusinessException {

    private final static String ERROR_CODE = "E403";
    private final static HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN;

    public CredentialsMismatchException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }

}
