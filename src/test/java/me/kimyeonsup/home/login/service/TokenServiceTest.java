package me.kimyeonsup.home.login.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import me.kimyeonsup.home.config.jwt.TokenProvider;
import me.kimyeonsup.home.domain.login.service.RefreshTokenService;
import me.kimyeonsup.home.domain.login.service.TokenService;
import me.kimyeonsup.home.domain.login.service.UserService;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @Mock
    private MessageSourceResolver messageSourceResolver;

    @InjectMocks
    private TokenService tokenService;

    @DisplayName("validateToken: 유효한 토큰이면 true를 반환한다.")
    @Test
    void validateToken_validToken() {
        String token = "valid-token";
        when(tokenProvider.validToken(token)).thenReturn(true);

        boolean result = tokenService.validateToken(token);

        assertThat(result).isTrue();
    }

    @DisplayName("validateToken: 유효하지 않은 토큰이면 false를 반환한다.")
    @Test
    void validateToken_invalidToken() {
        String token = "invalid-token";
        when(tokenProvider.validToken(token)).thenReturn(false);

        boolean result = tokenService.validateToken(token);

        assertThat(result).isFalse();
    }

    @DisplayName("validateToken: 토큰이 null이면 TokenProvider를 호출하지 않고 false를 반환한다.")
    @Test
    void validateToken_nullToken() {
        boolean result = tokenService.validateToken(null);

        assertThat(result).isFalse();
        verifyNoInteractions(tokenProvider);
    }

    @DisplayName("validateToken: 토큰이 공백이면 TokenProvider를 호출하지 않고 false를 반환한다.")
    @Test
    void validateToken_blankToken() {
        boolean result = tokenService.validateToken("  ");

        assertThat(result).isFalse();
        verifyNoInteractions(tokenProvider);
    }
}
