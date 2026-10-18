package me.kimyeonsup.home.domain.article.adaptor.in.web.file.reponse;

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
