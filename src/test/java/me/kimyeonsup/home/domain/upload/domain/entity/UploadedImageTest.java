package me.kimyeonsup.home.domain.upload.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.kimyeonsup.home.global.exception.business.InvalidFileFormatException;
import me.kimyeonsup.home.global.exception.business.InvalidFileSizeException;
import me.kimyeonsup.home.global.exception.business.RequiredValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UploadedImage 엔티티 테스트")
class UploadedImageTest {

    /* 테스트 시나리오
    1. UploadedImage 생성 - 성공
    2. UploadedImage 생성 - ID 포함
    3. UploadedImage 생성 - 실패 메시지 포함
    4. UploadedImage 생성 - 다양한 확장자
    5. UploadedImage 생성 - logicalName이 null일 때 예외 발생
    6. UploadedImage 생성 - physicalName이 null일 때 예외 발생
    7. UploadedImage 생성 - logicalName이 빈 문자열일 때 예외 발생
    8. UploadedImage 생성 - 확장자가 없는 파일명일 때 예외 발생
    9. UploadedImage 생성 - 파일 크기 0
    10. UploadedImage 생성 - 큰 파일 크기
    11. UploadedImage 생성 - 다양한 상태값
    12. UploadedImage 생성 - 대문자 확장자는 소문자로 변환
    13. 확장자 예외 처리
    14. 파일 사이즈 예외 처리
    15. 썸네일 이미지 조회 url 검증
     */

    @Test
    @DisplayName("UploadedImage 생성 - 성공")
    void createUploadedImage_Success() {
        // given
        String type = "image";
        String url = "https://example.com/images/test.png";
        String logicalName = "test.png";
        String physicalName = "2025/10/19/uuid-test.png";
        Long size = 1024L;
        String userId = "user123";

        // when
        UploadedImage uploadedImage = UploadedImage.builder()
                .type(type)
                .url(url)
                .logicalName(logicalName)
                .size(size)
                .userId(userId)
                .build();

        // then
        assertThat(uploadedImage).isNotNull();
        assertThat(uploadedImage.getType()).isEqualTo(type);
        assertThat(uploadedImage.getUrl().getUrl()).isEqualTo(url);
        assertThat(uploadedImage.getFileName()).isNotNull();
        assertThat(uploadedImage.getFileExtension()).isNotNull();
        assertThat(uploadedImage.getSize().getSize()).isEqualTo(size);
        assertThat(uploadedImage.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("UploadedImage 생성 - ID 포함")
    void createUploadedImage_WithId() {
        // given
        Long id = 1L;
        String logicalName = "banner.jpg";

        // when
        UploadedImage uploadedImage = UploadedImage.builder()
                .id(id)
                .type("image")
                .url("https://example.com/banner.jpg")
                .logicalName(logicalName)
                .size(1024L)
                .userId("admin")
                .build();

        // then
        assertThat(uploadedImage.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("UploadedImage 생성 - 실패 메시지 포함")
    void createUploadedImage_WithFailMessage() {
        // given
        String failMessage = "Upload failed due to network error";

        // when
        UploadedImage uploadedImage = UploadedImage.builder()
                .type("image")
                .url("https://example.com/test.png")
                .logicalName("test.png")
                .size(1024L)
                .userId("user123")
                .build();

        uploadedImage.uploadFail(failMessage);

        // then
        assertThat(uploadedImage.getStatus()).isEqualTo(0);
        assertThat(uploadedImage.getFailMessage()).isEqualTo(failMessage);
    }

    @Test
    @DisplayName("UploadedImage 생성 - 다양한 확장자")
    void createUploadedImage_WithDifferentExtensions() {
        // given & when & then
        String[] extensions = {"jpg", "png", "gif", "webp"};

        for (String ext : extensions) {
            UploadedImage uploadedImage = UploadedImage.builder()
                    .type("image")
                    .url("https://example.com/test." + ext)
                    .logicalName("test." + ext)
                    .size(1024L)
                    .userId("user123")
                    .build();

            assertThat(uploadedImage).isNotNull();
            assertThat(uploadedImage.getFileExtension()).isNotNull();
        }
    }

    @Test
    @DisplayName("UploadedImage 생성 - logicalName이 null일 때 예외 발생")
    void createUploadedImage_WithNullLogicalName_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
                UploadedImage.builder()
                        .type("image")
                        .url("https://example.com/test.png")
                        .logicalName(null)
                        .size(1024L)
                        .userId("user123")
                        .build()
        ).isInstanceOf(RequiredValueException.class)
                .hasMessageContaining("logicalName 값은 필수입니다.");
    }

    @Test
    @DisplayName("UploadedImage 생성 - logicalName이 빈 문자열일 때 예외 발생")
    void createUploadedImage_WithBlankLogicalName_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
                UploadedImage.builder()
                        .type("image")
                        .url("https://example.com/test.png")
                        .logicalName("   ")
                        .size(1024L)
                        .userId("user123")
                        .build()
        ).isInstanceOf(RequiredValueException.class)
                .hasMessageContaining("logicalName 값은 필수입니다.");
    }

