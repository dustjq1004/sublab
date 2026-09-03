package me.kimyeonsup.home.domain.upload.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.upload.domain.entity.UploadedImage;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddUploadImageRequest {

    @NotBlank
    private String type;

    @NotBlank
    private String url;

    @NotBlank
    private String logicalName;

    @NotNull
    private Long size;

    public UploadedImage toEntity(String userId) {
        return UploadedImage.builder()
                .type(type)
                .url(url)
                .logicalName(logicalName)
                .size(size)
                .userId(userId)
                .build();
    }
}
