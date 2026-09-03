package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class DraftConflictException extends BusinessException {

    private static final String ERROR_CODE = "E409";
    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;
    private static final String DEFAULT_MESSAGE = "다른 저장이 먼저 반영되었습니다. 임시글을 다시 불러와 주세요.";

    public DraftConflictException() {
        super(ERROR_CODE, HTTP_STATUS, DEFAULT_MESSAGE);
    }

    public DraftConflictException(Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, DEFAULT_MESSAGE, cause);
    }
}
