package me.kimyeonsup.home.domain.blog.article.domain.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class ArticlesPaginationResponse {

    private final int pageNumber;
    private final int pageSize;
    private final int numberOfElements;
    private final boolean isFirst;
    private final boolean isLast;
    private final long totalPages; // 전체 페이지 수
    private final long totalCount;
    private final List<ArticleListViewResponse> articles;

    public static ArticlesPaginationResponse of(Page<Article> articles) {
        return ArticlesPaginationResponse.builder()
                .pageNumber(articles.getNumber())
                .pageSize(articles.getSize())
                .numberOfElements(articles.getNumberOfElements())
                .isFirst(articles.isFirst())
                .isLast(articles.isLast())
                .totalPages(articles.getTotalPages())
                .totalCount(articles.getTotalElements())
                .articles(articles.stream()
                        .map(ArticleListViewResponse::new)
                        .toList())
                .build();
    }
}
