package me.kimyeonsup.home.domain.blog.draft.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Thumbnail;
import me.kimyeonsup.home.domain.blog.draft.domain.dto.UpdateDraftArticle;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DraftArticle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Version
    private Long version = 0L;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "delete_yn", nullable = false)
    Boolean deleteYn = false;

    @Column(name = "author", nullable = false)
    String author;

    @ManyToOne
    private Menu menu;

    @Embedded
    private Thumbnail thumbnailUrl;

    @Builder
    public DraftArticle(String title, String subTitle, String content, Menu menu, String author, Thumbnail thumbnailUrl) {
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.menu = menu;
        this.author = author;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        if (thumbnailUrl == null) return "";
        String logicalFileName = thumbnailUrl.getLogicalFileName();
        if (logicalFileName == null || logicalFileName.isEmpty()) return "";
        return thumbnailUrl.getUrl();
    }

    public String getThumbnailLogicalFileName() {
        if (thumbnailUrl == null) return "";
        return thumbnailUrl.getLogicalFileName() == null ? "" : thumbnailUrl.getLogicalFileName();
    }

    public DraftArticle updateAll(UpdateDraftArticle draftArticle) {
        this.title = draftArticle.getTitle();
        this.subTitle = draftArticle.getSubTitle();
        this.content = draftArticle.getContent();
        this.menu = Menu.builder().id(draftArticle.getMenuId()).build();
        this.thumbnailUrl = Thumbnail.of(draftArticle.getThumbnailUrl(), draftArticle.getThumbnailLogicalFileName());
        return this;
    }

    public void delete() {
        this.deleteYn = true;
    }
}
