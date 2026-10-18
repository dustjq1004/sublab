package me.kimyeonsup.home.config.jwt;

import jakarta.servlet.http.Cookie;
import java.time.Duration;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.login.repository.UserRepository;
import me.kimyeonsup.home.util.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("access-token 쿠키로 인증하면 AuthenticationPrincipal에 PrincipalDetail이 전달된다")
    @Test
    void resolvesPrincipalDetailFromAccessTokenCookie() throws Exception {
        User user = userRepository.save(User.builder()
                .email("admin@example.com")
                .password("password")
                .role("ROLE_USER")
                .nickname("admin")
                .build());
        String token = tokenProvider.generateToken(user, Duration.ofMinutes(10));

        mockMvc.perform(get("/api/upload/image")
                        .cookie(new Cookie(CookieUtil.ACCESS_TOKEN_COOKIE_NAME, token)))
                .andExpect(status().isOk());
    }
}
