package me.kimyeonsup.home.domain.blog.article.assembler;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticleResponse;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.markdown.service.MarkdownService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDtoAssembler {

    private final MarkdownService markdownService;

    // 빈 dto 생성
    public ArticleResponse toEmpty() {
        // 빈 response dto 생성
        return ArticleResponse.builder().build();
    }

    public ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
            article.getId(),
            article.getTitle(),
            article.getSubTitle(),
            markdownService.toSafeHtml(article.getContent()),
            article.getAuthor(),
            article.getMenu().getName(),
            article.getThumbnailLogicalFileName(),
            article.getThumbnailUrl(),
            article.getVisibility(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }

    public ArticleResponse toEditResponse(Article article) {
        return new ArticleResponse(
            article.getId(),
            article.getTitle(),
            article.getSubTitle(),
            article.getContent(),
            article.getAuthor(),
            article.getMenu().getName(),
            article.getThumbnailLogicalFileName(),
            article.getThumbnailUrl(),
            article.getVisibility(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }
}
