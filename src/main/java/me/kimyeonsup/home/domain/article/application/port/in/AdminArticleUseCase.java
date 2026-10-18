package me.kimyeonsup.home.domain.article.application.port.in;

import java.util.List;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import org.springframework.data.domain.Page;

public interface AdminArticleUseCase {
    int updateArticlesVisibilityByIds(List<Long> articleIds, String visibility, String author);
    int updateArticlesMenuByIds(List<Long> articleIds, Long menuId);
    int deleteArticlesByIds(List<Long> articleIds);
    Page<ArticleResult> findArticlesBy(ArticleSearchQuery query);
}