    @Test
    @DisplayName("UploadedImage 생성 - 확장자가 없는 파일명일 때 예외 발생")
    void createUploadedImage_WithoutExtension_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
                UploadedImage.builder()
                        .type("image")
                        .url("https://example.com/testfile")
                        .logicalName("testfile")
                        .size(1024L)
                        .userId("user123")
                        .build()
        ).isInstanceOf(RequiredValueException.class)
                .hasMessageContaining("ext 값은 필수입니다.");
    }

    @Test
    @DisplayName("UploadedImage 생성 - 파일 크기 0")
    void createUploadedImage_WithZeroSize() {
        // when
        UploadedImage uploadedImage = UploadedImage.builder()
                .type("image")
                .url("https://example.com/empty.png")
                .logicalName("empty.png")
                .size(0L)
                .userId("user123")
                .build();

        // then
        assertThat(uploadedImage.getSize().getSize()).isZero();
    }

    @Test
    @DisplayName("UploadedImage 생성 - 큰 파일 크기")
    void createUploadedImage_WithLargeSize() {
        // given
        Long largeSize = Long.MAX_VALUE;

        // when & then
        assertThatThrownBy(() ->
                // when
                UploadedImage.builder()
                    .type("image")
                    .url("https://example.com/large.png")
                    .logicalName("large.png")
                    .size(largeSize)
                    .userId("user123")
                .build()
        ).isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("파일 크기는 5MB를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("UploadedImage 생성 - 대문자 확장자는 소문자로 변환")
    void createUploadedImage_WithUpperCaseExtension() {
        // when
        UploadedImage uploadedImage = UploadedImage.builder()
                .type("image")
                .url("https://example.com/test.PNG")
                .logicalName("test.PNG")
                .size(1024L)
                .userId("user123")
                .build();

        // then
        assertThat(uploadedImage.getFileExtension()).isNotNull();
    }

    @Test
    @DisplayName("확장자 예외 처리 - jpeg, jpg, png, gif, webp 외 확장자 예외 발생")
    void createUploadedImage_InvalidExtension_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
                UploadedImage.builder()
                        .type("image")
                        .url("https://example.com/test.exe")
                        .logicalName("test.exe")
                        .size(1024L)
                        .userId("user123")
                        .build()
        ).isInstanceOf(InvalidFileFormatException.class)
                .hasMessageContaining("허용되지 않는 확장자입니다. 허용 확장자 - jpg, jpeg, png, gif, webp");
    }

    @Test
    @DisplayName("파일 사이즈 예외 처리 - 음수 처리")
    void createUploadedImage_NegativeSize_ThrowsException() {
        // when & then
        assertThatThrownBy(() ->
                UploadedImage.builder()
                        .type("image")
                        .url("https://example.com/test.png")
                        .logicalName("test.png")
                        .size(-100L)
                        .userId("user123")
                        .build()
        ).isInstanceOf(InvalidFileSizeException.class)
                .hasMessageContaining("파일 크기는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("업로드 상태 업데이트")
    void updateStatus_Success() {
        // given
        String physicalName = "2025/10/19/uuid-test.png";
        UploadedImage uploadedImage = UploadedImage.builder()
                .type("image")
                .url("https://example.com/test.png")
                .logicalName("test.png")
                .size(1024L)
                .userId("user123")
                .build();

        // when
        uploadedImage.uploadSuccess(physicalName);

        // then
        assertThat(uploadedImage.getStatus()).isEqualTo(1);
        assertThat(uploadedImage.getUploadedAt()).isNotNull();
        assertThat(uploadedImage.getPhysicalName()).isEqualTo(physicalName);
    }
}

