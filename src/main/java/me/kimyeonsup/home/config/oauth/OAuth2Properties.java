package me.kimyeonsup.home.config.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Setter
@Component
@ConfigurationProperties("oauth2")
public class OAuth2Properties {
    private String redirectUri;

    public String getLoginFailureRedirectUri() {
        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("loginError", true)
                .build()
                .toUriString();
    }
}
