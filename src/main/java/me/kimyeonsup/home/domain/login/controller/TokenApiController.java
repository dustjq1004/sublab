package me.kimyeonsup.home.domain.login.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.OAuth2SuccessHandler;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import me.kimyeonsup.home.domain.login.domain.dto.CreateAccessTokenRequest;
import me.kimyeonsup.home.domain.login.domain.dto.CreateAccessTokenResponse;
import me.kimyeonsup.home.domain.login.domain.dto.ValidateTokenResponse;
import me.kimyeonsup.home.domain.login.service.TokenService;
import me.kimyeonsup.home.util.CookieUtil;
import me.kimyeonsup.home.util.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenApiController {

    private final TokenService tokenService;

    @PostMapping("/api/token")
    public ApiResponse<CreateAccessTokenResponse> createNewAccessToken(
            @RequestBody(required = false) CreateAccessTokenRequest request,
            @CookieValue(value = OAuth2SuccessHandler.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie,
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie
                : request == null ? null : request.getRefreshToken();

        String newAccessToken = tokenService.createNewAccessToken(refreshToken);
        CookieUtil.addHttpOnlyCookie(httpServletRequest, httpServletResponse,
                CookieUtil.ACCESS_TOKEN_COOKIE_NAME, newAccessToken,
                (int) OAuth2SuccessHandler.ACCESS_TOKEN_DURATION.toSeconds());

        return ApiResponse.success(CreateAccessTokenResponse.builder()
                .accessToken(newAccessToken)
                .build());
    }

    @GetMapping("/api/token/validate")
    public ApiResponse<ValidateTokenResponse> validateToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @CookieValue(value = CookieUtil.ACCESS_TOKEN_COOKIE_NAME, required = false) String cookieToken) {
        String token = JwtUtils.extractBearerToken(authorizationHeader);
        if (token == null) {
            token = cookieToken;
        }

        return ApiResponse.success(ValidateTokenResponse.builder()
                .valid(tokenService.validateToken(token))
                .build());
    }
}
