package me.kimyeonsup.home.domain.article.domain.model;

import static org.assertj.core.api.Assertions.*;

import me.kimyeonsup.home.domain.article.domain.model.*;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ArticleTest {
    @ParameterizedTest
    @CsvSource({"PUBLIC,PUBLIC", "public,PUBLIC", "PRIVATE,PRIVATE", "private,PRIVATE"})
    void createsVisibilityIgnoringCase(String input, Visibility expected) {
        assertThat(article(input).getVisibility()).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "SECRET"})
    void unrecognizedVisibilityBecomesNull(String input) {
        assertThat(Visibility.from(input)).isNull();
        assertThat(article(input).getVisibility()).isNull();
    }

    @Test
    void changesVisibility() {
        Article article = article("PUBLIC");
        article.changePrivate();
        assertThat(article.isPrivate()).isTrue();
        article.changePublic();
        assertThat(article.isPrivate()).isFalse();
        article.changeVisibility("private");
        assertThat(article.getVisibility()).isEqualTo(Visibility.PRIVATE);
    }

    @Test
    void updatesContentAndMenuPreservingAuthor() {
        Article article = article("PUBLIC");
        Menu menu = Menu.builder().id(2L).name("new menu").build();
        article.update("new title", "subtitle", "new body", Thumbnail.of("https://example.com/a.png", "a.png"), menu, "PRIVATE");
        assertThat(article.getTitle()).isEqualTo("new title");
        assertThat(article.getSubTitle()).isEqualTo("subtitle");
        assertThat(article.getContent()).isEqualTo("new body");
        assertThat(article.getAuthor()).isEqualTo("author");
        assertThat(article.getMenu()).isSameAs(menu);
        assertThat(article.getThumbnailUrl()).isEqualTo("https://example.com/a.png");
        assertThat(article.getThumbnailLogicalFileName()).isEqualTo("a.png");
        assertThat(article.isPrivate()).isTrue();
    }

    @Test
    void missingThumbnailUsesOneOfDefaultImages() {
        Article article = article("PUBLIC");
        assertThat(article.getThumbnailUrl()).isIn("/img/noimage01.png", "/img/noimage02.png", "/img/noimage03.png");
        assertThat(article.getThumbnailLogicalFileName()).isEmpty();
    }

    @Test
    void changesOnlyMenu() {
        Article article = article("PUBLIC");
        Menu menu = Menu.builder().id(3L).build();
        article.changeMenu(menu);
        assertThat(article.getMenu()).isSameAs(menu);
        assertThat(article.getTitle()).isEqualTo("title");
        assertThat(article.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    private Article article(String visibility) {
        return Article.builder().title("title").content("body").author("author").visibility(visibility).build();
    }
}
