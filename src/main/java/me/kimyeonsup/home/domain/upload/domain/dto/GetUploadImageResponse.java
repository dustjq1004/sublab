package me.kimyeonsup.home.domain.upload.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.upload.domain.entity.UploadedImage;

@Getter
@NoArgsConstructor
public class GetUploadImageResponse {

    private Long id;
    private String url;
    private String type;
    private String logicalName;
    private String physicalName;
    private String extension;
    private Long size;
    private String status;
    private String userId;
    private String failMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime uploadedAt;

    public static GetUploadImageResponse fromEntity(UploadedImage uploadedImage) {
        Integer status = uploadedImage.getStatus();
        String statusString = "업로드중";
        if (Objects.nonNull(status))
            statusString = status == 1 ? "성공" : "실패";

        return GetUploadImageResponse.builder()
                .id(uploadedImage.getId())
                .url(uploadedImage.getUrl().getUrl())
                .type(uploadedImage.getType())
                .logicalName(uploadedImage.getLogicalName())
                .physicalName(uploadedImage.getPhysicalName())
                .extension(uploadedImage.getFileExtension())
                .size(uploadedImage.getSize().getSize())
                .status(statusString)
                .userId(uploadedImage.getUserId())
                .failMessage(uploadedImage.getFailMessage())
                .createdAt(uploadedImage.getCreatedAt())
                .updatedAt(uploadedImage.getUpdatedAt())
                .uploadedAt(uploadedImage.getUploadedAt())
                .build();
    }

    @Builder
    public GetUploadImageResponse(Long id, String url, String type, String logicalName, String physicalName, String extension,
                                  Long size, String status, String userId, String failMessage, LocalDateTime createdAt, LocalDateTime updatedAt,
                                  LocalDateTime uploadedAt) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.logicalName = logicalName;
        this.physicalName = physicalName;
        this.extension = extension;
        this.size = size;
        this.status = status;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.failMessage = failMessage;
        this.uploadedAt = uploadedAt;
    }
}
