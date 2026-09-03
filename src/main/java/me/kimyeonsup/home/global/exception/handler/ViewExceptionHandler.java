package me.kimyeonsup.home.global.exception.handler;

import me.kimyeonsup.home.global.exception.business.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(annotations = Controller.class)
public class ViewExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex) {
        if (ex.getHttpStatus().value() == HttpStatus.NOT_FOUND.value()) {
            return "error/404";
        } else if (ex.getHttpStatus().value() == HttpStatus.FORBIDDEN.value()) {
            return "error/403";
        } else if (ex.getHttpStatus().value() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return "error/500";
        } else if (ex.getHttpStatus().value() == HttpStatus.BAD_GATEWAY.value()) {
            return "error/502";
        } else if (ex.getHttpStatus().value() == HttpStatus.GATEWAY_TIMEOUT.value()) {
            return "error/504";
        }
        return "error/general-error";
    }
}
