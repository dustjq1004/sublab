package me.kimyeonsup.home.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.kimyeonsup.home.config.oauth.OAuth2SuccessHandler;
import me.kimyeonsup.home.util.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ApiOnlyWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("서버 렌더링 화면과 기존 정적 리소스 경로를 제공하지 않는다")
    @ParameterizedTest
    @ValueSource(strings = {
            "/", "/admin", "/blog", "/login", "/signup", "/login-error",
            "/sitemap.xml", "/robots.txt", "/log", "/css/common.css"
    })
    void doesNotServeLegacyWebResources(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isNotFound());
    }

    @DisplayName("React에서 재사용할 기존 정적 이미지는 제공한다")
    @Test
    void servesStaticImageAssets() throws Exception {
        mockMvc.perform(get("/img/favicon/favicon.ico"))
                .andExpect(status().isOk());
    }

    @DisplayName("Google OAuth2 로그인 시작 경로는 인증 서버로 리다이렉트한다")
    @Test
    void startsGoogleOAuth2Login() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .startsWith("https://accounts.google.com/o/oauth2/v2/auth"));
    }

    @DisplayName("API 로그아웃은 인증 쿠키를 만료시키고 본문 없이 성공한다")
    @Test
    void logoutExpiresAuthenticationCookies() throws Exception {
        mockMvc.perform(post("/api/logout").with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(result -> assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                        .anySatisfy(cookie -> assertThat(cookie)
                                .contains(CookieUtil.ACCESS_TOKEN_COOKIE_NAME + "=")
                                .contains("Max-Age=0"))
                        .anySatisfy(cookie -> assertThat(cookie)
                                .contains(OAuth2SuccessHandler.REFRESH_TOKEN_COOKIE_NAME + "=")
                                .contains("Max-Age=0")));
    }
}
