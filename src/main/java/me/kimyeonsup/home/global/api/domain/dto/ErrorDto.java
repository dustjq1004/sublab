package me.kimyeonsup.home.global.api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorDto {
    private String field;
    private String message;

    public static ErrorDto of(String field, String message) {
        return new ErrorDto(field, message);
    }

    @Override
    public String toString() {
        return "ErrorDto{" +
                "field='" + field + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
