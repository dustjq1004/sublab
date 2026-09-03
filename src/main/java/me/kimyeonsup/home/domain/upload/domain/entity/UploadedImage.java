package me.kimyeonsup.home.domain.upload.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.upload.domain.vo.FileExtension;
import me.kimyeonsup.home.domain.upload.domain.vo.FileName;
import me.kimyeonsup.home.domain.upload.domain.vo.FileSize;
import me.kimyeonsup.home.domain.upload.domain.vo.FileType;
import me.kimyeonsup.home.domain.upload.domain.vo.Url;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;

@Entity
@Table(name = "uploaded_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadedImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Embedded
    private Url url;

    @Embedded
    private FileName fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ext", nullable = false)
    private FileExtension fileExtension;

    @Embedded
    private FileSize size;

    @Column(name = "status")
    private Integer status;

    @Column(name = "fail_message")
    private String failMessage;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "uploaded_dt")
    private LocalDateTime uploadedAt;

    @Builder
    public UploadedImage(Long id, String type, String url, String logicalName,
                         String ext, Long size, String userId) {
        this.id = id;
        this.type = type;
        this.url = new Url(url);
        this.fileName = new FileName(logicalName, null);
        this.fileExtension = FileExtension.from(fileName.extension(), FileType.IMAGE);
        this.size = new FileSize(size);
        this.userId = userId;
    }

    public String getLogicalName() {
        return this.fileName.getLogicalName();
    }

    public String getPhysicalName() {
        return this.fileName.getPhysicalName();
    }

    public String getFileExtension() {
        return this.fileExtension.getFileExtension();
    }

    public void uploadSuccess(String physicalName) {
        this.status = 1;
        this.uploadedAt = LocalDateTime.now();
        this.fileName = new FileName(this.fileName.getLogicalName(), physicalName);
    }

    public void uploadFail(String failMessage) {
        this.status = 0;
        this.failMessage = failMessage;
    }
}
