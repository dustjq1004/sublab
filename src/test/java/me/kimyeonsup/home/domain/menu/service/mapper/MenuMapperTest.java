package me.kimyeonsup.home.domain.menu.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.menu.domain.dto.AddMenuRequest;
import me.kimyeonsup.home.domain.menu.domain.dto.MenuResponse;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.util.DateTimeFormat;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MenuMapperTest {
    private final MenuMapper mapper = new MenuMapper();
    private final LocalDateTime createdAt = LocalDateTime.of(2020, 1, 2, 3, 4);
    private final LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 7, 8, 9);

    @Test
    void requestIsMappedToANewMenu() {
        Category category = category();

        Menu menu = mapper.toMenu(new AddMenuRequest(42L, "☕", "java", "description"), category);

        assertThat(menu.getCategory()).isSameAs(category);
        assertThat(menu.getEmoji()).isEqualTo("☕");
        assertThat(menu.getName()).isEqualTo("java");
        assertThat(menu.getDescription()).isEqualTo("description");
    }

    @Test
    void createdAndDeletedResponsesUseTheCommandFieldSet() {
        Menu menu = menu();

        for (MenuResponse response : List.of(
                mapper.toCreatedResponse(menu),
                mapper.toDeletedResponse(menu))) {
            assertCommandResponse(response);
            assertThat(response.getEmoji()).isNull();
        }
    }

    @Test
    void updatedResponseIncludesTheCurrentEmoji() {
        MenuResponse response = mapper.toUpdatedResponse(menu());

        assertCommandResponse(response);
        assertThat(response.getEmoji()).isEqualTo("☕");
    }

    @Test
    void detailResponseIncludesContentDatesAndArticleCount() {
        MenuResponse response = mapper.toDetailResponse(menu(), 2);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getEmoji()).isEqualTo("☕");
        assertThat(response.getName()).isEqualTo("java");
        assertThat(response.getDescription()).isEqualTo("description");
        assertThat(response.getCreatedAt()).isEqualTo(DateTimeFormat.diffDateFromNow(createdAt));
        assertThat(response.getUpdatedAt()).isEqualTo(DateTimeFormat.diffDateFromNow(updatedAt));
        assertThat(response.getArticleCount()).isEqualTo(2);
        assertCategory(response);
    }

    @Test
    void listResponseMapsEveryMenuAndKeepsAnEmptyArray() {
        Menu menu = menu();

        assertThat(mapper.toListResponse(List.of(menu), Map.of(7L, 2)).getMenus())
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.getId()).isEqualTo(7L);
                    assertThat(response.getArticleCount()).isEqualTo(2);
                });
        assertThat(mapper.toListResponse(List.of(), Map.of()).getMenus()).isEmpty();
    }

    private void assertCommandResponse(MenuResponse response) {
        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("java");
        assertThat(response.getDescription()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
        assertThat(response.getArticleCount()).isZero();
        assertCategory(response);
    }

    private void assertCategory(MenuResponse response) {
        assertThat(response.getCategoryId()).isEqualTo(42L);
        assertThat(response.getCategoryEmoji()).isEqualTo("📚");
        assertThat(response.getCategoryName()).isEqualTo("language");
    }

    private Menu menu() {
        Menu menu = Menu.builder()
                .id(7L)
                .emoji("☕")
                .name("java")
                .description("description")
                .category(category())
                .articles(List.of(Article.builder().build(), Article.builder().build()))
                .build();
        ReflectionTestUtils.setField(menu, "createdAt", createdAt);
        ReflectionTestUtils.setField(menu, "updatedAt", updatedAt);
        return menu;
    }

    private Category category() {
        Category category = Category.builder().name("language").build();
        ReflectionTestUtils.setField(category, "id", 42L);
        ReflectionTestUtils.setField(category, "emoji", "📚");
        return category;
    }
}
