package me.kimyeonsup.home.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// SecurityMockMvcRequestPostProcessors.csrf() mutates the shared CsrfFilter repository, so this test needs its own context.
@SpringBootTest(properties = "test.context.csrf-security=isolated")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CsrfSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("안전한 요청에서 브라우저가 읽을 수 있는 CSRF 쿠키를 발급한다")
    void issueCsrfCookie() throws Exception {
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("XSRF-TOKEN=")))
                .andExpect(result -> assertThat(result.getResponse()
                        .getCookie("XSRF-TOKEN")
                        .getAttribute("SameSite")).isEqualTo("Lax"));
    }

    @Test
    @DisplayName("CSRF 토큰이 없는 상태 변경 요청은 거부한다")
    void rejectPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("쿠키 없이 Bearer Token만 사용하는 요청은 CSRF 검증 대상에서 제외한다")
    void allowBearerOnlyRequestWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer api-client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증 쿠키가 있으면 Bearer 헤더가 있어도 CSRF 토큰을 검증한다")
    void rejectCookieRequestWithoutCsrfTokenEvenWithBearerHeader() throws Exception {
        mockMvc.perform(post("/api/token")
                        .cookie(new Cookie("access_token", "cookie-token"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer api-client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF 토큰이 있는 상태 변경 요청은 CSRF 검증을 통과한다")
    void allowPostWithCsrfToken() throws Exception {
        MvcResult csrfResponse = mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie csrfCookie = csrfResponse.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/token")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
