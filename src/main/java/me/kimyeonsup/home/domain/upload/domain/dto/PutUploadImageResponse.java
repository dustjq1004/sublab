package me.kimyeonsup.home.domain.upload.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PutUploadImageResponse {

    private Integer code;
    private String message;
    private Long id;

    public static PutUploadImageResponse fromEntity(Integer code, String message, Long id) {
        return new PutUploadImageResponse(code, message, id);
    }
}
