package me.kimyeonsup.home.domain.upload.domain.vo;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.global.exception.business.InvalidUrlException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UrlValidator {

    @Value("${cloud.s3.bucket}")
    private String bucketName;

    public void validateUrl(String urlString) {
        if (!urlString.contains(bucketName)) {
            throw new InvalidUrlException("%s 는 올바른 S3 URL이 아닙니다.".formatted(urlString));
        }
    }
}
