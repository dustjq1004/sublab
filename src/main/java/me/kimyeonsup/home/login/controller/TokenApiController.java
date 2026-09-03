package me.kimyeonsup.home.login.controller;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import me.kimyeonsup.home.login.domain.dto.CreateAccessTokenRequest;
import me.kimyeonsup.home.login.domain.dto.CreateAccessTokenResponse;
import me.kimyeonsup.home.login.service.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenApiController {
    private final TokenService tokenService;

    @PostMapping("/api/token")
    public ApiResponse<CreateAccessTokenResponse> createNewAccessToken(
            @RequestBody CreateAccessTokenRequest request) {
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ApiResponse.success(new CreateAccessTokenResponse(newAccessToken));
    }
}
