package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response;


import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LatestArticles {

    List<ArticleListViewResponse> articles;

    public LatestArticles(List<ArticleListViewResponse> articles) {
        this.articles = articles;
    }
}
