package me.kimyeonsup.home.domain.article.adaptor.in.web.admin.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
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

    public AdminArticlesResponse(ArticleResult article) {
        this.id = article.id();
        this.title = article.title();
        this.subTitle = article.subTitle();
        this.content = StringUtils.replaceAllSpecialCharacter(article.content());
        this.author = article.author();
        this.menuName = article.menuName();
        this.visibility = article.visibility() != null ? article.visibility().name() : null;
        this.createdAt = article.createdAt();
        this.updatedAt = article.updatedAt();
    }
}
