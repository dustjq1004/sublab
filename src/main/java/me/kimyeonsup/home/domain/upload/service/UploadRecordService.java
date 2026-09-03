package me.kimyeonsup.home.domain.upload.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.upload.domain.dto.AddUploadImageRequest;
import me.kimyeonsup.home.domain.upload.domain.dto.GetUploadImageResponse;
import me.kimyeonsup.home.domain.upload.domain.dto.PutUploadImageResponse;
import me.kimyeonsup.home.domain.upload.domain.entity.UploadedImage;
import me.kimyeonsup.home.domain.upload.domain.vo.UrlValidator;
import me.kimyeonsup.home.domain.upload.repository.UploadRecordRepository;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.UploadExceptionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadRecordService {

    private final UrlValidator urlValidator;
    private final UploadRecordRepository uploadRecordRepository;
    private final MessageSourceResolver messageSourceResolver;

    public UploadedImage saveImageMetaData(AddUploadImageRequest addUploadImageRequest, String userId) {
        urlValidator.validateUrl(addUploadImageRequest.getUrl());
        UploadedImage uploadedImage = addUploadImageRequest.toEntity(userId);
        return uploadRecordRepository.save(uploadedImage);
    }

    public GetUploadImageResponse getUploadedImageData(Long id) {
        UploadedImage uploadedImage = uploadRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSourceResolver.resolve(UploadExceptionMessage.UPLOAD_IMAGE_NOT_FOUND, id)
                ));
        return GetUploadImageResponse.fromEntity(uploadedImage);
    }

    public List<GetUploadImageResponse> getUploadedImageData(String userId) {
        List<UploadedImage> uploadedImages = uploadRecordRepository.findByUserId(userId);
        return uploadedImages.stream()
                .map(GetUploadImageResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PutUploadImageResponse uploadSuccess(Long id, String physicalName, String failMessage, Boolean isSuccess) {
        UploadedImage uploadedImage = uploadRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSourceResolver.resolve(UploadExceptionMessage.UPLOAD_IMAGE_NOT_FOUND, id)
                ));

        if (isSuccess) {
            uploadedImage.uploadSuccess(physicalName);
        } else {
            uploadedImage.uploadFail(failMessage);
        }

        uploadRecordRepository.save(uploadedImage);

        return PutUploadImageResponse.fromEntity(HttpStatus.OK.value(), "updated upload image status", uploadedImage.getId());
    }
}
