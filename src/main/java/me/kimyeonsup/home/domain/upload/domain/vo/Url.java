package me.kimyeonsup.home.domain.upload.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Url {

    @Column(name = "url", nullable = false, length = 400)
    private String url;

    protected Url() {}

    public Url(String url) {
        this.url = url;
    }
}
