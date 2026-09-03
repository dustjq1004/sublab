package me.kimyeonsup.home.domain.main.domain.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticleListViewResponse;

import java.util.List;

@Getter
@NoArgsConstructor
public class LatestArticles {

    List<ArticleListViewResponse> articles;

    public LatestArticles(List<ArticleListViewResponse> articles) {
        this.articles = articles;
    }
}
