package me.kimyeonsup.home.domain.upload.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.upload.domain.dto.AddUploadImageRequest;
import me.kimyeonsup.home.domain.upload.domain.entity.UploadedImage;
import me.kimyeonsup.home.domain.upload.service.UploadRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UploadRecordRepository 테스트")
class UploadRecordRepositoryTest {

    @Autowired
    private UploadRecordRepository uploadRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockBean
    private UploadRecordService uploadRecordService;

    private UploadedImage testImage;

    @BeforeEach
    void setUp() {
        testImage = UploadedImage.builder()
                .type("image")
                .url("https://example.com/test-image.jpg")
                .logicalName("test-image.jpg")
                .size(1024L)
                .userId("test-user")
                .build();
    }

    @Test
    @DisplayName("이미지 메타데이터 저장 - 성공")
    void saveImageMetadata_Success() {
        // when
        UploadedImage savedImage = uploadRecordRepository.save(testImage);

        // then
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getType()).isEqualTo("image");
        assertThat(savedImage.getUrl().getUrl()).isEqualTo("https://example.com/test-image.jpg");
        assertThat(savedImage.getLogicalName()).isEqualTo("test-image.jpg");
        assertThat(savedImage.getSize().getSize()).isEqualTo(1024L);
        assertThat(savedImage.getUserId()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("이미지 메타데이터 조회 - ID로 조회")
    void findById_Success() {
        // given
        UploadedImage savedImage = uploadRecordRepository.save(testImage);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<UploadedImage> foundImage = uploadRecordRepository.findById(savedImage.getId());

        // then
        assertThat(foundImage).isPresent();
        assertThat(foundImage.get().getId()).isEqualTo(savedImage.getId());
        assertThat(foundImage.get().getLogicalName()).isEqualTo("test-image.jpg");
    }

    @Test
    @DisplayName("이미지 메타데이터 조회 - 존재하지 않는 ID")
    void findById_NotFound() {
        // when
        Optional<UploadedImage> foundImage = uploadRecordRepository.findById(999L);

        // then
        assertThat(foundImage).isEmpty();
    }

    @Test
    @DisplayName("모든 이미지 메타데이터 조회")
    void findAll_Success() {
        // given
        UploadedImage image1 = UploadedImage.builder()
                .type("image")
                .url("https://example.com/image1.jpg")
                .logicalName("image1.jpg")
                .size(1024L)
                .userId("user1")
                .build();

        UploadedImage image2 = UploadedImage.builder()
                .type("image")
                .url("https://example.com/image2.jpg")
                .logicalName("image2.jpg")
                .size(2048L)
                .userId("user2")
                .build();

        uploadRecordRepository.save(image1);
        uploadRecordRepository.save(image2);
        entityManager.flush();

        // when
        List<UploadedImage> allImages = uploadRecordRepository.findAll();

        // then
        assertThat(allImages).hasSize(2);
        assertThat(allImages).extracting(UploadedImage::getLogicalName)
                .containsExactlyInAnyOrder("image1.jpg", "image2.jpg");
    }

    @Test
    @DisplayName("이미지 메타데이터 수정 - 성공")
    void updateImageMetadata_Success() {
        // given
        String physicalName = "20251019uuid-test.png";
        UploadedImage savedImage = uploadRecordRepository.save(testImage);
        entityManager.flush();

        // when
        savedImage.uploadSuccess(physicalName);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<UploadedImage> foundImage = uploadRecordRepository.findById(savedImage.getId());
        assertThat(foundImage).isPresent();
        assertThat(foundImage.get().getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미지 메타데이터 삭제")
    void deleteImageMetadata_Success() {
        // given
        UploadedImage savedImage = uploadRecordRepository.save(testImage);
        entityManager.flush();

        // when
        uploadRecordRepository.delete(savedImage);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<UploadedImage> foundImage = uploadRecordRepository.findById(savedImage.getId());
        assertThat(foundImage).isEmpty();
    }

    @Test
    @DisplayName("UploadRecordService를 통한 이미지 메타데이터 저장")
    void saveImageMetadataThroughService_Success() {
        // given
        AddUploadImageRequest request = new AddUploadImageRequest(
                "image",
                "https://example.com/service-test.jpg",
                "service-test.jpg",
                1024L
        );
        UploadedImage dummy = request.toEntity("service-user");
        given(uploadRecordService.saveImageMetaData(request, "service-user")).willReturn(dummy);

        // when
        UploadedImage uploadedImage = uploadRecordService.saveImageMetaData(request, "service-user");

        // then
        assertThat(uploadedImage).isNotNull();
        assertThat(uploadedImage.getType()).isEqualTo("image");
        assertThat(uploadedImage.getUrl().getUrl()).isEqualTo("https://example.com/service-test.jpg");
        assertThat(uploadedImage.getLogicalName()).isEqualTo("service-test.jpg");
        assertThat(uploadedImage.getSize().getSize()).isEqualTo(1024L);
        assertThat(uploadedImage.getUserId()).isEqualTo("service-user");
        assertThat(uploadedImage.getStatus()).isNull();
        assertThat(uploadedImage.getUploadedAt()).isNull();
    }

    @Test
    @DisplayName("여러 사용자의 이미지 메타데이터 저장 및 조회")
    void saveAndFindMultipleUsersImages_Success() {
        // given
        UploadedImage user1Image = UploadedImage.builder()
                .type("image")
                .url("https://example.com/user1-image.jpg")
                .logicalName("user1-image.jpg")
                .size(1024L)
                .userId("user1")
                .build();

        UploadedImage user2Image = UploadedImage.builder()
                .type("image")
                .url("https://example.com/user2-image.jpg")
                .logicalName("user2-image.jpg")
                .size(2048L)
                .userId("user2")
                .build();

        // when
        uploadRecordRepository.save(user1Image);
        uploadRecordRepository.save(user2Image);
        entityManager.flush();

        List<UploadedImage> allImages = uploadRecordRepository.findAll();

        // then
        assertThat(allImages).hasSize(2);
        assertThat(allImages).extracting(UploadedImage::getUserId)
                .containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("이미지 상태 변경 테스트 성공 - 1")
    void updateImageStatus_Success() {
        // given
        String physicalName = "20251019uuid-test.png";
        UploadedImage savedImage = uploadRecordRepository.save(testImage);
        entityManager.flush();

        // when - 상태를 성공(1)로 변경

        savedImage.uploadSuccess(physicalName);
        uploadRecordRepository.save(savedImage);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<UploadedImage> foundImage = uploadRecordRepository.findById(savedImage.getId());
        assertThat(foundImage).isPresent();
        assertThat(foundImage.get().getStatus()).isEqualTo(1);
        assertThat(foundImage.get().getUploadedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미지 상태 변경 테스트 실패 - 0")
    void updateImageStatus_Failure() {
        // given
        UploadedImage savedImage = uploadRecordRepository.save(testImage);
        entityManager.flush();

        // when - 상태를 실패(0)로 변경
        savedImage.uploadFail("Network error");
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<UploadedImage> foundImage = uploadRecordRepository.findById(savedImage.getId());
        assertThat(foundImage).isPresent();
        assertThat(foundImage.get().getStatus()).isEqualTo(0);
        assertThat(foundImage.get().getFailMessage()).isEqualTo("Network error");
    }
}