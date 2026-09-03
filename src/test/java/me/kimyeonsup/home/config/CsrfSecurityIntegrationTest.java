package me.kimyeonsup.home.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CsrfSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("안전한 요청에서 브라우저가 읽을 수 있는 CSRF 쿠키를 발급한다")
    void issuesCsrfCookie() throws Exception {
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("XSRF-TOKEN=")));
    }

    @Test
    @DisplayName("CSRF 토큰이 없는 상태 변경 요청을 거부한다")
    void rejectsPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/md/render")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("유효한 CSRF 토큰이 있는 상태 변경 요청을 허용한다")
    void allowsPostWithCsrfToken() throws Exception {
        mockMvc.perform(post("/api/md/render")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("쿠키 없이 Bearer 토큰만 사용하는 API 요청은 CSRF 검증에서 제외한다")
    void allowsBearerOnlyRequestWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/md/render")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer api-client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("세션 쿠키가 있으면 Bearer 헤더가 있어도 CSRF 토큰을 검증한다")
    void rejectsCookieRequestWithoutCsrfTokenEvenWithBearerHeader() throws Exception {
        mockMvc.perform(post("/api/md/render")
                        .cookie(new Cookie("JSESSIONID", "browser-session"))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer api-client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isForbidden());
    }
}
