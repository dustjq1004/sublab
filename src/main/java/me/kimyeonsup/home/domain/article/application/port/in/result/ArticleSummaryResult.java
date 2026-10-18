package me.kimyeonsup.home.domain.article.application.port.in.result;

import java.time.LocalDateTime;
import lombok.Builder;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;

@Builder
public record ArticleSummaryResult(
        Long id,
        String title,
        String subTitle,
        String content,
        String thumbnailUrl,
        String menuName,
        Visibility visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
