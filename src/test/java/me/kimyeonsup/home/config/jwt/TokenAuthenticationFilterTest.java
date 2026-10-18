package me.kimyeonsup.home.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.global.exception.business.UserNotFoundException;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.login.service.UserDetailService;
import me.kimyeonsup.home.util.CookieUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("유효한 Bearer 토큰이면 PrincipalDetail 인증을 SecurityContext에 저장한다")
    @Test
    void authenticatesWithPrincipalDetailForValidBearerToken() throws Exception {
        String token = "valid-token";
        String email = "admin@example.com";
        PrincipalDetail principal = new PrincipalDetail(User.builder()
                .email(email)
                .password("password")
                .role("ROLE_USER")
                .build());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validToken(token)).thenReturn(true);
        when(tokenProvider.getSubject(token)).thenReturn(email);
        when(userDetailService.loadUserByUsername(email)).thenReturn(principal);

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(principal);
        assertThat(authentication.isAuthenticated()).isTrue();
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("유효한 access-token 쿠키이면 PrincipalDetail 인증을 SecurityContext에 저장한다")
    @Test
    void authenticatesWithPrincipalDetailForValidCookieToken() throws Exception {
        String token = "valid-cookie-token";
        String email = "admin@example.com";
        PrincipalDetail principal = new PrincipalDetail(User.builder()
                .email(email)
                .password("password")
                .role("ROLE_USER")
                .build());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validToken(token)).thenReturn(true);
        when(tokenProvider.getSubject(token)).thenReturn(email);
        when(userDetailService.loadUserByUsername(email)).thenReturn(principal);

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(principal);
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("유효하지 않은 Bearer 토큰이면 인증을 생성하지 않는다")
    @Test
    void doesNotAuthenticateInvalidToken() throws Exception {
        String token = "invalid-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validToken(token)).thenReturn(false);

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).getSubject(token);
        verify(userDetailService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("유효한 토큰의 사용자가 없으면 인증 없이 다음 필터로 진행한다")
    @Test
    void continuesWithoutAuthenticationWhenUserDoesNotExist() throws Exception {
        String token = "valid-token";
        String email = "deleted-user@example.com";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validToken(token)).thenReturn(true);
        when(tokenProvider.getSubject(token)).thenReturn(email);
        when(userDetailService.loadUserByUsername(email))
                .thenThrow(new UserNotFoundException("사용자를 찾을 수 없습니다."));

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @DisplayName("Authorization 헤더가 없으면 토큰 검증 없이 다음 필터로 진행한다")
    @Test
    void skipsAuthenticationWithoutAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validToken(null);
        verify(filterChain).doFilter(request, response);
    }
}
