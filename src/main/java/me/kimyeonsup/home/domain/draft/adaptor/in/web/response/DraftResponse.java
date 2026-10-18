package me.kimyeonsup.home.domain.draft.adaptor.in.web.response;

import lombok.Builder;

@Builder
public record DraftResponse(
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
