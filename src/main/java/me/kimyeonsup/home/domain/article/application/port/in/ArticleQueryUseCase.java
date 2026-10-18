package me.kimyeonsup.home.domain.article.application.port.in;

import java.util.List;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleSummaryResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public interface ArticleQueryUseCase {
    ArticleResult findById(long id);
    Page<ArticleSummaryResult> findAllPagination(ArticleSearchQuery query);
    ArticleResult findByIdCredentialCheck(long id);
    List<ArticleSummaryResult> getLatestArticles();
    List<ArticleSummaryResult> getArticlesByTitle(String titleParam);
}
