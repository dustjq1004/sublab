package me.kimyeonsup.home.domain.login.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateAccessTokenResponse {
    private String accessToken;
}
