package me.kimyeonsup.home.domain.article.application.port.in.command;

import lombok.Builder;
import org.springframework.data.domain.Sort.Direction;

@Builder
public record ArticleSearchQuery(
        int pageNumber,
        int pageSize,
        String sortedBy,
        Direction direction,
        Long categoryId,
        Long menuId,
        String menuName,
        String searchKeyword,
        String visibility
) {
}
