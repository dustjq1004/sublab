package me.kimyeonsup.home.domain.blog.admin.article.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.admin.article.domain.entity.AdminArticle;
import me.kimyeonsup.home.domain.blog.admin.article.service.AdminArticleService;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.login.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminArticleApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminArticleService adminArticleService;

    private final PrincipalDetail adminPrincipal = new PrincipalDetail(
            User.builder().email("admin@example.com").password("password").role("ADMIN").nickname("admin").build());

    @Nested
    @DisplayName("관리자 Article 조회 API")
    class FindArticles {

        @Test
        @DisplayName("관리자는 목록 조회에 성공한다")
        void adminCanFindArticles() throws Exception {
            AdminArticle article = createArticle(1L, "관리자 글", 10L, 1L);
            when(adminArticleService.findArticlesBy(any(PageRequest.class), any()))
                    .thenReturn(new PageImpl<>(List.of(article), PageRequest.of(0, 10), 1));

            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .queryParam("title", "관리자")
                            .with(user(adminPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalCount").value(1))
                    .andExpect(jsonPath("$.data.articles[0].id").value(1))
                    .andExpect(jsonPath("$.data.articles[0].title").value("관리자 글"))
                    .andExpect(jsonPath("$.data.articles[0].visibility").value("PUBLIC"));

            verify(adminArticleService).findArticlesBy(any(PageRequest.class), any());
        }

        @Test
        @DisplayName("다른 사용자는 목록 조회가 금지된다")
        void nonAdminCannotFindArticles() throws Exception {
            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .with(user("user"))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비로그인 사용자는 목록 조회가 금지된다")
        void anonymousCannotFindArticles() throws Exception {
            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("menuId로 조회한 게시글은 0개인 경우 totalCount는 0을 반환한다")
        void findArticlesByMenuIdReturnsZeroTotalCount() throws Exception {
            when(adminArticleService.findArticlesBy(any(PageRequest.class), any()))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .queryParam("menuId", "999")
                            .with(user(adminPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalCount").value(0))
                    .andExpect(jsonPath("$.data.articles").isEmpty());

            verify(adminArticleService).findArticlesBy(any(PageRequest.class), any());
        }
    }

    @Nested
    @DisplayName("관리자 Article 일괄 삭제 API")
    class DeleteArticles {

        @Test
        @DisplayName("관리자는 선택한 게시글을 일괄 삭제할 수 있다")
        void adminCanDeleteArticles() throws Exception {
            when(adminArticleService.deleteArticlesByIds(eq(List.of(1L, 2L, 3L)))).thenReturn(3);

            mockMvc.perform(delete("/api/admin/articles/batch")
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2,3]}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.deletedCount").value(3));

            verify(adminArticleService).deleteArticlesByIds(eq(List.of(1L, 2L, 3L)));
        }

        @Test
        @DisplayName("일반 사용자는 일괄 삭제가 금지된다")
        void nonAdminCannotDeleteArticles() throws Exception {
            mockMvc.perform(delete("/api/admin/articles/batch")
                            .with(user("user").authorities(() -> "USER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2]}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비로그인 사용자는 일괄 삭제가 금지된다")
        void anonymousCannotDeleteArticles() throws Exception {
            mockMvc.perform(delete("/api/admin/articles/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2]}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("빈 articleIds 요청은 400을 반환한다")
        void deleteArticlesWithEmptyIdsReturnsBadRequest() throws Exception {
            mockMvc.perform(delete("/api/admin/articles/batch")
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[]}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("관리자 Article 메뉴 일괄 변경 API")
    class UpdateArticlesMenu {

        @Test
        @DisplayName("관리자는 선택한 게시글의 메뉴를 일괄 변경할 수 있다")
        void adminCanUpdateArticlesMenu() throws Exception {
            when(adminArticleService.updateArticlesMenuByIds(eq(List.of(1L, 2L)), eq(10L))).thenReturn(2);

            mockMvc.perform(put("/api/admin/articles/batch/menu")
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"targetMenuId":10}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.updatedCount").value(2));
        }

        @Test
        @DisplayName("다른 사용자는 메뉴 일괄 변경이 금지된다")
        void nonAdminCannotUpdateArticlesMenu() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/menu")
                            .with(user("user"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"targetMenuId":10}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비로그인 사용자는 메뉴 일괄 변경이 금지된다")
        void anonymousCannotUpdateArticlesMenu() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/menu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"targetMenuId":10}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("빈 articleIds 요청은 400을 반환한다")
        void updateArticlesMenuWithEmptyIdsReturnsBadRequest() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/menu")
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[],"targetMenuId":10}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("관리자 Article visibility 일괄 변경 API")
    class UpdateArticlesVisibility {

        @Test
        @DisplayName("관리자는 선택한 게시글의 visibility를 일괄 변경할 수 있다")
        void adminCanUpdateArticlesVisibility() throws Exception {
            when(adminArticleService.updateArticlesVisibilityByIds(eq(List.of(1L, 2L)), eq("PUBLIC"))).thenReturn(2);

            mockMvc.perform(put("/api/admin/articles/batch/visibility")
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"visibility":"PUBLIC"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.updatedCount").value(2));
        }

        @Test
        @DisplayName("다른 사용자 글은 visibility 일괄 변경이 금지된다")
        void nonAdminCannotUpdateArticlesVisibility() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/visibility")
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(user("test"))
                            .content("""
                                    {"articleIds":[1,2],"visibility":"PUBLIC"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비로그인 사용자는 visibility 일괄 변경이 금지된다")
        void anonymousCannotUpdateArticlesVisibility() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/visibility")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"visibility":"PUBLIC"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("visibility 값이 비어 있으면 400을 반환한다")
        void updateArticlesVisibilityWithBlankValueReturnsBadRequest() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/visibility")
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"visibility":""}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    private AdminArticle createArticle(Long articleId, String title, Long menuId, Long categoryId) {
        Category category = Category.builder().name("category").build();
        Menu menu = Menu.builder().name("menu").category(category).build();
        ReflectionTestUtils.setField(category, "id", categoryId);
        ReflectionTestUtils.setField(menu, "id", menuId);

        AdminArticle article = AdminArticle.builder()
                .title(title)
                .content("content")
                .author("admin")
                .menu(menu)
                .build();
        ReflectionTestUtils.setField(article, "id", articleId);
        ReflectionTestUtils.setField(article, "visibility", Visibility.PUBLIC);
        return article;
    }
}
