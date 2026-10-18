package me.kimyeonsup.home.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class ArticleAdminAuthorizationManagerTest {

    @ParameterizedTest
    @ValueSource(strings = {"admin", "other", "string", "unauthenticated", "missing"})
    void requiresAuthenticatedPrincipalWithConfiguredEmail(String kind) {
        AdminProperties properties = new AdminProperties();
        properties.setEmail("admin@example.com");
        var manager = new ArticleAdminAuthorizationManager(properties);
        PrincipalDetail principal = new PrincipalDetail(User.builder()
                .email(kind.equals("other") ? "other@example.com" : "admin@example.com")
                .role(kind.equals("admin") ? "USER" : "ADMIN").build());
        Authentication authentication = switch (kind) {
            case "missing" -> null;
            case "unauthenticated" -> new UsernamePasswordAuthenticationToken(principal, "pwd");
            case "string" -> new UsernamePasswordAuthenticationToken("admin", "pwd", List.of());
            default -> new UsernamePasswordAuthenticationToken(principal, "pwd", List.of());
        };

        assertThat(manager.check(() -> authentication, null).isGranted()).isEqualTo(kind.equals("admin"));
    }
}
