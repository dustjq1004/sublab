package me.kimyeonsup.home.global.exception.handler;


import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import me.kimyeonsup.home.global.api.domain.dto.ErrorDto;
import me.kimyeonsup.home.global.exception.business.BusinessException;
import me.kimyeonsup.home.global.exception.business.DraftConflictException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErrorDto> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> ErrorDto.of(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiResponse<Void> body = ApiResponse.fail(HttpStatus.BAD_REQUEST, "E001", "Validation failed", errors);
        log.warn("Validation failed. fields={}",
                errors.stream().map(ErrorDto::getField).toList());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ApiResponse<Void> body = ApiResponse.fail(ex.getHttpStatus(), ex.getCode(), ex.getMessage(), null);
        log.warn("Business exception. code={}", ex.getCode());

        return new ResponseEntity<>(body, ex.getHttpStatus());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockException(ObjectOptimisticLockingFailureException ex) {
        DraftConflictException conflictException = new DraftConflictException(ex);
        ApiResponse<Void> body = ApiResponse.fail(conflictException.getHttpStatus(), conflictException.getCode(), conflictException.getMessage(), null);

        log.warn("Optimistic lock conflict");

        return new ResponseEntity<>(body, conflictException.getHttpStatus());
    }

    @ExceptionHandler({
            CannotAcquireLockException.class,
            DeadlockLoserDataAccessException.class,
            PessimisticLockingFailureException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleTransientLockException(Exception ex) {
        ApiResponse<Void> body = ApiResponse.fail(HttpStatus.SERVICE_UNAVAILABLE, "E503", "일시적인 DB 락 경합이 발생했습니다. 잠시 후 다시 시도해주세요.", null);

        log.error("Transient database lock conflict. type={}", ex.getClass().getSimpleName());

        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }

}
