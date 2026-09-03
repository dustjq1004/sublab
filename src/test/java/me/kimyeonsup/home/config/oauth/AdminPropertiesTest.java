package me.kimyeonsup.home.config.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminPropertiesTest {

    @Autowired
    private AdminProperties adminProperties;

    @DisplayName("test 프로필의 관리자 이메일을 예제 값으로 바인딩한다")
    @Test
    void bindAdminEmail() {
        assertThat(adminProperties.getEmail()).isEqualTo("admin@example.com");
    }
}
