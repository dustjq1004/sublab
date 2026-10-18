package me.kimyeonsup.home.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import me.kimyeonsup.home.config.jwt.JwtFactory;
import me.kimyeonsup.home.config.jwt.JwtProperties;
import me.kimyeonsup.home.config.oauth.OAuth2SuccessHandler;
import me.kimyeonsup.home.domain.login.domain.dto.CreateAccessTokenRequest;
import me.kimyeonsup.home.domain.login.domain.entity.RefreshToken;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.login.repository.RefreshTokenRepository;
import me.kimyeonsup.home.domain.login.repository.UserRepository;
import me.kimyeonsup.home.util.CookieUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@SpringBootTest
@AutoConfigureMockMvc
class TokenApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("createNewAccessToken: 새로운 액세스 토큰을 발급한다.")
    @Test
    public void createNewAccessToken() throws Exception {
        // given
        final String url = "/api/token";

        User testUser = userRepository.save(User.builder()
                .email("user@example.com")
                .password("test")
                .role("관리자")
                .build());

        String refreshToekn = JwtFactory.builder()
                .claims(Map.of("id", testUser.getId()))
                .build()
                .createToken(jwtProperties);

        refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToekn));

        CreateAccessTokenRequest request = new CreateAccessTokenRequest();
        request.setRefreshToken(refreshToekn);
        final String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        Matchers.allOf(
                                Matchers.containsString("access_token="),
                                Matchers.containsString("HttpOnly"),
                                Matchers.containsString("SameSite=Lax"))))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @DisplayName("createNewAccessToken: refresh-token 쿠키로 액세스 토큰을 재발급한다.")
    @Test
    void createNewAccessTokenFromCookie() throws Exception {
        User testUser = userRepository.save(User.builder()
                .email("cookie-user@example.com")
                .password("test")
                .role("관리자")
                .build());
        String refreshToken = JwtFactory.builder()
                .claims(Map.of("id", testUser.getId()))
                .build()
                .createToken(jwtProperties);
        refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToken));

        mockMvc.perform(post("/api/token")
                        .cookie(new Cookie(OAuth2SuccessHandler.REFRESH_TOKEN_COOKIE_NAME, refreshToken))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        Matchers.containsString("access_token=")))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @DisplayName("validateToken: 유효한 Bearer 토큰이면 true를 반환한다.")
    @Test
    void validateToken_validToken() throws Exception {
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);

        mockMvc.perform(get("/api/token/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true));
    }

    @DisplayName("validateToken: 유효한 access-token 쿠키이면 true를 반환한다.")
    @Test
    void validateToken_validCookieToken() throws Exception {
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);

        mockMvc.perform(get("/api/token/validate")
                        .cookie(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true));
    }

    @DisplayName("validateToken: 만료된 토큰이면 false를 반환한다.")
    @Test
    void validateToken_expiredToken() throws Exception {
        String token = JwtFactory.builder()
                .expriation(new Date(System.currentTimeMillis() - Duration.ofDays(1).toMillis()))
                .build()
                .createToken(jwtProperties);

        mockMvc.perform(get("/api/token/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));
    }

    @DisplayName("validateToken: 파싱할 수 없는 토큰이면 false를 반환한다.")
    @Test
    void validateToken_malformedToken() throws Exception {
        mockMvc.perform(get("/api/token/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));
    }

    @DisplayName("validateToken: 서명이 위조된 토큰이면 false를 반환한다.")
    @Test
    void validateToken_invalidSignature() throws Exception {
        JwtProperties invalidJwtProperties = new JwtProperties();
        invalidJwtProperties.setIssuer(jwtProperties.getIssuer());
        invalidJwtProperties.setSecretKey("different-secret-key");
        String token = JwtFactory.withDefaultValues().createToken(invalidJwtProperties);

        mockMvc.perform(get("/api/token/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));
    }

    @DisplayName("validateToken: Authorization 헤더가 없으면 false를 반환한다.")
    @Test
    void validateToken_missingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/token/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));
    }

    @DisplayName("validateToken: Bearer 형식이 아니면 false를 반환한다.")
    @Test
    void validateToken_invalidAuthorizationScheme() throws Exception {
        mockMvc.perform(get("/api/token/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Basic token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false));
    }

}
