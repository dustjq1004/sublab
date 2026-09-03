package me.kimyeonsup.home.global.api.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;

    @JsonIgnore
    private HttpStatus httpStatus;

    private String code;
    private String message;

    @Nullable
    private T data;

    @Nullable
    private List<ErrorDto> errors;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, HttpStatus.OK, "S200","SUCCESS", data, null);
    }

    public static ApiResponse<Void> fail(HttpStatus httpStatus, String code, String message, List<ErrorDto> errors) {
        return new ApiResponse<>(false, httpStatus, code, message, null, errors);
    }
}
