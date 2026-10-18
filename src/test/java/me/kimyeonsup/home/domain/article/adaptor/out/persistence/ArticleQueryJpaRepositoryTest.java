package me.kimyeonsup.home.domain.article.adaptor.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ArticleQueryJpaRepositoryTest {

    @Autowired
    private ArticleQueryJpaRepository repository;

    @Test
    void findsOnlyRequestedIdsOwnedByAuthor() {
        Article first = save("author");
        Article second = save("author");
        Article otherAuthor = save("other");
        save("author");

        List<Article> articles = repository.findByIdInAndAuthor(
                List.of(first.getId(), second.getId(), otherAuthor.getId()), "author");

        assertThat(articles).extracting(Article::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void returnsEmptyWhenRequestedArticlesBelongToAnotherAuthor() {
        Article article = save("other");
        assertThat(repository.findByIdInAndAuthor(List.of(article.getId()), "author")).isEmpty();
    }

    private Article save(String author) {
        return repository.saveAndFlush(Article.builder().title("title").content("body")
                .author(author).visibility("PRIVATE").build());
    }
}
