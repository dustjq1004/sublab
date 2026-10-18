package me.kimyeonsup.home.domain.menu.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import org.junit.jupiter.api.Test;

class MenuTest {

    @Test
    void builderWithoutArticlesInitializesEmptyArticles() {
        Menu menu = Menu.builder()
                .name("java")
                .category(Category.builder().name("language").build())
                .build();

        assertThat(menu.getArticles()).isNotNull().isEmpty();
        assertThat(menu.hasArticles()).isFalse();
    }

    @Test
    void builderWithNullArticlesInitializesEmptyArticles() {
        Menu menu = Menu.builder()
                .name("java")
                .category(Category.builder().name("language").build())
                .articles(null)
                .build();

        assertThat(menu.getArticles()).isNotNull().isEmpty();
        assertThat(menu.hasArticles()).isFalse();
    }

    @Test
    void builderPreservesNonEmptyArticles() {
        Article article = Article.builder().title("title").content("body").author("author").build();

        Menu menu = Menu.builder()
                .name("java")
                .category(Category.builder().name("language").build())
                .articles(List.of(article))
                .build();

        assertThat(menu.getArticles()).containsExactly(article);
        assertThat(menu.hasArticles()).isTrue();
    }

    @Test
    void updatesNameDescriptionEmojiAndCategory() {
        Category originalCategory = Category.builder().name("original").build();
        Category changedCategory = Category.builder().name("changed").build();
        Menu menu = Menu.builder()
                .name("java")
                .description("original description")
                .emoji("☕")
                .category(originalCategory)
                .build();

        menu.update("kotlin", "changed description", "🧩", changedCategory);

        assertThat(menu.getName()).isEqualTo("kotlin");
        assertThat(menu.getDescription()).isEqualTo("changed description");
        assertThat(menu.getCategory()).isSameAs(changedCategory);
        assertThat(menu.getEmoji()).isEqualTo("🧩");
    }

    @Test
    void nullEmojiDeletesTheExistingEmoji() {
        Menu menu = Menu.builder()
                .name("java")
                .emoji("☕")
                .category(Category.builder().name("language").build())
                .build();

        menu.update("java", null, null, menu.getCategory());

        assertThat(menu.getEmoji()).isNull();
    }
}
