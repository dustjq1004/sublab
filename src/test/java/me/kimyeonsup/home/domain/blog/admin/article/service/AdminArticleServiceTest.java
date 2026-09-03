package me.kimyeonsup.home.domain.blog.admin.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.ArticleSelectCondition;
import me.kimyeonsup.home.domain.blog.admin.article.domain.entity.AdminArticle;
import me.kimyeonsup.home.domain.blog.admin.article.repository.AdminArticleRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminArticleServiceTest {

    @Mock
    private AdminArticleRepository adminArticleRepository;

    @Mock
    private MessageSourceResolver messageSourceResolver;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private AdminArticleService adminArticleService;

    @Nested
    @DisplayName("관리자 Article 조회")
    class FindArticles {

        @Test
        @DisplayName("메뉴 조건이 있으면 해당 메뉴의 게시글만 조회한다")
        void findArticlesByMenu() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder()
                    .menuId(3L)
                    .title("spring")
                    .build();
            Page<AdminArticle> expected = new PageImpl<>(List.of(
                    createArticle(1L, "spring boot", createMenu(3L, 1L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByMenu_IdAndTitleContaining(pageRequest, 3L, "spring"))
                    .thenReturn(expected);

            Page<AdminArticle> result = adminArticleService.findArticlesBy(pageRequest, condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMenu().getId()).isEqualTo(3L);
            verify(adminArticleRepository).findByMenu_IdAndTitleContaining(pageRequest, 3L, "spring");
        }

        @Test
        @DisplayName("카테고리 조건이 있으면 해당 카테고리의 게시글만 조회한다")
        void findArticlesByCategory() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder()
                    .categoryId(5L)
                    .title("java")
                    .build();
            Page<AdminArticle> expected = new PageImpl<>(List.of(
                    createArticle(2L, "java", createMenu(7L, 5L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByMenu_CategoryIdAndTitleContaining(pageRequest, 5L, "java"))
                    .thenReturn(expected);

            Page<AdminArticle> result = adminArticleService.findArticlesBy(pageRequest, condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMenu().getCategory().getId()).isEqualTo(5L);
            verify(adminArticleRepository).findByMenu_CategoryIdAndTitleContaining(pageRequest, 5L, "java");
        }

        @Test
        @DisplayName("조건이 없으면 제목 기준 전체 목록을 조회한다")
        void findArticlesWithoutFilter() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder().build();
            Page<AdminArticle> expected = new PageImpl<>(List.of(
                    createArticle(3L, "title", createMenu(1L, 1L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByTitleContaining(pageRequest, ""))
                    .thenReturn(expected);

            Page<AdminArticle> result = adminArticleService.findArticlesBy(pageRequest, condition);

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(adminArticleRepository).findByTitleContaining(pageRequest, "");
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 페이지를 반환한다")
        void findArticlesReturnsEmptyPage() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder()
                    .title("missing")
                    .build();

            when(adminArticleRepository.findByTitleContaining(pageRequest, "missing"))
                    .thenReturn(Page.empty(pageRequest));

            Page<AdminArticle> result = adminArticleService.findArticlesBy(pageRequest, condition);

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("메뉴와 visibility 조건이 있으면 두 조건을 모두 반영한다")
        void findArticlesByMenuAndVisibility() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder()
                    .menuId(3L)
                    .title("spring")
                    .visibility("PUBLIC")
                    .build();
            Page<AdminArticle> expected = new PageImpl<>(List.of(
                    createArticle(1L, "spring boot", createMenu(3L, 1L), Visibility.PUBLIC)));

            when(adminArticleRepository.findByMenu_IdAndVisibilityAndTitleContaining(pageRequest, 3L, Visibility.PUBLIC,
                    "spring")).thenReturn(expected);

            Page<AdminArticle> result = adminArticleService.findArticlesBy(pageRequest, condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getVisibility()).isEqualTo(Visibility.PUBLIC);
            verify(adminArticleRepository).findByMenu_IdAndVisibilityAndTitleContaining(pageRequest, 3L, Visibility.PUBLIC,
                    "spring");
        }

        @Test
        @DisplayName("카테고리와 visibility 조건이 있으면 두 조건을 모두 반영한다")
        void findArticlesByCategoryAndVisibility() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder()
                    .categoryId(5L)
                    .title("java")
                    .visibility("PRIVATE")
                    .build();
            Page<AdminArticle> expected = new PageImpl<>(List.of(
                    createArticle(2L, "java", createMenu(7L, 5L), Visibility.PRIVATE)));

            when(adminArticleRepository.findByMenu_CategoryIdAndVisibilityAndTitleContaining(pageRequest, 5L,
                    Visibility.PRIVATE, "java")).thenReturn(expected);

            Page<AdminArticle> result = adminArticleService.findArticlesBy(pageRequest, condition);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getVisibility()).isEqualTo(Visibility.PRIVATE);
            verify(adminArticleRepository).findByMenu_CategoryIdAndVisibilityAndTitleContaining(pageRequest, 5L,
                    Visibility.PRIVATE, "java");
        }

        @Test
        @DisplayName("visibility 조건만 있으면 visibility 기준으로 조회한다")
        void findArticlesByVisibilityOnly() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder()
                    .title("guide")
                    .visibility("PUBLIC")
                    .build();
            Page<AdminArticle> expected = new PageImpl<>(List.of(
                    createArticle(3L, "guide", createMenu(1L, 1L), Visibility.PUBLIC)));

            when(adminArticleRepository.findByVisibilityAndTitleContaining(pageRequest, Visibility.PUBLIC, "guide"))
                    .thenReturn(expected);

            Page<AdminArticle> result = adminArticleService.findArticlesBy(pageRequest, condition);

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(adminArticleRepository).findByVisibilityAndTitleContaining(pageRequest, Visibility.PUBLIC, "guide");
        }

        @Test
        @DisplayName("잘못된 visibility 값이면 예외를 던진다")
        void findArticlesWithInvalidVisibility() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            ArticleSelectCondition condition = ArticleSelectCondition.builder()
                    .visibility("UNKNOWN")
                    .build();

            assertThatThrownBy(() -> adminArticleService.findArticlesBy(pageRequest, condition))
                    .isInstanceOf(RequiredValueException.class)
                    .hasMessageContaining("visibility");
        }
    }

    @Nested
    @DisplayName("관리자 Article 일괄 삭제")
    class DeleteArticles {

        @Test
        @DisplayName("선택한 게시글이 모두 존재하면 일괄 삭제한다")
        void deleteArticlesByIds() {
            List<Long> articleIds = List.of(1L, 2L, 3L);
            List<AdminArticle> articles = articleIds.stream()
                    .map(id -> createArticle(id, "title" + id, createMenu(id, id), Visibility.PRIVATE))
                    .toList();

            when(adminArticleRepository.findAllById(articleIds)).thenReturn(articles);
            when(adminArticleRepository.deleteByIds(articleIds)).thenReturn(3);

            int deletedCount = adminArticleService.deleteArticlesByIds(articleIds);

            assertThat(deletedCount).isEqualTo(3);
            verify(adminArticleRepository).deleteByIds(articleIds);
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
            verify(adminArticleRepository, never()).deleteByIds(any());
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
            verify(adminArticleRepository, never()).deleteByIds(any());
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

            verify(adminArticleRepository, never()).deleteByIds(any());
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
            List<AdminArticle> articles = List.of(
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
            List<AdminArticle> articles = List.of(
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
            List<AdminArticle> articles = List.of(
                    createArticle(1L, "title1", createMenu(10L, 1L), Visibility.PRIVATE),
                    createArticle(2L, "title2", createMenu(10L, 1L), Visibility.PRIVATE)
            );

            when(adminArticleRepository.findAllById(articleIds)).thenReturn(articles);

            int updatedCount = adminArticleService.updateArticlesVisibilityByIds(articleIds, "PUBLIC");

            assertThat(updatedCount).isEqualTo(2);
            assertThat(articles).allMatch(article -> article.getVisibility() == Visibility.PUBLIC);
        }

        @Test
        @DisplayName("빈 게시글 목록이면 예외를 던진다")
        void updateArticlesVisibilityByIdsWithEmptyIds() {
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE))
                    .thenReturn("visibility를 변경할 게시글이 올바르지 않습니다.");

            assertThatThrownBy(() -> adminArticleService.updateArticlesVisibilityByIds(List.of(), "PUBLIC"))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("visibility를 변경할 게시글이 올바르지 않습니다.");
        }

        @Test
        @DisplayName("visibility 값이 비어 있으면 예외를 던진다")
        void updateArticlesVisibilityByIdsWithBlankVisibility() {
            assertThatThrownBy(() -> adminArticleService.updateArticlesVisibilityByIds(List.of(1L), " "))
                    .isInstanceOf(RequiredValueException.class)
                    .hasMessage("visibility은(는) 공백일 수 없습니다");
        }

        @Test
        @DisplayName("visibility 값이 잘못되면 예외를 던진다")
        void updateArticlesVisibilityByIdsWithInvalidVisibility() {
            assertThatThrownBy(() -> adminArticleService.updateArticlesVisibilityByIds(List.of(1L), "SECRET"))
                    .isInstanceOf(RequiredValueException.class)
                    .hasMessage("visibility 값이 올바르지 않습니다.");
        }

        @Test
        @DisplayName("일부 게시글이 없으면 예외를 던진다")
        void updateArticlesVisibilityByIdsWithMissingArticle() {
            List<Long> articleIds = List.of(1L, 2L);
            when(adminArticleRepository.findAllById(articleIds))
                    .thenReturn(List.of(createArticle(1L, "title1", createMenu(10L, 1L), Visibility.PRIVATE)));
            when(messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE))
                    .thenReturn("visibility를 변경할 게시글이 올바르지 않습니다.");

            assertThatThrownBy(() -> adminArticleService.updateArticlesVisibilityByIds(articleIds, "PUBLIC"))
                    .isInstanceOf(UnexpectedResultSizeException.class)
                    .hasMessage("visibility를 변경할 게시글이 올바르지 않습니다.");
        }
    }

    private AdminArticle createArticle(Long articleId, String title, Menu menu, Visibility visibility) {
        AdminArticle article = AdminArticle.builder()
                .title(title)
                .content("content")
                .author("admin")
                .menu(menu)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(article, "id", articleId);
        org.springframework.test.util.ReflectionTestUtils.setField(article, "visibility", visibility);
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
