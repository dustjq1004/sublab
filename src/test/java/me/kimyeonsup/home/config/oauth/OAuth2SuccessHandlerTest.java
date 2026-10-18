package me.kimyeonsup.home.config.oauth;

import java.util.Map;
import me.kimyeonsup.home.config.jwt.TokenProvider;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.login.repository.RefreshTokenRepository;
import me.kimyeonsup.home.domain.login.service.UserService;
import me.kimyeonsup.home.util.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2SuccessHandlerTest {

    @DisplayName("OAuth 로그인 성공 시 access token을 HttpOnly 쿠키로 저장한다")
    @Test
    void storesAccessTokenInHttpOnlyCookie() throws Exception {
        TokenProvider tokenProvider = mock(TokenProvider.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository =
                mock(OAuth2AuthorizationRequestBasedOnCookieRepository.class);
        UserService userService = mock(UserService.class);
        OAuth2Properties properties = new OAuth2Properties();
        properties.setRedirectUri("https://example.com/blog");
        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(tokenProvider, refreshTokenRepository,
                authorizationRequestRepository, userService, properties);
        User user = User.builder()
                .email("admin@example.com")
                .password("password")
                .role("ROLE_USER")
                .build();
        OAuth2User oauth2User = mock(OAuth2User.class);
        Authentication authentication = mock(Authentication.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSecure(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of("email", user.getEmail()));
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(tokenProvider.generateToken(user, OAuth2SuccessHandler.REFRESH_TOKEN_DURATION))
                .thenReturn("refresh-token");
        when(tokenProvider.generateToken(user, OAuth2SuccessHandler.ACCESS_TOKEN_DURATION))
                .thenReturn("access-token");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains(CookieUtil.ACCESS_TOKEN_COOKIE_NAME + "=access-token")
                        .contains("HttpOnly")
                        .contains("Secure")
                        .contains("SameSite=Lax"));
        assertThat(response.getRedirectedUrl()).isEqualTo("https://example.com/blog");
        assertThat(response.getRedirectedUrl()).doesNotContain("access-token", "token=");
    }
}
