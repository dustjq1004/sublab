package me.kimyeonsup.home.config.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OAuth2PropertiesTest {

    @Autowired
    private OAuth2Properties oauth2Properties;

    @DisplayName("test 프로필의 OAuth2 리다이렉트 URI를 바인딩한다.")
    @Test
    void bindRedirectUri() {
        assertThat(oauth2Properties.getRedirectUri()).isEqualTo("http://localhost:5173");
    }

    @DisplayName("OAuth2 로그인 실패 시 React가 처리할 오류 파라미터를 추가한다.")
    @Test
    void createLoginFailureRedirectUri() {
        assertThat(oauth2Properties.getLoginFailureRedirectUri())
                .isEqualTo("http://localhost:5173?loginError=true");
    }
}
