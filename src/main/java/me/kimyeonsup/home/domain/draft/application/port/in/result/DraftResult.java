package me.kimyeonsup.home.domain.draft.application.port.in.result;

import lombok.Builder;

@Builder
public record DraftResult(
        Long id,
        Long version,
        String title,
        String subTitle,
        String content,
        String author,
        Long menuId,
        String thumbnailUrl,
        String thumbnailLogicalFileName
) {}
