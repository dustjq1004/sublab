package me.kimyeonsup.home.infra.s3.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.domain.article.adaptor.in.web.file.reponse.PresignedUrlResponse;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PresignedService {

    private final S3Presigner s3Presigner;

    public PresignedUrlResponse getPresignedUrl(String bucketName, String fileName) {
        final String keyName = "images/" + bucketName + "%s-%s".formatted(UUID.randomUUID().toString(), fileName);
        try {

            String[] splitFileName = fileName.split("\\.");
            String extension = splitFileName[splitFileName.length - 1].toLowerCase();

            final String contentType = "image/" + extension;
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            final String signedUrl = presignedRequest.url().toString();

            return PresignedUrlResponse.of(signedUrl, keyName);
        } catch (Exception e) {
            log.error("S3 Presigned URL 생성 실패: {}", e.getMessage());
            throw e;
        }
    }
}
