package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response;

import java.util.List;
import lombok.Builder;

@Builder
public record ArticlesPaginationResponse (
    int pageNumber,
    int pageSize,
    int numberOfElements,
    boolean isFirst,
    boolean isLast,
    long totalPages,
    long totalCount,
    List<ArticleListViewResponse> articles
) {}
