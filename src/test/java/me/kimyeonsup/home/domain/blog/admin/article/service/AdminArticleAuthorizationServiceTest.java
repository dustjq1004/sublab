package me.kimyeonsup.home.domain.blog.admin.article.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.global.exception.business.AuthenticationFailedException;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.UpdateAccessDeniedException;
import me.kimyeonsup.home.login.domain.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class AdminArticleAuthorizationServiceTest {

    private final AdminArticleAuthorizationService adminArticleAuthorizationService;

    AdminArticleAuthorizationServiceTest() {
        AdminProperties adminProperties = new AdminProperties();
        adminProperties.setEmail("admin@example.com");
        adminArticleAuthorizationService = new AdminArticleAuthorizationService(adminProperties);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("관리자 읽기 권한 검증")
    class ValidateAdminReadAccess {

        @Test
        @DisplayName("설정된 관리자 이메일의 PrincipalDetail 이면 통과한다")
        void passesWhenPrincipalIsPrincipalDetail() {
            setAuthentication(new PrincipalDetail(User.builder()
                    .email("admin@example.com")
                    .password("password")
                    .role("USER")
                    .nickname("admin")
                    .build()));

            assertThatCode(() -> adminArticleAuthorizationService.validateAdminReadAccess())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("설정되지 않은 이메일의 PrincipalDetail 이면 읽기 권한 예외를 던진다")
        void throwsWhenPrincipalEmailIsNotConfiguredAdmin() {
            setAuthentication(new PrincipalDetail(User.builder()
                    .email("user@example.com")
                    .password("password")
                    .role("ADMIN")
                    .nickname("user")
                    .build()));

            assertThatThrownBy(() -> adminArticleAuthorizationService.validateAdminReadAccess())
                    .isInstanceOf(ReadAccessDeniedException.class)
                    .hasMessage("관리자만 접근할 수 있습니다.");
        }

        @Test
        @DisplayName("principal 이 PrincipalDetail 이 아니면 읽기 권한 예외를 던진다")
        void throwsWhenPrincipalIsNotPrincipalDetail() {
            setAuthentication("admin");

            assertThatThrownBy(() -> adminArticleAuthorizationService.validateAdminReadAccess())
                    .isInstanceOf(ReadAccessDeniedException.class)
                    .hasMessage("관리자만 접근할 수 있습니다.");
        }

        @Test
        @DisplayName("인증 정보가 없으면 인증 예외를 던진다")
        void throwsWhenAuthenticationIsMissing() {
            SecurityContextHolder.clearContext();

            assertThatThrownBy(() -> adminArticleAuthorizationService.validateAdminReadAccess())
                    .isInstanceOf(AuthenticationFailedException.class)
                    .hasMessage("인증이 필요합니다.");
        }
    }

    @Nested
    @DisplayName("관리자 쓰기 권한 검증")
    class ValidateAdminWriteAccess {

        @Test
        @DisplayName("설정된 관리자 이메일의 PrincipalDetail 이면 통과한다")
        void passesWhenPrincipalIsPrincipalDetail() {
            setAuthentication(new PrincipalDetail(User.builder()
                    .email("admin@example.com")
                    .password("password")
                    .role("USER")
                    .nickname("admin")
                    .build()));

            assertThatCode(() -> adminArticleAuthorizationService.validateAdminWriteAccess())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("설정되지 않은 이메일의 PrincipalDetail 이면 쓰기 권한 예외를 던진다")
        void throwsWhenPrincipalEmailIsNotConfiguredAdmin() {
            setAuthentication(new PrincipalDetail(User.builder()
                    .email("user@example.com")
                    .password("password")
                    .role("ADMIN")
                    .nickname("user")
                    .build()));

            assertThatThrownBy(() -> adminArticleAuthorizationService.validateAdminWriteAccess())
                    .isInstanceOf(UpdateAccessDeniedException.class)
                    .hasMessage("관리자만 접근할 수 있습니다.");
        }

        @Test
        @DisplayName("principal 이 PrincipalDetail 이 아니면 쓰기 권한 예외를 던진다")
        void throwsWhenPrincipalIsNotPrincipalDetail() {
            setAuthentication("admin");

            assertThatThrownBy(() -> adminArticleAuthorizationService.validateAdminWriteAccess())
                    .isInstanceOf(UpdateAccessDeniedException.class)
                    .hasMessage("관리자만 접근할 수 있습니다.");
        }
    }

    private void setAuthentication(Object principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, "password",
                        AuthorityUtils.createAuthorityList("ROLE_USER")));
    }
}
