package me.kimyeonsup.home.domain.draft.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.article.domain.model.Thumbnail;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;

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
    private Boolean deleteYn = false;

    @Column(name = "author", nullable = false)
    private String author;

    @ManyToOne
    private Menu menu;

    @Embedded
    private Thumbnail thumbnailUrl;

    @Builder
    public DraftArticle(String title, String subTitle, String content, Menu menu, String author, Thumbnail thumbnailUrl) {
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.author = author;
        this.menu = menu;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void update(String title, String subTitle, String content, Menu menu, Thumbnail thumbnail) {
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.menu = menu;
        this.thumbnailUrl = thumbnail;
    }

    public void delete() {
        this.deleteYn = true;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl == null ? "" : thumbnailUrl.getUrl();
    }

    public String getThumbnailLogicalFileName() {
        return thumbnailUrl == null || thumbnailUrl.getLogicalFileName() == null
                ? "" : thumbnailUrl.getLogicalFileName();
    }
}
