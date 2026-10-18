package me.kimyeonsup.home.domain.menu.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;
import me.kimyeonsup.home.domain.menu.domain.dto.AddCategoryRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.CategoryResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.util.DateTimeFormat;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CategoryMapperTest {
    private final CategoryMapper mapper = new CategoryMapper();
    private final LocalDateTime createdAt = LocalDateTime.of(2020, 1, 2, 3, 4);
    private final LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 7, 8, 9);

    @Test
    void requestIsMappedToANewCategory() {
        Category category = mapper.toCategory(new AddCategoryRequest("language"));

        assertThat(category.getName()).isEqualTo("language");
    }

    @Test
    void createdResponseHasNoDatesAndAnEmptyMenuList() {
        CategoryResponse response = mapper.toCreatedResponse(category());
        assertResponse(response, null, null);
        assertThat(response.getMenus()).isEmpty();
    }

    @Test
    void detailResponsePreservesNullDateFormattingAndOmitsMenus() {
        CategoryResponse response = mapper.toDetailResponse(category());
        assertResponse(response,
                DateTimeFormat.diffDateFromNow(null), DateTimeFormat.diffDateFromNow(null));
        assertThat(response.getMenus()).isNull();
    }

    @Test
    void updatedResponseIncludesOnlyUpdatedDateAndOmitsMenus() {
        CategoryResponse response = mapper.toUpdatedResponse(category());
        assertResponse(response,
                DateTimeFormat.diffDateFromNow(null), DateTimeFormat.diffDateFromNow(updatedAt));
        assertThat(response.getMenus()).isNull();
    }

    @Test
    void listItemResponseIncludesBothDistinctDatesAndOmitsMenus() {
        CategoryResponse response = mapper.toListItemResponse(category());
        assertResponse(response, DateTimeFormat.diffDateFromNow(createdAt),
                DateTimeFormat.diffDateFromNow(updatedAt));
        assertThat(response.getMenus()).isNull();
        assertThat(response.getCreatedAt()).isNotEqualTo(response.getUpdatedAt());
    }

    @Test
    void navigationResponseIncludesMenusAndArticleCounts() {
        Category category = category();
        var menu = me.kimyeonsup.home.domain.menu.domain.entity.Menu.builder()
                .category(category).name("java").build();
        ReflectionTestUtils.setField(menu, "id", 7L);
        ReflectionTestUtils.setField(category, "menus", java.util.List.of(menu));

        CategoryResponse response = mapper.toNavigationResponse(category, Map.of(7L, 3));

        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
        assertThat(response.getMenus()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(7L);
            assertThat(item.getName()).isEqualTo("java");
            assertThat(item.getCategoryId()).isEqualTo(42L);
            assertThat(item.getCategoryEmoji()).isEqualTo("📚");
            assertThat(item.getCategoryName()).isEqualTo("language");
            assertThat(item.getArticleCount()).isEqualTo(3);
        });
    }

    private Category category() {
        Category category = Category.builder().name("language").build();
        ReflectionTestUtils.setField(category, "id", 42L);
        ReflectionTestUtils.setField(category, "emoji", "📚");
        ReflectionTestUtils.setField(category, "createdAt", createdAt);
        ReflectionTestUtils.setField(category, "updatedAt", updatedAt);
        return category;
    }

    private void assertResponse(CategoryResponse response, String created, String updated) {
        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getName()).isEqualTo("language");
        assertThat(response.getEmoji()).isEqualTo("📚");
        assertThat(response.getCreatedAt()).isEqualTo(created);
        assertThat(response.getUpdatedAt()).isEqualTo(updated);
    }
}
