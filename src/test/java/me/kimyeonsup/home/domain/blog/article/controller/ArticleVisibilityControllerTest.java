package me.kimyeonsup.home.domain.blog.article.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.domain.blog.article.service.ArticleService;
import me.kimyeonsup.home.global.exception.business.CredentialsMismatchException;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.business.UpdateAccessDeniedException;
import me.kimyeonsup.home.login.domain.entity.User;
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

    @MockBean ArticleService articleService;

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
            when(articleService.findByIdCredentialCheck(eq(ARTICLE_ID))).thenReturn(article);

            mockMvc.perform(get("/api/articles/{id}", ARTICLE_ID).with(anonymous()).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.visibility").value("PUBLIC"));
            verify(articleService).findByIdCredentialCheck(eq(ARTICLE_ID));
        }

        @Test
        @DisplayName("given_public_article_when_authenticated_then_returns_200_and_article")
        void given_public_article_when_authenticated_then_returns_200() throws Exception {
            // 검증: PUBLIC 글은 모두 조회 가능
            Article article = createArticle(ARTICLE_ID, Visibility.PUBLIC, AUTHOR_EMAIL, menu);
            when(articleService.findByIdCredentialCheck(eq(ARTICLE_ID))).thenReturn(article);

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
            when(articleService.findByIdCredentialCheck(eq(ARTICLE_ID))).thenReturn(article);

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
            when(articleService.findByIdCredentialCheck(eq(ARTICLE_ID)))
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
            when(articleService.findByIdCredentialCheck(eq(ARTICLE_ID)))
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
            when(articleService.update(eq(ARTICLE_ID), any())).thenReturn(updated);

            String body = "{\"title\":\"t\",\"subTitle\":\"\",\"content\":\"c\",\"menuId\":1,\"visibility\":\"PRIVATE\"}";
            mockMvc.perform(put("/api/articles/{id}", ARTICLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(user(createPrincipal(AUTHOR_EMAIL))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.visibility").value("PRIVATE"));
        }

        @Test
        @DisplayName("given_private_article_when_other_user_updates_then_returns_403")
        void given_private_when_other_user_updates_then_403() throws Exception {
            // 검증: 다른 사용자는 수정(visibility 포함) 불가
            doThrow(new UpdateAccessDeniedException("작성자만 수정 가능합니다."))
                    .when(articleService).update(eq(ARTICLE_ID), any());

            String body = "{\"title\":\"t\",\"subTitle\":\"\",\"content\":\"c\",\"menuId\":1,\"visibility\":\"PUBLIC\"}";
            mockMvc.perform(put("/api/articles/{id}", ARTICLE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .with(user(createPrincipal(OTHER_EMAIL))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("given_invalid_request_when_update_then_returns_400")
        void given_invalid_request_when_update_then_400() throws Exception {
            String body = "{\"title\":\"\",\"subTitle\":\"\",\"content\":\"c\",\"menuId\":null,\"visibility\":\"PUBLIC\"}";

            mockMvc.perform(put("/api/articles/{id}", ARTICLE_ID)
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
            mockMvc.perform(delete("/api/articles/{id}", ARTICLE_ID)
                            .with(user(createPrincipal(AUTHOR_EMAIL))))
                    .andExpect(status().isOk());

            verify(articleService).delete(eq(ARTICLE_ID));
        }

        @Test
        @DisplayName("given_article_when_other_user_deletes_then_returns_403")
        void given_article_when_other_user_deletes_then_returns403() throws Exception {
            doThrow(new CredentialsMismatchException("작성자만 삭제 가능합니다."))
                    .when(articleService).delete(eq(ARTICLE_ID));

            mockMvc.perform(delete("/api/articles/{id}", ARTICLE_ID)
                            .with(user(createPrincipal(OTHER_EMAIL))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("given_article_when_anonymous_deletes_then_returns_403")
        void given_article_when_anonymous_deletes_then_returns403() throws Exception {
            doThrow(new CredentialsMismatchException("작성자만 삭제 가능합니다."))
                    .when(articleService).delete(eq(ARTICLE_ID));

            mockMvc.perform(delete("/api/articles/{id}", ARTICLE_ID).with(user("test")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("given_non_existent_article_when_delete_then_returns_404")
        void given_non_existent_article_when_delete_then_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("글을 찾을 수 없습니다."))
                    .when(articleService).delete(eq(ARTICLE_ID));

            mockMvc.perform(delete("/api/articles/{id}", ARTICLE_ID)
                            .with(user(createPrincipal(AUTHOR_EMAIL))))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("given_non_existent_article_when_find_then_returns_404")
    void given_non_existent_article_when_find_then_404() throws Exception {
        // 검증: 존재하지 않는 글 조회 시 404
        when(articleService.findByIdCredentialCheck(eq(999L)))
                .thenThrow(new ResourceNotFoundException("글을 찾을 수 없습니다."));

        mockMvc.perform(get("/api/articles/{id}", 999L)
                        .with(user(createPrincipal(AUTHOR_EMAIL)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
