package me.kimyeonsup.home.domain.blog.article.controller;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.article.domain.dto.PresignedUrlResponse;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import me.kimyeonsup.home.infra.s3.service.S3PresignedService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3/image")
public class S3FileController {

    @Value("${cloud.s3.bucket}")
    private String bucketName;
    private final S3PresignedService s3PresignedService;

    @GetMapping
    public ApiResponse<PresignedUrlResponse> getSignedUrl(@RequestParam String fileName) {
        PresignedUrlResponse responseMap = s3PresignedService.getPresignedUrl(bucketName, fileName);
        return ApiResponse.success(responseMap);
    }
}
