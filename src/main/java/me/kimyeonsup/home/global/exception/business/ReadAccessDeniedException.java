package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class ReadAccessDeniedException extends BusinessException {

    private final static String ERROR_CODE = "E403";
    private final static String DEFAULT_MESSAGE = "읽기 권한이 없습니다.";
    private final static HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN;

    public ReadAccessDeniedException() {
        super(ERROR_CODE, HTTP_STATUS, DEFAULT_MESSAGE);
    }

    public ReadAccessDeniedException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
