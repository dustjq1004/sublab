package me.kimyeonsup.home.domain.upload.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.upload.domain.dto.AddUploadImageRequest;
import me.kimyeonsup.home.domain.upload.domain.dto.AddUploadImageResponse;
import me.kimyeonsup.home.domain.upload.domain.dto.GetUploadImageResponse;
import me.kimyeonsup.home.domain.upload.domain.dto.PutUploadImageRequest;
import me.kimyeonsup.home.domain.upload.domain.dto.PutUploadImageResponse;
import me.kimyeonsup.home.domain.upload.domain.entity.UploadedImage;
import me.kimyeonsup.home.domain.upload.service.UploadRecordService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UploadRecordController {

    private final UploadRecordService uploadRecordService;

    @PostMapping("/api/upload/image")
    public ApiResponse<AddUploadImageResponse> uploadImage(@Validated @RequestBody AddUploadImageRequest request,
                                                           @AuthenticationPrincipal PrincipalDetail user) {
        UploadedImage uploadedImage = uploadRecordService.saveImageMetaData(request, user.getName());
        return ApiResponse.success(new AddUploadImageResponse(HttpStatus.OK.value(), "saved uploaded image meta", uploadedImage.getId()));
    }

    @PutMapping("/api/upload/image")
    public ApiResponse<PutUploadImageResponse> updateUploadImageStatus(@Validated @RequestBody PutUploadImageRequest request) {
        PutUploadImageResponse putUploadImageResponse = uploadRecordService.uploadSuccess(request.getId(),
                request.getPhysicalName(), request.getFailMessage(), request.getIsSuccess());
        return ApiResponse.success(putUploadImageResponse);
    }

    @GetMapping("/api/upload/image")
    public ApiResponse<List<GetUploadImageResponse>> getUploadImageStatus(@AuthenticationPrincipal PrincipalDetail user) {
        List<GetUploadImageResponse> uploadedImageData = uploadRecordService.getUploadedImageData(user.getName());
        return ApiResponse.success(uploadedImageData);
    }
}
