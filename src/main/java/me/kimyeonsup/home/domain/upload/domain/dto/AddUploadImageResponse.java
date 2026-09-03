package me.kimyeonsup.home.domain.upload.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AddUploadImageResponse {
    private Integer code;
    private String message;
    private Long id;

    public AddUploadImageResponse(Integer code, String message, Long id) {
        this.code = code;
        this.message = message;
        this.id = id;
    }
}
