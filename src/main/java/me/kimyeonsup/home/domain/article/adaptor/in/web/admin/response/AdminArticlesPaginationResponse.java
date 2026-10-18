package me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminArticlesPaginationResponse<T> {

    private final int pageNumber;
    private final int pageSize;
    private final int numberOfElements;
    @JsonProperty("isFirst")
    private final boolean isFirst;
    @JsonProperty("isLast")
    private final boolean isLast;
    private final long totalPages; // 전체 페이지 수
    private final long totalCount;
    private final List<T> articles;
}
