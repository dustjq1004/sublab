package me.kimyeonsup.home.domain.draft.application.port.in.command;

public record CreateDraftCommand(
        String title,
        String subTitle,
        String content,
        Long menuId,
        String thumbnailUrl,
        String thumbnailLogicalFileName,
        String author
) {}
