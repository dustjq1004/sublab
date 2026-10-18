package me.kimyeonsup.home.domain.article.application.port.in.command;

import lombok.Builder;

@Builder
public record CreateArticleCommand(
        String title,
        String subTitle,
        String content,
        Long menuId,
        String thumbnailUrl,
        String thumbnailLogicalFileName,
        String visibility,
        String author,
        Long drateId
) {
}
