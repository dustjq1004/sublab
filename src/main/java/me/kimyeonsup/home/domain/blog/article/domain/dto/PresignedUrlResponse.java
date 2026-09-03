package me.kimyeonsup.home.domain.blog.article.domain.dto;

import lombok.Getter;

@Getter
public class PresignedUrlResponse {
    private String signedUrl;
    private String fileName;

    private PresignedUrlResponse(String signedUrl, String fileName) {
        this.signedUrl = signedUrl;
        this.fileName = fileName;
    }

    public static PresignedUrlResponse of(String signedUrl, String fileName) {
        return new PresignedUrlResponse(signedUrl, fileName);
    }
}
