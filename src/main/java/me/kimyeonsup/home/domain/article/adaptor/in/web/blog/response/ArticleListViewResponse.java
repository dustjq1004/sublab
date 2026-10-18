package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleSummaryResult;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
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

    public ArticleListViewResponse(ArticleSummaryResult article) {
        this.id = article.id();
        this.title = article.title();
        this.subTitle = article.subTitle();
        this.content = StringUtils.replaceAllSpecialCharacter(article.content());
        this.thumbnailUrl = article.thumbnailUrl();
        this.menuName = article.menuName();
        this.visibility = article.visibility();
        this.createdAt = article.createdAt();
        this.updatedAt = article.updatedAt();
    }
}
