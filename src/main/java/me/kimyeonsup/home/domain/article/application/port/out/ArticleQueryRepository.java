package me.kimyeonsup.home.domain.article.application.port.out;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.article.adaptor.out.persistence.ArticlePrevNextDto;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleQueryRepository {

    Optional<Article> findById(Long id);
    List<Article> findAllById(List<Long> ids);
    Optional<Article> findByIdAndAuthor(Long id, String author);
    Page<Article> findByMenuNameAndTitleContaining(ArticleSearchQuery query);
    Page<Article> findByMenuNameAndTitleContainingAndVisibility(ArticleSearchQuery query, Visibility visibility);
    Page<Article> findByCategoryIdAndTitleContaining(ArticleSearchQuery query);
    Page<Article> findByCategoryIdAndTitleContainingAndVisibility(ArticleSearchQuery query, Visibility visibility);
    ArticlePrevNextDto findPrevNextArticle(Long id, String menuName);
    List<Article> findTop8ByOrderByCreatedAtDesc();
    List<Article> findTop8ByVisibilityOrderByCreatedAtDesc(@Param("visibility") Visibility visibility);
    List<Article> findByTitleContains(@NotBlank String titleParam);
    List<Article> findByTitleContainsAndVisibility(@NotBlank String titleParam, Visibility visibility);
    List<MenuArticleCount> countByMenuId();
    Page<Article> findByTitleContaining(ArticleSearchQuery query);
    Page<Article> findByTitleContainingAndVisibility(ArticleSearchQuery query, Visibility visibility);
    Page<Article> findByMenu_IdAndTitleContaining(ArticleSearchQuery query);
    Page<Article> findByMenu_IdAndTitleContainingAndVisibility(ArticleSearchQuery query);
    Page<Article> findByMenu_CategoryIdAndTitleContaining(ArticleSearchQuery query);
    Page<Article> findByMenu_CategoryIdAndVisibilityAndTitleContaining(ArticleSearchQuery query);
    Page<Article> findByTitleContainsAndVisibility(ArticleSearchQuery query);
    List<Article> findByIdInAndAuthor(List<Long> articleIds, String author);
}
