package me.kimyeonsup.home.domain.draft.application.port.in.command;

public record UpdateDraftCommand(
        Long id,
        Long version,
        String title,
        String subTitle,
        String content,
        Long menuId,
        String thumbnailUrl,
        String thumbnailLogicalFileName,
        String author
) {}
