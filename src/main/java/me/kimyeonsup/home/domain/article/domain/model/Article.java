package me.kimyeonsup.home.domain.article.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "author", nullable = false)
    private String author;

    @Embedded
    private Thumbnail thumbnailUrl;

    @ManyToOne
    private Menu menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    @ColumnDefault("'PRIVATE'")
    private Visibility visibility;

    @Builder
    public Article(String title, String content, Thumbnail thumbnailUrl, String subTitle, String author, Menu menu, String visibility) {
        this.title = title;
        this.content = content;
        this.subTitle = subTitle;
        this.author = author;
        this.menu = menu;
        this.thumbnailUrl = thumbnailUrl;
        this.visibility = Visibility.from(visibility);
    }

    public void update(String title, String subTitle, String content, Thumbnail thumbnailUrl, Menu menu, String visibility) {
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.menu = menu;
        this.visibility = Visibility.from(visibility);
    }

    public void changePublic() {
        this.visibility = Visibility.PUBLIC;
    }

    public void changePrivate() {
        this.visibility = Visibility.PRIVATE;
    }

    public String getThumbnailUrl() {
        if (thumbnailUrl == null) {
            return Thumbnail.empty().getUrl();
        }
        return thumbnailUrl.getUrl();
    }

    public String getThumbnailLogicalFileName() {
        if (thumbnailUrl == null) {
            return "";
        }
        return thumbnailUrl.getLogicalFileName();
    }

    public boolean isPrivate() {
        return this.visibility == Visibility.PRIVATE;
    }

    public void changeMenu(Menu menu) {
        this.menu = menu;
    }

    public void changeVisibility(String visibility) {
        this.visibility = Visibility.from(visibility);
    }
}
