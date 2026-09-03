package me.kimyeonsup.home.domain.blog.admin.article.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Thumbnail;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;

@Entity
@Getter
@Table(name = "article")
@NoArgsConstructor
public class AdminArticle extends BaseTimeEntity {

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
    @Column(name = "thumbnail_url")
    private Thumbnail thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility;

    @Builder
    public AdminArticle(String title, String content, Thumbnail thumbnailUrl, String subTitle, String author,
                        Menu menu) {
        this.title = title;
        this.content = content;
        this.subTitle = subTitle;
        this.author = author;
        this.menu = menu;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void changeMenu(Menu menu) {
        this.menu = menu;
    }

    public void changeVisibility(String visibility) {
        this.visibility = Visibility.from(visibility);
    }

}
