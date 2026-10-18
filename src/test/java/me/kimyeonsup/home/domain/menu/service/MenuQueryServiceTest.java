package me.kimyeonsup.home.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.application.port.out.MenuArticleCount;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.dto.MenusResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuArticleCountMapConverter;
import me.kimyeonsup.home.domain.menu.service.mapper.MenuMapper;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MenuQueryServiceTest {

    @Mock MenuRepository menuRepository;
    @Mock ArticleQueryRepository articleQueryRepository;
    @Mock MessageSourceResolver messageSourceResolver;
    @Mock MenuArticleCountMapConverter articleCountConverter;
    @Spy MenuMapper mapper = new MenuMapper();
    @InjectMocks MenuQueryService menuQueryService;

    @Test
    void findAllUsesZeroForMenuMissingFromArticleCounts() {
        // MQ-001
        Category category = category(1L, "category-a", "A");
        Menu countedMenu = menu(11L, "counted", category);
        Menu uncountedMenu = menu(12L, "uncounted", category);
        when(menuRepository.findAllWithCategory()).thenReturn(List.of(countedMenu, uncountedMenu));
        List<MenuArticleCount> articleCounts = List.of(count(11L, 3L));
        when(articleQueryRepository.countByMenuId()).thenReturn(articleCounts);
        when(articleCountConverter.toArticleCountByMenuId(articleCounts)).thenReturn(Map.of(11L, 3));

        MenusResponse response = menuQueryService.findAll();

        assertThat(response.getMenus()).hasSize(2);
        assertThat(menuById(response, 11L).getArticleCount()).isEqualTo(3);
        MenuResponse uncounted = menuById(response, 12L);
        assertThat(uncounted.getArticleCount()).isZero();
        assertThat(uncounted.getId()).isEqualTo(12L);
        assertThat(uncounted.getName()).isEqualTo("uncounted");
        assertThat(uncounted.getCategoryId()).isEqualTo(1L);
        assertThat(uncounted.getCategoryName()).isEqualTo("category-a");
        verify(menuRepository).findAllWithCategory();
        verify(articleQueryRepository).countByMenuId();
        verify(articleCountConverter).toArticleCountByMenuId(articleCounts);
        verify(menuRepository, never()).findByCategoryIdWithCategory(org.mockito.ArgumentMatchers.anyLong());
        verifyNoInteractions(messageSourceResolver);
    }

    @Test
    void findByCategoryUsesZeroForMissingCountAndExcludesOtherCategoryMenu() {
        // MQ-002
        Category categoryA = category(1L, "category-a", "A");
        Menu countedMenu = menu(11L, "counted", categoryA);
        Menu uncountedMenu = menu(12L, "uncounted", categoryA);
        when(menuRepository.findByCategoryIdWithCategory(1L)).thenReturn(List.of(countedMenu, uncountedMenu));
        List<MenuArticleCount> articleCounts = List.of(count(11L, 2L), count(21L, 5L));
        when(articleQueryRepository.countByMenuId()).thenReturn(articleCounts);
        when(articleCountConverter.toArticleCountByMenuId(articleCounts))
                .thenReturn(Map.of(11L, 2, 21L, 5));

        MenusResponse response = menuQueryService.findByCategory(1L);

        assertThat(response.getMenus()).extracting(MenuResponse::getId)
                .containsExactlyInAnyOrder(11L, 12L)
                .doesNotContain(21L);
        assertThat(menuById(response, 11L).getArticleCount()).isEqualTo(2);
        assertThat(menuById(response, 12L).getArticleCount()).isZero();
        assertThat(response.getMenus()).allSatisfy(menu -> {
            assertThat(menu.getCategoryId()).isEqualTo(1L);
            assertThat(menu.getCategoryName()).isEqualTo("category-a");
        });
        verify(menuRepository).findByCategoryIdWithCategory(1L);
        verify(articleQueryRepository).countByMenuId();
        verify(articleCountConverter).toArticleCountByMenuId(articleCounts);
        verify(menuRepository, never()).findAllWithCategory();
        verifyNoInteractions(messageSourceResolver);
    }

    private MenuResponse menuById(MenusResponse response, long id) {
        return response.getMenus().stream()
                .filter(menu -> menu.getId() == id)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing menu id " + id));
    }

    private Category category(long id, String name, String emoji) {
        Category category = Category.builder().name(name).menus(List.of()).build();
        ReflectionTestUtils.setField(category, "id", id);
        ReflectionTestUtils.setField(category, "emoji", emoji);
        return category;
    }

    private Menu menu(long id, String name, Category category) {
        return Menu.builder()
                .id(id)
                .name(name)
                .description("description-" + id)
                .emoji("menu-" + id)
                .category(category)
                .articles(List.of())
                .build();
    }

    private MenuArticleCount count(long menuId, long articleCount) {
        return new MenuArticleCount(menuId, articleCount);
    }
}
