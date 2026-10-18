package me.kimyeonsup.home.domain.article.adaptor.in.web.blog.response;

import java.util.List;
import lombok.Getter;

@Getter
public class SearchedArticles {
    List<ArticleListViewResponse> articles;

    public SearchedArticles(List<ArticleListViewResponse> articles) {
        this.articles = articles;
    }
}
