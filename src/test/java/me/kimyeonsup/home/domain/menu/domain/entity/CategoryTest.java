package me.kimyeonsup.home.domain.menu.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CategoryTest {

    @Test
    void builderWithoutMenusInitializesEmptyMenus() {
        Category category = Category.builder().name("language").build();

        assertThat(category.getMenus()).isNotNull().isEmpty();
        assertThat(category.hasMenus()).isFalse();
    }

    @Test
    void builderWithNullMenusInitializesEmptyMenus() {
        Category category = Category.builder().name("language").menus(null).build();

        assertThat(category.getMenus()).isNotNull().isEmpty();
        assertThat(category.hasMenus()).isFalse();
    }

    @Test
    void builderPreservesNonEmptyMenus() {
        Menu menu = Menu.builder().name("java").build();

        Category category = Category.builder().name("language").menus(List.of(menu)).build();

        assertThat(category.getMenus()).containsExactly(menu);
        assertThat(category.hasMenus()).isTrue();
    }
}
