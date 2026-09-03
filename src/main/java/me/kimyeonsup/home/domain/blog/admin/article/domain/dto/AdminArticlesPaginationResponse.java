package me.kimyeonsup.home.domain.blog.admin.article.domain.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminArticlesPaginationResponse<T> {

    private final int pageNumber;
    private final int pageSize;
    private final int numberOfElements;
    private final boolean isFirst;
    private final boolean isLast;
    private final long totalPages; // 전체 페이지 수
    private final long totalCount;
    private final List<T> articles;
}
