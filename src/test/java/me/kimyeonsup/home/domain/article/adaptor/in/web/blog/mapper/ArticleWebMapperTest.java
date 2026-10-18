package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.mapper.ArticleWebMapper;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.AddArticleRequest;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.request.UpdateArticleRequest;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleSummaryResult;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ArticleWebMapperTest {
    private final ArticleWebMapper mapper = new ArticleWebMapper();

    @Test
    void createCommandIncludesAuthenticatedAuthorAndDraft() {
        var request = new AddArticleRequest("title", "subtitle", "body", 2L, 3L,
                "https://example.com/a.png", "a.png", "PUBLIC");
        var command = mapper.toCreateArticleCommand(request, "author");
        assertThat(command.title()).isEqualTo("title");
        assertThat(command.subTitle()).isEqualTo("subtitle");
        assertThat(command.content()).isEqualTo("body");
        assertThat(command.author()).isEqualTo("author");
        assertThat(command.menuId()).isEqualTo(2L);
        assertThat(command.drateId()).isEqualTo(3L);
        assertThat(command.thumbnailUrl()).isEqualTo("https://example.com/a.png");
        assertThat(command.thumbnailLogicalFileName()).isEqualTo("a.png");
        assertThat(command.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void updateCommandUsesBodyIdAndAuthenticatedAuthor() {
        var request = new UpdateArticleRequest(1L, "title", "subtitle", "body", 2L,
                "https://example.com/a.png", "a.png", "PRIVATE");
        var command = mapper.toUpdateArticleCommand(request, "author");
        assertThat(command.id()).isEqualTo(1L);
        assertThat(command.author()).isEqualTo("author");
        assertThat(command.title()).isEqualTo("title");
        assertThat(command.subTitle()).isEqualTo("subtitle");
        assertThat(command.content()).isEqualTo("body");
        assertThat(command.menuId()).isEqualTo(2L);
        assertThat(command.thumbnailUrl()).isEqualTo("https://example.com/a.png");
        assertThat(command.thumbnailLogicalFileName()).isEqualTo("a.png");
        assertThat(command.visibility()).isEqualTo("PRIVATE");
    }

    @Test
    void paginationPreservesPageMetadata() {
        var summary = ArticleSummaryResult.builder().id(7L).title("title").content("body")
                .visibility(Visibility.PUBLIC).build();
        var response = mapper.toArticlesPaginationResponse(
                new PageImpl<>(List.of(summary), PageRequest.of(2, 10), 21));
        assertThat(response.pageNumber()).isEqualTo(2);
        assertThat(response.pageSize()).isEqualTo(10);
        assertThat(response.totalCount()).isEqualTo(21);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.numberOfElements()).isEqualTo(1);
        assertThat(response.articles()).singleElement().satisfies(article -> {
            assertThat(article.getId()).isEqualTo(7L);
            assertThat(article.getTitle()).isEqualTo("title");
        });
    }
}
