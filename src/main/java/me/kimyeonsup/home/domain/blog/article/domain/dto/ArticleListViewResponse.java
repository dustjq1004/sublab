package me.kimyeonsup.home.domain.blog.article.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.util.StringUtils;

@Getter
public class ArticleListViewResponse {

    private final Long id;
    private final String title;
    private final String subTitle;
    private final String content;
    private final String thumbnailUrl;
    private final String menuName;
    private final Visibility visibility;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private final LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private final LocalDateTime updatedAt;

    public ArticleListViewResponse(Article article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.subTitle = article.getSubTitle();
        this.content = StringUtils.replaceAllSpecialCharacter(article.getContent());
        this.thumbnailUrl = article.getThumbnailUrl();
        this.menuName = article.getMenu().getName();
        this.visibility = article.getVisibility();
        this.createdAt = article.getCreatedAt();
        this.updatedAt = article.getUpdatedAt();
    }
}
