package me.kimyeonsup.home.global.exception.business;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends BusinessException {

    private final static String ERROR_CODE = "E404";
    private final static HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String message) {
        super(ERROR_CODE, HTTP_STATUS, message);
    }
}
