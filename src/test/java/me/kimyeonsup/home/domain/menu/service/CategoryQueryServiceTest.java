package me.kimyeonsup.home.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.application.port.out.MenuArticleCount;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.CategoryRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.CategoryMapper;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuArticleCountMapConverter;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock ArticleQueryRepository articleQueryRepository;
    @Mock MessageSourceResolver messageSourceResolver;
    @Mock MenuArticleCountMapConverter articleCountConverter;
    @Spy CategoryMapper mapper = new CategoryMapper();
    @InjectMocks CategoryQueryService categoryQueryService;

    @Test
    void navigationUsesEmptyMenuListForCategoryWithoutMenus() {
        // CQ-001
        Category emptyCategory = category(1L, "empty", "E", new ArrayList<>());
        when(categoryRepository.findAllWithMenus()).thenReturn(List.of(emptyCategory));
        List<MenuArticleCount> articleCounts = List.of();
        when(articleQueryRepository.countByMenuId()).thenReturn(articleCounts);
        when(articleCountConverter.toArticleCountByMenuId(articleCounts)).thenReturn(Map.of());

        List<CategoryResponse> response = categoryQueryService.findNavigationCategories();

        assertThat(response).hasSize(1);
        CategoryResponse empty = categoryById(response, 1L);
        assertThat(empty.getId()).isEqualTo(1L);
        assertThat(empty.getName()).isEqualTo("empty");
        assertThat(empty.getMenus()).isNotNull().isEmpty();
        verify(categoryRepository).findAllWithMenus();
        verify(articleQueryRepository).countByMenuId();
        verify(articleCountConverter).toArticleCountByMenuId(articleCounts);
        verify(categoryRepository, never()).findAll();
        verifyNoInteractions(messageSourceResolver);
    }

    @Test
    void navigationKeepsMenusAndOwnershipSeparatedByCategory() {
        // CQ-002
        List<Menu> categoryAMenus = new ArrayList<>();
        List<Menu> categoryBMenus = new ArrayList<>();
        Category categoryA = category(1L, "category-a", "A", categoryAMenus);
        Category categoryB = category(2L, "category-b", "B", categoryBMenus);
        Menu menuA = menu(11L, "menu-a", categoryA);
        Menu menuB = menu(21L, "menu-b", categoryB);
        categoryAMenus.add(menuA);
        categoryBMenus.add(menuB);
        when(categoryRepository.findAllWithMenus()).thenReturn(List.of(categoryA, categoryB));
        List<MenuArticleCount> articleCounts = List.of(count(11L, 4L));
        when(articleQueryRepository.countByMenuId()).thenReturn(articleCounts);
        when(articleCountConverter.toArticleCountByMenuId(articleCounts)).thenReturn(Map.of(11L, 4));

        List<CategoryResponse> response = categoryQueryService.findNavigationCategories();

        assertThat(response).extracting(CategoryResponse::getId)
                .containsExactlyInAnyOrder(1L, 2L);
        CategoryResponse responseA = categoryById(response, 1L);
        CategoryResponse responseB = categoryById(response, 2L);
        assertThat(responseA.getMenus()).extracting(MenuResponse::getId)
                .containsExactly(11L)
                .doesNotContain(21L);
        assertThat(responseB.getMenus()).extracting(MenuResponse::getId)
                .containsExactly(21L)
                .doesNotContain(11L);
        assertMenuOwnership(menuById(responseA, 11L), 1L, "category-a", "A", 4);
        assertMenuOwnership(menuById(responseB, 21L), 2L, "category-b", "B", 0);
        verify(categoryRepository).findAllWithMenus();
        verify(articleQueryRepository).countByMenuId();
        verify(articleCountConverter).toArticleCountByMenuId(articleCounts);
        verifyNoInteractions(messageSourceResolver);
    }

    private void assertMenuOwnership(MenuResponse menu, long categoryId, String categoryName,
                                     String categoryEmoji, int articleCount) {
        assertThat(menu.getCategoryId()).isEqualTo(categoryId);
        assertThat(menu.getCategoryName()).isEqualTo(categoryName);
        assertThat(menu.getCategoryEmoji()).isEqualTo(categoryEmoji);
        assertThat(menu.getArticleCount()).isEqualTo(articleCount);
    }

    private CategoryResponse categoryById(List<CategoryResponse> response, long id) {
        return response.stream()
                .filter(category -> category.getId() == id)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing category id " + id));
    }

    private MenuResponse menuById(CategoryResponse category, long id) {
        return category.getMenus().stream()
                .filter(menu -> menu.getId() == id)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing menu id " + id));
    }

    private Category category(long id, String name, String emoji, List<Menu> menus) {
        Category category = Category.builder().name(name).menus(menus).build();
        ReflectionTestUtils.setField(category, "id", id);
        ReflectionTestUtils.setField(category, "emoji", emoji);
        return category;
    }

    private Menu menu(long id, String name, Category category) {
        return Menu.builder()
                .id(id)
                .name(name)
                .category(category)
                .articles(List.of())
                .build();
    }

    private MenuArticleCount count(long menuId, long articleCount) {
        return new MenuArticleCount(menuId, articleCount);
    }
}
