package me.kimyeonsup.home.domain.blog.article.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class ArticleUpdatedResponse {

    private Long id;
    private String title;
    private String subTitle;
    private String content;
    private LocalDateTime createdAt;
    private String author;
    private String menuName;

    public ArticleUpdatedResponse(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.subTitle = article.getSubTitle();
        this.content = article.getContent();
        this.createdAt = article.getCreatedAt();
        this.author = article.getAuthor();
        this.menuName = article.getMenu().getName();
    }
}
