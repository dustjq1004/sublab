package me.kimyeonsup.home.domain.article.adaptor.in.web.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.application.port.in.AdminArticleUseCase;
import me.kimyeonsup.home.domain.article.application.port.mapper.ArticleCommandMapper;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminArticleUseCase adminArticleService;

    private final PrincipalDetail adminPrincipal = new PrincipalDetail(
            User.builder().email("admin@example.com").password("password").role("ADMIN").nickname("admin").build());

    private final PrincipalDetail userPrincipal = new PrincipalDetail(
            User.builder().email("user@example.com").password("password").role("USER").nickname("user").build());

    @Nested
    @DisplayName("관리자 Article 조회 API")
    class FindArticles {

        @Test
        @DisplayName("첫 페이지 요청 번호 1은 query 인덱스 0, 응답 번호 1이 된다")
        void firstPageUsesZeroBasedQueryAndOneBasedResponse() throws Exception {
            ArticleResult first = articleResult(1L, "첫 페이지 글");
            when(adminArticleService.findArticlesBy(any()))
                    .thenReturn(new PageImpl<>(List.of(first), PageRequest.of(0, 10), 21));

            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .with(user(adminPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageNumber").value(1))
                    .andExpect(jsonPath("$.data.isFirst").value(true))
                    .andExpect(jsonPath("$.data.articles[0].id").value(1));

            ArgumentCaptor<ArticleSearchQuery> query = ArgumentCaptor.forClass(ArticleSearchQuery.class);
            verify(adminArticleService).findArticlesBy(query.capture());
            org.junit.jupiter.api.Assertions.assertEquals(0, query.getValue().pageNumber());
        }

        @Test
        @DisplayName("두 번째 페이지 요청 번호 2는 query 인덱스 1과 두 번째 페이지 콘텐츠를 반환한다")
        void secondPageUsesOneBasedResponseAndSecondPageContent() throws Exception {
            ArticleResult second = articleResult(11L, "두 번째 페이지 글");
            when(adminArticleService.findArticlesBy(any()))
                    .thenReturn(new PageImpl<>(List.of(second), PageRequest.of(1, 10), 21));

            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "2")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .with(user(adminPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageNumber").value(2))
                    .andExpect(jsonPath("$.data.articles[0].id").value(11))
                    .andExpect(jsonPath("$.data.articles[0].title").value("두 번째 페이지 글"));

            ArgumentCaptor<ArticleSearchQuery> query = ArgumentCaptor.forClass(ArticleSearchQuery.class);
            verify(adminArticleService).findArticlesBy(query.capture());
            org.junit.jupiter.api.Assertions.assertEquals(1, query.getValue().pageNumber());
        }

        @Test
        @DisplayName("마지막 페이지 요청은 1-based 번호와 마지막 페이지 상태를 반환한다")
        void lastPageReturnsOneBasedNumberAndIsLast() throws Exception {
            ArticleResult last = articleResult(21L, "마지막 페이지 글");
            when(adminArticleService.findArticlesBy(any()))
                    .thenReturn(new PageImpl<>(List.of(last), PageRequest.of(2, 10), 21));

            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "3")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .with(user(adminPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageNumber").value(3))
                    .andExpect(jsonPath("$.data.isLast").value(true))
                    .andExpect(jsonPath("$.data.isFirst").value(false))
                    .andExpect(jsonPath("$.data.articles[0].id").value(21));

            ArgumentCaptor<ArticleSearchQuery> query = ArgumentCaptor.forClass(ArticleSearchQuery.class);
            verify(adminArticleService).findArticlesBy(query.capture());
            org.junit.jupiter.api.Assertions.assertEquals(2, query.getValue().pageNumber());
        }

        @Test
        @DisplayName("menuId 필터를 사용한 두 번째 페이지도 인덱스 1과 필터 콘텐츠를 반환한다")
        void filteredSecondPagePreservesFilterAndPaginationContract() throws Exception {
            ArticleResult filteredOne = articleResult(31L, "필터 게시글 1");
            ArticleResult filteredTwo = articleResult(32L, "필터 게시글 2");
            when(adminArticleService.findArticlesBy(any()))
                    .thenReturn(new PageImpl<>(List.of(filteredOne, filteredTwo), PageRequest.of(1, 10), 12));

            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("menuId", "20")
                            .queryParam("pageNumber", "2")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .with(user(adminPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pageNumber").value(2))
                    .andExpect(jsonPath("$.data.totalCount").value(12))
                    .andExpect(jsonPath("$.data.numberOfElements").value(2))
                    .andExpect(jsonPath("$.data.isLast").value(true))
                    .andExpect(jsonPath("$.data.articles[0].id").value(31))
                    .andExpect(jsonPath("$.data.articles[1].id").value(32));

            ArgumentCaptor<ArticleSearchQuery> query = ArgumentCaptor.forClass(ArticleSearchQuery.class);
            verify(adminArticleService).findArticlesBy(query.capture());
            org.junit.jupiter.api.Assertions.assertEquals(1, query.getValue().pageNumber());
            org.junit.jupiter.api.Assertions.assertEquals(20L, query.getValue().menuId());
        }

        @Test
        @DisplayName("pageNumber 0은 400을 반환하고 조회 use case를 호출하지 않는다")
        void zeroPageNumberIsRejectedWithoutCallingUseCase() throws Exception {
            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "0")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .with(user(adminPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(adminArticleService, never()).findArticlesBy(any());
        }

        @Test
        @DisplayName("관리자는 목록 조회에 성공한다")
        void adminCanFindArticles() throws Exception {
            Article article = createArticle(1L, "관리자 글", 10L, 1L);
            when(adminArticleService.findArticlesBy(any()))
                    .thenReturn(new PageImpl<>(List.of(new ArticleCommandMapper().toArticleResult(article)), PageRequest.of(0, 10), 1));

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

            verify(adminArticleService).findArticlesBy(any());
        }

        @Test
        @DisplayName("다른 사용자는 목록 조회가 금지된다")
        void nonAdminCannotFindArticles() throws Exception {
            when(adminArticleService.findArticlesBy(any())).thenReturn(Page.empty());
            mockMvc.perform(get("/api/admin/articles")
                            .queryParam("pageNumber", "1")
                            .queryParam("pageSize", "10")
                            .queryParam("sortBy", "createdAt")
                            .queryParam("direction", "DESC")
                            .with(user(userPrincipal))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비로그인 사용자는 목록 조회가 금지된다")
        void anonymousCannotFindArticles() throws Exception {
            when(adminArticleService.findArticlesBy(any())).thenReturn(Page.empty());
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
            when(adminArticleService.findArticlesBy(any()))
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

            verify(adminArticleService).findArticlesBy(any());
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
                            .with(csrf())
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
                            .with(csrf())
                            .with(user(userPrincipal))
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
                            .with(csrf())
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
                            .with(csrf())
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
                            .with(csrf())
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
                            .with(csrf())
                            .with(user(userPrincipal))
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
                            .with(csrf())
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
                            .with(csrf())
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
            when(adminArticleService.updateArticlesVisibilityByIds(eq(List.of(1L, 2L)), eq("PRIVATE"), eq(adminPrincipal.getName()))).thenReturn(2);

            mockMvc.perform(put("/api/admin/articles/batch/visibility")
                            .with(csrf())
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"visibility":"PRIVATE"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.updatedCount").value(2));
        }

        @Test
        @DisplayName("다른 사용자 글은 visibility 일괄 변경이 금지된다")
        void nonAdminCannotUpdateArticlesVisibility() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/visibility")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(user(userPrincipal))
                            .content("""
                                    {"articleIds":[1,2],"visibility":"PUBLIC"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("비로그인 사용자는 visibility 일괄 변경이 금지된다")
        void anonymousCannotUpdateArticlesVisibility() throws Exception {
            mockMvc.perform(put("/api/admin/articles/batch/visibility")
                            .with(csrf())
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
                            .with(csrf())
                            .with(user(adminPrincipal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"articleIds":[1,2],"visibility":""}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    private Article createArticle(Long articleId, String title, Long menuId, Long categoryId) {
        Category category = Category.builder().name("category").build();
        Menu menu = Menu.builder().name("menu").category(category).build();
        ReflectionTestUtils.setField(category, "id", categoryId);
        ReflectionTestUtils.setField(menu, "id", menuId);

        Article article = Article.builder()
                .title(title)
                .content("content")
                .author("admin")
                .menu(menu)
                .visibility(Visibility.PUBLIC.name())
                .build();
        ReflectionTestUtils.setField(article, "id", articleId);
        return article;
    }

    private ArticleResult articleResult(Long id, String title) {
        return ArticleResult.builder()
                .id(id)
                .title(title)
                .content("테스트 본문")
                .author("admin")
                .menuId(20L)
                .menuName("menu")
                .visibility(Visibility.PUBLIC)
                .build();
    }
}
