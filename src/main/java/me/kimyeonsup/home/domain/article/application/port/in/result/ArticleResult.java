package me.kimyeonsup.home.domain.article.application.port.in.result;

import java.time.LocalDateTime;
import lombok.Builder;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;

@Builder
public record ArticleResult (
        Long id,
        String title,
        String subTitle,
        String content,
        String author,
        Long menuId,
        String menuName,
        Long draftId,
        String thumbnailUrl,
        String thumbnailLogicalFileName,
        Visibility visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
}
