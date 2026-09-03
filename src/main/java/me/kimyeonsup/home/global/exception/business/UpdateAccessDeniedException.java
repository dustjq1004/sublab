package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class UpdateAccessDeniedException extends BusinessException {

    private final static String ERROR_CODE = "E403";
    private final static String DEFAULT_MESSAGE = "수정 권한이 없습니다.";
    private final static HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN;

    public UpdateAccessDeniedException() {
        super(ERROR_CODE, HTTP_STATUS, DEFAULT_MESSAGE);
    }

    public UpdateAccessDeniedException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
