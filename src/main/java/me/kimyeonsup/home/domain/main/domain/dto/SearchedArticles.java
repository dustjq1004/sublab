package me.kimyeonsup.home.domain.main.domain.dto;

import lombok.Getter;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticleListViewResponse;

import java.util.List;

@Getter
public class SearchedArticles {
    List<ArticleListViewResponse> articles;

    public SearchedArticles(List<ArticleListViewResponse> articles) {
        this.articles = articles;
    }
}
