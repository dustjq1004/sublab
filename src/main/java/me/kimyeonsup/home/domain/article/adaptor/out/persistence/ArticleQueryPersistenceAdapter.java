package me.kimyeonsup.home.domain.article.adaptor.out.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.application.port.out.MenuArticleCount;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleQueryPersistenceAdapter implements ArticleQueryRepository {

    private final ArticleQueryJpaRepository articleQueryJpaRepository;

    @Override
    public Optional<Article> findById(Long id) {
        return articleQueryJpaRepository.findById(id);
    }

    @Override
    public List<Article> findAllById(List<Long> ids) {
        return articleQueryJpaRepository.findAllById(ids);
    }

    @Override
    public Optional<Article> findByIdAndAuthor(Long id, String author) {
        return articleQueryJpaRepository.findByIdAndAuthor(id, author);
    }

    @Override
    public Page<Article> findByMenuNameAndTitleContaining(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByMenuNameAndTitleContaining(pageRequest, query.menuName(), query.searchKeyword());
    }

    @Override
    public Page<Article> findByMenuNameAndTitleContainingAndVisibility(ArticleSearchQuery query,
                                                                        Visibility visibility) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByMenuNameAndTitleContainingAndVisibility(
                pageRequest, query.menuName(), query.searchKeyword(), visibility);
    }

    @Override
    public Page<Article> findByCategoryIdAndTitleContaining(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByCategoryIdAndTitleContaining(pageRequest, query.categoryId(), query.searchKeyword());
    }

    @Override
    public Page<Article> findByCategoryIdAndTitleContainingAndVisibility(ArticleSearchQuery query,
                                                                           Visibility visibility) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByCategoryIdAndTitleContainingAndVisibility(
                pageRequest, query.categoryId(), query.searchKeyword(), visibility);
    }

    @Override
    public ArticlePrevNextDto findPrevNextArticle(Long id, String menuName) {
        return articleQueryJpaRepository.findPrevNextArticle(id, menuName);
    }

    @Override
    public List<Article> findTop8ByOrderByCreatedAtDesc() {
        return articleQueryJpaRepository.findTop8ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Article> findTop8ByVisibilityOrderByCreatedAtDesc(Visibility visibility) {
        return articleQueryJpaRepository.findTop8ByVisibilityOrderByCreatedAtDesc(visibility);
    }

    @Override
    public List<Article> findByTitleContains(String titleParam) {
        return articleQueryJpaRepository.findByTitleContains(titleParam);
    }

    @Override
    public List<Article> findByTitleContainsAndVisibility(String titleParam, Visibility visibility) {
        return articleQueryJpaRepository.findByTitleContainsAndVisibility(titleParam, visibility);
    }

    @Override
    public Page<Article> findByTitleContaining(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByTitleContaining(query.searchKeyword(), pageRequest);
    }

    @Override
    public Page<Article> findByTitleContainingAndVisibility(ArticleSearchQuery query, Visibility visibility) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByTitleContainsAndVisibility(
                pageRequest, query.searchKeyword(), visibility);
    }

    @Override
    public Page<Article> findByMenu_IdAndTitleContaining(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByMenuIdAndTitleContaining(pageRequest, query.menuId(), query.searchKeyword());
    }

    @Override
    public Page<Article> findByMenu_IdAndTitleContainingAndVisibility(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByMenuIdAndTitleContainingAndVisibility(
                pageRequest, query.menuId(), query.searchKeyword(), Visibility.from(query.visibility()));
    }

    @Override
    public Page<Article> findByMenu_CategoryIdAndTitleContaining(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByCategoryIdAndTitleContaining(
                pageRequest, query.categoryId(), query.searchKeyword());
    }

    @Override
    public Page<Article> findByMenu_CategoryIdAndVisibilityAndTitleContaining(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByCategoryIdAndTitleContainingAndVisibility(
                pageRequest, query.categoryId(), query.searchKeyword(), Visibility.from(query.visibility()));
    }

    @Override
    public Page<Article> findByTitleContainsAndVisibility(ArticleSearchQuery query) {
        PageRequest pageRequest = PageRequest.of(query.pageNumber(), query.pageSize(), query.direction(),
                query.sortedBy());
        return articleQueryJpaRepository.findByTitleContainsAndVisibility(
                pageRequest, query.searchKeyword(), Visibility.from(query.visibility()));
    }

    @Override
    public List<Article> findByIdInAndAuthor(List<Long> articleIds, String author) {
        return articleQueryJpaRepository.findByIdInAndAuthor(articleIds, author);
    }

    @Override
    public List<MenuArticleCount> countByMenuId() {
        return articleQueryJpaRepository.countByMenuId().stream()
                .map(row -> {
                    Number menuId = (Number) row[0];
                    Number articleCount = (Number) row[1];
                    return new MenuArticleCount(menuId.longValue(), articleCount.longValue());
                })
                .toList();
    }
}
