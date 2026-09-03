package me.kimyeonsup.home.domain.upload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.kimyeonsup.home.domain.upload.domain.dto.AddUploadImageRequest;
import me.kimyeonsup.home.domain.upload.domain.dto.GetUploadImageResponse;
import me.kimyeonsup.home.domain.upload.domain.entity.UploadedImage;
import me.kimyeonsup.home.domain.upload.domain.vo.Url;
import me.kimyeonsup.home.domain.upload.repository.UploadRecordRepository;
import me.kimyeonsup.home.global.exception.business.InvalidFileSizeException;
import me.kimyeonsup.home.global.exception.business.InvalidUrlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UploadRecordServiceTest {

    @Value("${cloud.s3.bucket}")
    private String bucketName;

    @Autowired
    private UploadRecordService uploadRecordService;

    @Autowired
    private UploadRecordRepository uploadRecordRepository;

    private UploadedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = UploadedImage.builder()
                .type("image")
                .url("https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test-object-image.png")
                .logicalName("test-image.jpg")
                .size(1024L)
                .userId("test-user")
                .build();
    }

    @Test
    @DisplayName("이미지를 ID로 조회 - 성공")
    void findImageById_Success() {
        // given
        UploadedImage savedImage = uploadRecordRepository.save(testImage);

        // when
        GetUploadImageResponse foundImage = uploadRecordService.getUploadedImageData(savedImage.getId());

        // then
        assertThat(foundImage.getId()).isEqualTo(savedImage.getId());
        assertThat(foundImage.getLogicalName()).isEqualTo("test-image.jpg");
        assertThat(foundImage.getUserId()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("이미지 메타데이터 저장 - 성공")
    void saveImageMetaData_Success() {
        // given
        AddUploadImageRequest request = new AddUploadImageRequest(
                "image",
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test-object-image.png",
                "service-test.jpg",
                2048L
        );

        // when
        UploadedImage savedImage = uploadRecordService.saveImageMetaData(request, "service-user");

        // then
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getType()).isEqualTo("image");
        assertThat(savedImage.getLogicalName()).isEqualTo("service-test.jpg");
        assertThat(savedImage.getSize().getSize()).isEqualTo(2048);
        assertThat(savedImage.getUserId()).isEqualTo("service-user");
        assertThat(uploadRecordRepository.findById(savedImage.getId())).isPresent();
    }

    @Test
    @DisplayName("업로드 상태를 성공으로 변경")
    void updateImageStatus_Success() {
        // given
        UploadedImage savedImage = uploadRecordRepository.save(testImage);

        // when
        uploadRecordService.uploadSuccess(savedImage.getId(), "2025/01/19/test.png", null, true);

        // then
        GetUploadImageResponse updatedImage = uploadRecordService.getUploadedImageData(savedImage.getId());

        assertThat(updatedImage.getStatus()).isEqualTo("성공");
        assertThat(updatedImage.getUploadedAt()).isNotNull();
        assertThat(updatedImage.getPhysicalName()).isEqualTo("2025/01/19/test.png");
    }

    @Test
    @DisplayName("썸네일 이미지 조회 - url 실패")
    void getThumbnailUrl_Failure() {
        // given
        AddUploadImageRequest addUploadImageRequest = new AddUploadImageRequest(
                "image",
                "invalid-url",
                "test.png",
                1024L
        );
        String userId = "user123";

        // when & then
        assertThatThrownBy(() ->
                uploadRecordService.saveImageMetaData(addUploadImageRequest, userId)
        ).isInstanceOf(InvalidUrlException.class);
    }

    @Test
    @DisplayName("썸네일 이미지 조회 - url 검증")
    void getThumbnailUrl_Success() {
        // given
        AddUploadImageRequest addUploadImageRequest = new AddUploadImageRequest(
                "image",
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test-object-image.png",
                "test.png",
                1024L
        );

        // when
        UploadedImage uploadedImage = uploadRecordService.saveImageMetaData(addUploadImageRequest, "user123");
        Url thumbnailUrl = uploadedImage.getUrl();

        // then
        assertThat(thumbnailUrl).isNotNull();
        assertThat(thumbnailUrl.getUrl()).contains(bucketName);
    }

    @Test
    @DisplayName("UploadedImage 생성 - 5MB 이상 에러")
    void createUploadedImage_Over5MB_ThrowsException() {
        // given
        AddUploadImageRequest addUploadImageRequest = new AddUploadImageRequest(
                "image",
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/images/test-object-image.png",
                "test.png",
                5 * 1024L * 1024L + 1
        );

        // when & then
        assertThatThrownBy(() ->
                uploadRecordService.saveImageMetaData(addUploadImageRequest, "user123")
        ).isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("파일 크기는 5MB를 초과할 수 없습니다");
    }
}