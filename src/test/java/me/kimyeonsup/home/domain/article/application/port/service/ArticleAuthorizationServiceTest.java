package me.kimyeonsup.home.domain.article.application.port.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.application.port.service.ArticleService;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.application.port.mapper.ArticleCommandMapper;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ArticleAuthorizationServiceTest {
    @Mock ArticleQueryRepository articleQueryRepository;
    @Spy ArticleCommandMapper articleCommandMapper = new ArticleCommandMapper();
    @Spy AdminProperties adminProperties = new AdminProperties();
    @InjectMocks ArticleService articleService;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "user", "anonymous", "string", "unauthenticated"})
    @DisplayName("최신 글과 제목 검색은 설정된 관리자 이메일에만 비공개 글을 포함한다")
    void filtersLatestAndSearchByPrincipal(String kind) {
        adminProperties.setEmail("admin@example.com");
        PrincipalDetail principal = new PrincipalDetail(User.builder()
                .email(kind.equals("admin") ? "admin@example.com" : "user@example.com")
                .nickname(kind).role("ADMIN").build());
        if (!kind.equals("anonymous")) {
            SecurityContextHolder.getContext().setAuthentication(kind.equals("unauthenticated")
                    ? new UsernamePasswordAuthenticationToken(principal, "pwd")
                    : new UsernamePasswordAuthenticationToken(kind.equals("string") ? "admin" : principal, "pwd", List.of()));
        }
        boolean admin = kind.equals("admin");
        Article article = Article.builder().title("spring").content("body").author("admin")
                .menu(Menu.builder().name("java").build()).visibility(admin ? "PRIVATE" : "PUBLIC").build();
        if (admin) {
            when(articleQueryRepository.findTop8ByOrderByCreatedAtDesc()).thenReturn(List.of(article));
            when(articleQueryRepository.findByTitleContains("spring")).thenReturn(List.of(article));
        } else {
            when(articleQueryRepository.findTop8ByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC)).thenReturn(List.of(article));
            when(articleQueryRepository.findByTitleContainsAndVisibility("spring", Visibility.PUBLIC)).thenReturn(List.of(article));
        }
        assertThat(articleService.getLatestArticles()).singleElement()
                .satisfies(result -> assertThat(result.visibility()).isEqualTo(admin ? Visibility.PRIVATE : Visibility.PUBLIC));
        assertThat(articleService.getArticlesByTitle("spring")).singleElement()
                .satisfies(result -> assertThat(result.title()).isEqualTo("spring"));
        if (admin) {
            verify(articleQueryRepository).findTop8ByOrderByCreatedAtDesc();
            verify(articleQueryRepository).findByTitleContains("spring");
        } else {
            verify(articleQueryRepository).findTop8ByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC);
            verify(articleQueryRepository).findByTitleContainsAndVisibility("spring", Visibility.PUBLIC);
        }
        verifyNoMoreInteractions(articleQueryRepository);
    }
}
