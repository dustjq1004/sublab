package me.kimyeonsup.home.domain.blog.draft.domain.dto;

import lombok.Getter;
import me.kimyeonsup.home.domain.blog.draft.domain.entity.DraftArticle;

@Getter
public class DraftArticlesResponse {

    private final Long id;
    private final Long version;
    private final String title;
    private final String subTitle;
    private final String content;
    private final String author;
    private final Long menuId;
    private final String thumbnailUrl;
    private final String thumbnailLogicalFileName;

    public DraftArticlesResponse(DraftArticle draftArticle) {
        this.id = draftArticle.getId();
        this.version = draftArticle.getVersion();
        this.title = draftArticle.getTitle();
        this.subTitle = draftArticle.getSubTitle();
        this.content = draftArticle.getContent();
        this.author = draftArticle.getAuthor();
        this.menuId = draftArticle.getMenu().getId();
        this.thumbnailUrl = draftArticle.getThumbnailUrl();
        this.thumbnailLogicalFileName = draftArticle.getThumbnailLogicalFileName();
    }
}
