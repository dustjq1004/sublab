package me.kimyeonsup.home.global.exception.business;

import org.springframework.http.HttpStatus;

public class RequiredValueException extends BusinessException {
    private final static String ERROR_CODE = "E400";
    private final static HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

    public RequiredValueException(String filedName) {
        super(ERROR_CODE, HTTP_STATUS, String.format(
                "%s 값은 필수입니다.", filedName
        ));
    }

    public RequiredValueException(String filedName, String message) {
        super(ERROR_CODE, HTTP_STATUS, String.format(
                message, filedName
        ));
    }

    public static RequiredValueException forField(String fieldName) {
        return new RequiredValueException(fieldName);
    }

    public static RequiredValueException forNull(String fieldName) {
        return new RequiredValueException(fieldName, "%s은(는) null일 수 없습니다");
    }

    public static RequiredValueException forEmpty(String fieldName) {
        return new RequiredValueException(fieldName, "%s은(는) 비어있을 수 없습니다");
    }

    public static RequiredValueException forBlank(String fieldName) {
        return new RequiredValueException(fieldName, "%s은(는) 공백일 수 없습니다");
    }
}
