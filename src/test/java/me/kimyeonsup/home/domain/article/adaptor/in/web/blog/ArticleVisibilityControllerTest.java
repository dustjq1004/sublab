package me.kimyeonsup.home.domain.article.adaptor.in.web.blog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import me.kimyeonsup.home.domain.article.application.port.in.ArticleQueryUseCase;
import me.kimyeonsup.home.domain.article.application.port.mapper.ArticleCommandMapper;
import me.kimyeonsup.home.domain.article.application.port.in.ArticleCommandUseCase;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
public class ArticleVisibilityControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean ArticleCommandUseCase articleService;
    @MockBean ArticleQueryUseCase articleReadService;

    private static final String AUTHOR_EMAIL = "author@test.com";
    private static final String OTHER_EMAIL = "other@test.com";
    private static final long ARTICLE_ID = 1L;

    Category category;
    Menu menu;

    @BeforeEach
    void setUp() {
        category = createCategory("cat1");
        menu = createMenu("menu1", category);
    }

    @Nested
    @DisplayName("상세 조회 - PUBLIC 글")
    class FindArticlePublic {

        @Test
        @DisplayName("given_public_article_when_anonymous_then_returns_200_and_article")
        void given_public_article_when_anonymous_then_returns_200() throws Exception {
            // 검증: PUBLIC 글은 비로그인 사용자도 조회 가능
            Article article = createArticle(ARTICLE_ID, Visibility.PUBLIC, AUTHOR_EMAIL, menu);
            when(articleReadService.findByIdCredentialCheck(eq(ARTICLE_ID))).thenReturn(new ArticleCommandMapper().toArticleResult(article));

            mockMvc.perform(get("/api/articles/{id}", ARTICLE_ID).with(anonymous()).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.visibility").value("PUBLIC"));
            verify(articleReadService).findByIdCredentialCheck(eq(ARTICLE_ID));
        }

        @Test
        @DisplayName("given_public_article_when_authenticated_then_returns_200_and_article")
        void given_public_article_when_authenticated_then_returns_200() throws Exception {
            // 검증: PUBLIC 글은 모두 조회 가능
            Article article = createArticle(ARTICLE_ID, Visibility.PUBLIC, AUTHOR_EMAIL, menu);
            when(articleReadService.findByIdCredentialCheck(eq(ARTICLE_ID))).thenReturn(new ArticleCommandMapper().toArticleResult(article));

            mockMvc.perform(get("/api/articles/{id}", ARTICLE_ID)
                            .with(user(createPrincipal(AUTHOR_EMAIL)))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.visibility").value("PUBLIC"));
        }
    }

    @Nested
    @DisplayName("상세 조회 - PRIVATE 글 (작성자 전용)")
    class FindArticlePrivate {

        @Test
        @DisplayName("given_private_article_when_author_then_returns_200_and_article")
        void given_private_article_when_author_then_returns_200() throws Exception {
            // 검증: PRIVATE 글은 작성자 본인만 상세 조회 가능
            Article article = createArticle(ARTICLE_ID, Visibility.PRIVATE, AUTHOR_EMAIL, menu);
            PrincipalDetail author = createPrincipal(AUTHOR_EMAIL);
            when(articleReadService.findByIdCredentialCheck(eq(ARTICLE_ID))).thenReturn(new ArticleCommandMapper().toArticleResult(article));

            mockMvc.perform(get("/api/articles/{id}", ARTICLE_ID)
                            .with(user(author))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.visibility").value("PRIVATE"));
        }

        @Test
        @DisplayName("given_private_article_when_other_user_then_returns_403_forbidden")
        void given_private_article_when_other_user_then_returns_403() throws Exception {
            // 검증: PRIVATE 글에 다른 로그인 사용자 접근 시 403 Forbidden (글 작성자만 조회 가능)
            PrincipalDetail otherUser = createPrincipal(OTHER_EMAIL);
            when(articleReadService.findByIdCredentialCheck(eq(ARTICLE_ID)))
                    .thenThrow(new ReadAccessDeniedException("작성자만 조회 가능합니다."));

            mockMvc.perform(get("/api/articles/{id}", ARTICLE_ID)
                            .with(user(otherUser))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("given_private_article_when_anonymous_then_returns_403_forbidden")
        void given_private_article_when_anonymous_then_returns_403() throws Exception {
            // 검증: PRIVATE 글에 비로그인 접근 시 403 반환
            when(articleReadService.findByIdCredentialCheck(eq(ARTICLE_ID)))
                    .thenThrow(new ReadAccessDeniedException("작성자만 조회 가능합니다."));

            mockMvc.perform(get("/api/articles/{id}", ARTICLE_ID).with(anonymous()).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("visibility 상태 전환")
    class UpdateVisibility {

        @Test
        @DisplayName("given_public_article_when_author_changes_to_private_then_returns_200")
        void given_public_when_change_to_private_then_success() throws Exception {
            // 검증: 작성자는 PUBLIC → PRIVATE 전환 가능, 즉시 비공개
            Article updated = createArticle(ARTICLE_ID, Visibility.PRIVATE, AUTHOR_EMAIL, menu);
            when(articleService.update(any())).thenReturn(new ArticleCommandMapper().toArticleResult(updated));

            String body = "{\"id\":1,\"title\":\"t\",\"subTitle\":\"\",\"content\":\"c\",\"menuId\":1,\"visibility\":\"PRIVATE\"}";
            mockMvc.perform(put("/api/articles/update")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(user(createPrincipal(AUTHOR_EMAIL))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.visibility").value("PRIVATE"));
        }

        @Test
        @DisplayName("given_private_article_when_other_user_updates_then_returns_404")
        void given_private_when_other_user_updates_then_404() throws Exception {
            // 작성자 조건에 맞는 글이 없으면 404를 반환한다.
            doThrow(new ResourceNotFoundException("글을 찾을 수 없습니다."))
                    .when(articleService).update(any());

            String body = "{\"id\":1,\"title\":\"t\",\"subTitle\":\"\",\"content\":\"c\",\"menuId\":1,\"visibility\":\"PUBLIC\"}";
            mockMvc.perform(put("/api/articles/update")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(user(createPrincipal(OTHER_EMAIL))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("given_invalid_request_when_update_then_returns_400")
        void given_invalid_request_when_update_then_400() throws Exception {
            String body = "{\"id\":1,\"title\":\"\",\"subTitle\":\"\",\"content\":\"c\",\"menuId\":null,\"visibility\":\"PUBLIC\"}";

            mockMvc.perform(put("/api/articles/update")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(user(createPrincipal(AUTHOR_EMAIL))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("삭제")
    class DeleteArticle {

        @Test
        @DisplayName("given_article_when_author_deletes_then_returns_200")
        void given_article_when_author_deletes_then_returns200() throws Exception {
            mockMvc.perform(delete("/api/articles/delete").param("id", "1")
                            .with(csrf())
                            .with(user(createPrincipal(AUTHOR_EMAIL))))
                    .andExpect(status().isOk());

            verify(articleService).delete(eq(ARTICLE_ID), any());
        }

        @Test
        @DisplayName("given_article_when_other_user_deletes_then_returns_404")
        void given_article_when_other_user_deletes_then_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("글을 찾을 수 없습니다."))
                    .when(articleService).delete(eq(ARTICLE_ID), any());

            mockMvc.perform(delete("/api/articles/delete").param("id", "1")
                            .with(csrf())
                            .with(user(createPrincipal(OTHER_EMAIL))))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("given_non_existent_article_when_delete_then_returns_404")
        void given_non_existent_article_when_delete_then_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("글을 찾을 수 없습니다."))
                    .when(articleService).delete(eq(ARTICLE_ID), any());

            mockMvc.perform(delete("/api/articles/delete").param("id", "1")
                            .with(csrf())
                            .with(user(createPrincipal(AUTHOR_EMAIL))))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("given_non_existent_article_when_find_then_returns_404")
    void given_non_existent_article_when_find_then_404() throws Exception {
        // 검증: 존재하지 않는 글 조회 시 404
        when(articleReadService.findByIdCredentialCheck(eq(999L)))
                .thenThrow(new ResourceNotFoundException("글을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/articles/{id}", 999L)
                        .with(user(createPrincipal(AUTHOR_EMAIL)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("최신 게시글 조회 API는 조회 유스케이스 결과를 반환한다")
    void getLatestArticles() throws Exception {
        when(articleReadService.getLatestArticles()).thenReturn(List.of());

        mockMvc.perform(get("/api/articles/latest").with(anonymous()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.articles").isArray())
                .andExpect(jsonPath("$.data.articles").isEmpty());

        verify(articleReadService).getLatestArticles();
    }

    private Article createArticle(long id, Visibility visibility, String authorEmail, Menu menu) {
        Article a = Article.builder()
                .title("title")
                .content("content")
                .menu(menu)
                .author(authorEmail.substring(0, authorEmail.indexOf("@")))
                .visibility(visibility.name())
                .build();
        // ReflectionTestUtils로 id 세팅 등
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }

    private Category createCategory(String name) {
        return Category.builder().name(name).build();
    }

    private Menu createMenu(String name, Category category) {
        return Menu.builder().name(name).category(category).build();
    }

    private PrincipalDetail createPrincipal(String email) {
        return new PrincipalDetail(User.builder().email(email).password("pwd").role("USER").build());
    }
}
