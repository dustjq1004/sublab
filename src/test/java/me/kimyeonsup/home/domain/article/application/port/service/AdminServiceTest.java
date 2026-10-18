package me.kimyeonsup.home.domain.article.application.port.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.application.port.mapper.ArticleCommandMapper;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleCommandRepository;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import me.kimyeonsup.home.domain.login.domain.entity.User;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.global.exception.business.RequiredValueException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.business.UnexpectedResultSizeException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.AdminExceptionMessage;
import me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ArticleQueryRepository adminArticleRepository;

    @Mock
    private ArticleCommandRepository articleCommandRepository;

    @Spy
    private ArticleCommandMapper articleCommandMapper = new ArticleCommandMapper();

    @Mock
    private MessageSourceResolver messageSourceResolver;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private AdminService adminArticleService;

    private final PrincipalDetail adminPrincipal = new PrincipalDetail(
            User.builder().email("admin@example.com").password("password").role("ADMIN").nickname("admin").build());

    private final PrincipalDetail userPrincipal = new PrincipalDetail(
            User.builder().email("user@example.com").password("password").role("USER").nickname("user").build());

    @Test
    @DisplayName("인식하지 못하는 공개 범위는 엔티티에 null로 반영한다")
    void unknownBatchVisibilityBecomesNull() {
        Article article = createArticle(1L, "title", createMenu(1L, 1L), Visibility.PUBLIC);
        when(adminArticleRepository.findByIdInAndAuthor(List.of(1L), "admin"))
                .thenReturn(List.of(article));

        int updatedCount = adminArticleService.updateArticlesVisibilityByIds(List.of(1L), "SECRET", "admin");

        assertThat(updatedCount).isEqualTo(1);
        assertThat(article.getVisibility()).isNull();
        verify(adminArticleRepository).findByIdInAndAuthor(List.of(1L), "admin");
    }

    @Test
    @DisplayName("인식하지 못하는 공개 범위는 조회 필터에서 제외한다")
    void unknownVisibilityFilterIsIgnored() {
        ArticleSearchQuery query = ArticleSearchQuery.builder().visibility("SECRET").build();
        Page<Article> articles = new PageImpl<>(List.of(
                createArticle(1L, "public", createMenu(1L, 1L), Visibility.PUBLIC),
                createArticle(2L, "private", createMenu(1L, 1L), Visibility.PRIVATE)));
        when(adminArticleRepository.findByTitleContaining(query)).thenReturn(articles);

        Page<ArticleResult> result = adminArticleService.findArticlesBy(query);

        assertThat(result.getContent()).extracting(ArticleResult::visibility)
                .containsExactly(Visibility.PUBLIC, Visibility.PRIVATE);
        verify(adminArticleRepository).findByTitleContaining(query);
        org.mockito.Mockito.verifyNoMoreInteractions(adminArticleRepository);
    }

    @Nested
    @DisplayName("관리자 Article 조회")
    class FindArticles {

        @Test
        @DisplayName("메뉴 조건이 있으면 해당 메뉴의 게시글만 조회한다")
        void findArticlesByMenu() {
            ArticleSearchQuery condition = ArticleSearchQuery.builder()
                    .menuId(3L)
                    .searchKeyword("spring")
                    .build();
            Page<Article> expected = new PageImpl<>(List.of(
                    createArticle(1L, "spring boot", createMenu(3L, 1L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByMenu_IdAndTitleContaining(condition))
                    .thenReturn(expected);

            Page<ArticleResult> result = adminArticleService.findArticlesBy(condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).menuName()).isEqualTo("menu-3");
            verify(adminArticleRepository).findByMenu_IdAndTitleContaining(condition);
        }

        @Test
        @DisplayName("카테고리 조건이 있으면 해당 카테고리의 게시글만 조회한다")
        void findArticlesByCategory() {
            ArticleSearchQuery condition = ArticleSearchQuery.builder()
                    .categoryId(5L)
                    .searchKeyword("java")
                    .build();
            Page<Article> expected = new PageImpl<>(List.of(
                    createArticle(2L, "java", createMenu(7L, 5L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByMenu_CategoryIdAndTitleContaining(condition))
                    .thenReturn(expected);

            Page<ArticleResult> result = adminArticleService.findArticlesBy(condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).menuName()).isEqualTo("menu-7");
            verify(adminArticleRepository).findByMenu_CategoryIdAndTitleContaining(condition);
        }

        @Test
        @DisplayName("조건이 없으면 제목 기준 전체 목록을 조회한다")
        void findArticlesWithoutFilter() {
            ArticleSearchQuery condition = ArticleSearchQuery.builder().build();
            Page<Article> expected = new PageImpl<>(List.of(
                    createArticle(3L, "title", createMenu(1L, 1L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByTitleContaining(condition))
                    .thenReturn(expected);

            Page<ArticleResult> result = adminArticleService.findArticlesBy(condition);

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(adminArticleRepository).findByTitleContaining(condition);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 페이지를 반환한다")
        void findArticlesReturnsEmptyPage() {
            ArticleSearchQuery condition = ArticleSearchQuery.builder()
                    .searchKeyword("missing")
                    .build();

            when(adminArticleRepository.findByTitleContaining(condition))
                    .thenReturn(Page.empty(PageRequest.of(0, 10)));

            Page<ArticleResult> result = adminArticleService.findArticlesBy(condition);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("메뉴와 visibility 조건이 있으면 두 조건을 모두 반영한다")
        void findArticlesByMenuAndVisibility() {
            ArticleSearchQuery condition = ArticleSearchQuery.builder()
                    .menuId(3L)
                    .searchKeyword("spring")
                    .visibility("PUBLIC")
                    .build();
            Page<Article> expected = new PageImpl<>(List.of(
                    createArticle(1L, "spring boot", createMenu(3L, 1L), Visibility.PUBLIC)));

            when(adminArticleRepository.findByMenu_IdAndTitleContainingAndVisibility(condition)).thenReturn(expected);

            Page<ArticleResult> result = adminArticleService.findArticlesBy(condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).visibility()).isEqualTo(Visibility.PUBLIC);
            verify(adminArticleRepository).findByMenu_IdAndTitleContainingAndVisibility(condition);
        }

        @Test
        @DisplayName("카테고리와 visibility 조건이 있으면 두 조건을 모두 반영한다")
        void findArticlesByCategoryAndVisibility() {
            ArticleSearchQuery condition = ArticleSearchQuery.builder()
                    .categoryId(5L)
                    .searchKeyword("java")
                    .visibility("PRIVATE")
                    .build();
            Page<Article> expected = new PageImpl<>(List.of(
                    createArticle(2L, "java", createMenu(7L, 5L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByMenu_CategoryIdAndVisibilityAndTitleContaining(condition)).thenReturn(expected);

            Page<ArticleResult> result = adminArticleService.findArticlesBy(condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).visibility()).isEqualTo(Visibility.PRIVATE);
            verify(adminArticleRepository).findByMenu_CategoryIdAndVisibilityAndTitleContaining(condition);
        }

        @Test
        @DisplayName("visibility 조건만 있으면 visibility 기준으로 조회한다")
        void findArticlesByVisibilityOnly() {
            ArticleSearchQuery condition = ArticleSearchQuery.builder()
                    .searchKeyword("guide")
                    .visibility("PUBLIC")
                    .build();
            Page<Article> expected = new PageImpl<>(List.of(
                    createArticle(3L, "guide", createMenu(1L, 1L), Visibility.PUBLIC)));

            when(adminArticleRepository.findByTitleContainsAndVisibility(condition))
                    .thenReturn(expected);

            Page<ArticleResult> result = adminArticleService.findArticlesBy(condition);

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(adminArticleRepository).findByTitleContainsAndVisibility(condition);
        }
    }

    @Nested
    @DisplayName("관리자 Article 일괄 삭제")
    class DeleteArticles {

        @Test
        @DisplayName("선택한 게시글이 모두 존재하면 일괄 삭제한다")
        void deleteArticlesByIds() {
            List<Long> articleIds = List.of(1L, 2L, 3L);
            List<Article> articles = articleIds.stream()
                    .map(id -> createArticle(id, "title" + id, createMenu(id, id), Visibility.PRIVATE))
                    .toList();

            when(adminArticleRepository.findAllById(articleIds)).thenReturn(articles);
            when(articleCommandRepository.deleteByIds(articleIds)).thenReturn(3);

            int deletedCount = adminArticleService.deleteArticlesByIds(articleIds);

            assertThat(deletedCount).isEqualTo(3);
            verify(articleCommandRepository).deleteByIds(articleIds);
        }

        @Test
        @DisplayName("빈 요청이면 예외를 던지고 삭제를 시도하지 않는다")
        void deleteArticlesByIdsWithEmptyIds() {
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_DELETE))
                    .thenReturn("삭제할 게시글을 다시 확인해주세요.");

            assertThatThrownBy(() -> adminArticleService.deleteArticlesByIds(List.of()))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("삭제할 게시글을 다시 확인해주세요.");

            verify(adminArticleRepository, never()).findAllById(any());
            verify(articleCommandRepository, never()).deleteByIds(any());
        }

        @Test
        @DisplayName("null 요청이면 예외를 던지고 삭제를 시도하지 않는다")
        void deleteArticlesByIdsWithNullIds() {
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_DELETE))
                    .thenReturn("삭제할 게시글을 다시 확인해주세요.");

            assertThatThrownBy(() -> adminArticleService.deleteArticlesByIds(null))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("삭제할 게시글을 다시 확인해주세요.");

            verify(adminArticleRepository, never()).findAllById(any());
            verify(articleCommandRepository, never()).deleteByIds(any());
        }

        @Test
        @DisplayName("일부 게시글이 존재하지 않으면 예외를 던진다")
        void deleteArticlesByIdsWithMissingArticle() {
            List<Long> articleIds = List.of(1L, 2L, 3L);
            when(adminArticleRepository.findAllById(articleIds))
                    .thenReturn(List.of(
                            createArticle(1L, "title1", createMenu(1L, 1L), Visibility.PRIVATE),
                            createArticle(2L, "title2", createMenu(2L, 2L), Visibility.PRIVATE)));
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_DELETE))
                    .thenReturn("없는 게시글이 포함되어있습니다.");

            assertThatThrownBy(() -> adminArticleService.deleteArticlesByIds(articleIds))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("없는 게시글이 포함되어있습니다.");

            verify(articleCommandRepository, never()).deleteByIds(any());
        }
    }

    @Nested
    @DisplayName("관리자 Article 메뉴 일괄 변경")
    class UpdateArticlesMenu {

        @Test
        @DisplayName("선택한 게시글의 메뉴를 일괄 변경한다")
        void updateArticlesMenuByIds() {
            List<Long> articleIds = List.of(1L, 2L);
            Menu sourceMenu = createMenu(10L, 1L);
            Menu targetMenu = createMenu(20L, 2L);
            List<Article> articles = List.of(
                    createArticle(1L, "title1", sourceMenu, Visibility.PRIVATE),
                    createArticle(2L, "title2", sourceMenu, Visibility.PUBLIC)
            );

            when(adminArticleRepository.findAllById(articleIds)).thenReturn(articles);
            when(menuRepository.findById(20L)).thenReturn(java.util.Optional.of(targetMenu));

            int updatedCount = adminArticleService.updateArticlesMenuByIds(articleIds, 20L);

            assertThat(updatedCount).isEqualTo(2);
            assertThat(articles).allMatch(article -> article.getMenu().getId().equals(20L));
        }

        @Test
        @DisplayName("빈 게시글 목록이면 예외를 던진다")
        void updateArticlesMenuByIdsWithEmptyIds() {
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_MENU_UPDATE))
                    .thenReturn("메뉴를 변경할 게시글이 올바르지 않습니다.");

            assertThatThrownBy(() -> adminArticleService.updateArticlesMenuByIds(List.of(), 1L))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("메뉴를 변경할 게시글이 올바르지 않습니다.");
        }

        @Test
        @DisplayName("대상 메뉴 ID가 null이면 예외를 던진다")
        void updateArticlesMenuByIdsWithNullMenuId() {
            assertThatThrownBy(() -> adminArticleService.updateArticlesMenuByIds(List.of(1L), null))
                    .isInstanceOf(RequiredValueException.class)
                    .hasMessage("targetMenuId은(는) null일 수 없습니다");
        }

        @Test
        @DisplayName("일부 게시글이 없으면 예외를 던진다")
        void updateArticlesMenuByIdsWithMissingArticle() {
            List<Long> articleIds = List.of(1L, 2L);
            when(adminArticleRepository.findAllById(articleIds))
                    .thenReturn(List.of(createArticle(1L, "title1", createMenu(10L, 1L), Visibility.PRIVATE)));
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_MENU_UPDATE))
                    .thenReturn("메뉴를 변경할 게시글이 올바르지 않습니다.");

            assertThatThrownBy(() -> adminArticleService.updateArticlesMenuByIds(articleIds, 20L))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("메뉴를 변경할 게시글이 올바르지 않습니다.");
        }

        @Test
        @DisplayName("대상 메뉴가 없으면 예외를 던진다")
        void updateArticlesMenuByIdsWithMissingMenu() {
            List<Long> articleIds = List.of(1L, 2L);
            List<Article> articles = List.of(
                    createArticle(1L, "title1", createMenu(10L, 1L), Visibility.PRIVATE),
                    createArticle(2L, "title2", createMenu(10L, 1L), Visibility.PUBLIC)
            );

            when(adminArticleRepository.findAllById(articleIds)).thenReturn(articles);
            when(menuRepository.findById(99L)).thenReturn(java.util.Optional.empty());
            when(messageSourceResolver.resolve(MenuExceptionMessage.MENU_NOT_FOUND, 99L))
                    .thenReturn("존재하지 않는 메뉴입니다.99");

            assertThatThrownBy(() -> adminArticleService.updateArticlesMenuByIds(articleIds, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("존재하지 않는 메뉴입니다.99");
        }
    }

    @Nested
    @DisplayName("관리자 Article visibility 일괄 변경")
    class UpdateArticlesVisibility {

        @Test
        @DisplayName("선택한 게시글의 visibility를 일괄 변경한다")
        void updateArticlesVisibilityByIds() {
            List<Long> articleIds = List.of(1L, 2L);
            List<Article> articles = List.of(
                    createArticle(1L, "title1", createMenu(10L, 1L), Visibility.PRIVATE),
                    createArticle(2L, "title2", createMenu(10L, 1L), Visibility.PRIVATE)
            );

            when(adminArticleRepository.findByIdInAndAuthor(articleIds, adminPrincipal.getName())).thenReturn(articles);

            int updatedCount = adminArticleService.updateArticlesVisibilityByIds(articleIds, "PUBLIC", adminPrincipal.getName());

            assertThat(updatedCount).isEqualTo(2);
            assertThat(articles).allMatch(article -> article.getVisibility() == Visibility.PUBLIC);
        }

        @Test
        @DisplayName("빈 게시글 목록이면 예외를 던진다")
        void updateArticlesVisibilityByIdsWithEmptyIds() {
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE))
                    .thenReturn("visibility를 변경할 게시글이 올바르지 않습니다.");

            assertThatThrownBy(() -> adminArticleService.updateArticlesVisibilityByIds(List.of(), "PUBLIC", userPrincipal.getName()))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("visibility를 변경할 게시글이 올바르지 않습니다.");
        }

        @Test
        @DisplayName("visibility 값이 비어 있으면 예외를 던진다")
        void updateArticlesVisibilityByIdsWithBlankVisibility() {
            assertThatThrownBy(() -> adminArticleService.updateArticlesVisibilityByIds(List.of(1L), " ", userPrincipal.getName()))
                    .isInstanceOf(RequiredValueException.class)
                    .hasMessage("visibility은(는) 공백일 수 없습니다");
        }

        @Test
        @DisplayName("일부 게시글이 없으면 예외를 던진다")
        void updateArticlesVisibilityByIdsWithMissingArticle() {
            List<Long> articleIds = List.of(1L, 2L);
            when(adminArticleRepository.findByIdInAndAuthor(articleIds, adminPrincipal.getName()))
                    .thenReturn(List.of(createArticle(1L, "title1", createMenu(10L, 1L), Visibility.PRIVATE)));
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE))
                    .thenReturn("visibility를 변경할 게시글이 올바르지 않습니다.");

            assertThatThrownBy(() -> adminArticleService.updateArticlesVisibilityByIds(articleIds, "PUBLIC", adminPrincipal.getName()))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("visibility를 변경할 게시글이 올바르지 않습니다.");
        }
    }

    private Article createArticle(Long articleId, String title, Menu menu, Visibility visibility) {
        Article article = Article.builder()
                .title(title)
                .content("content")
                .author("admin")
                .menu(menu)
                .visibility(visibility.name())
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(article, "id", articleId);
        return article;
    }

    private Menu createMenu(Long menuId, Long categoryId) {
        Category category = Category.builder().name("category").build();
        Menu menu = Menu.builder().name("menu-" + menuId).category(category).build();

        org.springframework.test.util.ReflectionTestUtils.setField(category, "id", categoryId);
        org.springframework.test.util.ReflectionTestUtils.setField(menu, "id", menuId);
        return menu;
    }
}
