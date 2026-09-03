package me.kimyeonsup.home.domain.blog.admin.article.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import me.kimyeonsup.home.domain.blog.admin.article.domain.entity.AdminArticle;
import me.kimyeonsup.home.util.StringUtils;

@Getter
public class AdminArticlesResponse {

    private final Long id;
    private final String title;
    private final String subTitle;
    private final String content;
    private final String author;
    private final String menuName;
    private final String visibility;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private final LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private final LocalDateTime updatedAt;

    public AdminArticlesResponse(AdminArticle article) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.subTitle = article.getSubTitle();
        this.content = StringUtils.replaceAllSpecialCharacter(article.getContent());
        this.author = article.getAuthor();
        this.menuName = article.getMenu().getName();
        this.visibility = article.getVisibility() != null ? article.getVisibility().name() : null;
        this.createdAt = article.getCreatedAt();
        this.updatedAt = article.getUpdatedAt();
    }
}
