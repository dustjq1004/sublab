package me.kimyeonsup.home.config.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.jwt.TokenProvider;
import me.kimyeonsup.home.login.domain.entity.RefreshToken;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.RefreshTokenRepository;
import me.kimyeonsup.home.login.service.UserService;
import me.kimyeonsup.home.util.CookieUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.WebUtils;

@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final String REDIRECT_PATH = "/";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository auth2AuthorizationRequestRepository;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        final User user = userService.findByEmail((String) oAuth2User.getAttributes().get("email"));
        /*
        final String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        final String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
         */
        final String targetUrl = getTargetUrl(request, response);

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 원래 요청 URL 가져오기
        Cookie originalUrlCookie = WebUtils.getCookie(request, "original_request_url");
        if (Objects.nonNull(originalUrlCookie)) {
            String originalUrl = originalUrlCookie.getValue();
            CookieUtil.deleteCookie(request, response, "original_request_url");
            if (isValidRedirectUri(originalUrl, request)) {
                return originalUrl;
            }
        }

        // 2. Referer 헤더 확인 (fallback)
        String referer = request.getHeader("referer");
        if (referer != null && isValidRedirectUri(referer, request)) {
            return referer;
        }

        // 3. 기본값
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                .build()
                .toUriString();
    }

    private boolean isValidRedirectUri(String redirectUri, HttpServletRequest request) {
        try {
            URI uri = new URI(redirectUri);
            String requestHost = request.getServerName();
            String redirectHost = uri.getHost();
            // 같은 도메인인지 확인
            return redirectHost != null && redirectHost.equals(requestHost);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response,
                                         String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        auth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);
    }
}
