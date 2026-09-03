package me.kimyeonsup.home.domain.blog.article.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.domain.blog.article.domain.dto.PresignedUrlResponse;
import me.kimyeonsup.home.infra.s3.service.S3PresignedService;
import me.kimyeonsup.home.login.domain.entity.User;
import me.kimyeonsup.home.login.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class S3FileControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private S3PresignedService s3PresignedService;

    @Autowired
    private UserRepository userRepository;

    User user;

    @Value("${cloud.s3.bucket}")
    private String bucketName;

    @BeforeEach
    void setSecurityContext() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("admin@example.com")
                .password("test")
                .role("관리자")
                .build());

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) () -> user.getRole());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user,
                user.getPassword(), authorities));
    }

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @DisplayName("getPreSigendUrl : S3에서 preSignedUrl을 가져온다.")
    @Test
//    @DisabledIfEnvironmentVariable(named = "spring.profiles.active", matches = "logging")
    @EnabledOnOs(OS.MAC)
    void getPreSigendUrlTest() {
        // given
        String fileName = "aaa.png";

        // when
        PresignedUrlResponse responseData = s3PresignedService.getPresignedUrl(bucketName, fileName);
        String url = responseData.getSignedUrl();

        // then
        assertThat(url).isNotBlank();
    }
}
