package me.kimyeonsup.home.domain.article.application.port.in.command;

import lombok.Builder;

@Builder
public record UpdateArticleCommand(
        Long id,
        String title,
        String subTitle,
        String content,
        Long menuId,
        Long draftId,
        String author,
        String thumbnailUrl,
        String thumbnailLogicalFileName,
        String visibility
){
}
