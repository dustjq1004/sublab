package me.kimyeonsup.home.domain.upload.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PutUploadImageRequest {

    @NotNull
    private Long id;
    private String physicalName;
    private String failMessage;

    @NotNull
    private Boolean isSuccess;
}
