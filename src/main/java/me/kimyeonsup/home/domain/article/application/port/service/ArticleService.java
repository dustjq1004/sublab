package me.kimyeonsup.home.domain.article.application.port.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.config.oauth.AdminProperties;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.article.application.port.in.ArticleCommandUseCase;
import me.kimyeonsup.home.domain.article.application.port.in.ArticleQueryUseCase;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.command.CreateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.command.UpdateArticleCommand;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleSummaryResult;
import me.kimyeonsup.home.domain.article.application.port.mapper.ArticleCommandMapper;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleCommandRepository;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Thumbnail;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import me.kimyeonsup.home.domain.draft.application.port.in.DraftCommandUseCase;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.ArticleExceptionMessage;
import me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService implements ArticleCommandUseCase, ArticleQueryUseCase {

    private final DraftCommandUseCase draftCommandUseCase;
    private final ArticleQueryRepository articleQueryRepository;
    private final ArticleCommandRepository articleCommandRepository;
    private final MenuRepository menuRepository;
    private final ArticleCommandMapper articleCommandMapper;
    private final MessageSourceResolver messageSourceResolver;
    private final AdminProperties adminProperties;

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public ArticleResult create(CreateArticleCommand command) {
        Article entity = articleCommandMapper.toEntity(command);
        Article article = articleCommandRepository.save(entity)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_SAVE_FAILED, command.title()))
                );
        ArticleResult articleResult = articleCommandMapper.toArticleResult(article);

        Long draftId = command.drateId();
        if (draftId == null) {
            return articleResult;
        }

        draftCommandUseCase.discard(draftId);
        return articleResult;
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public ArticleResult update(UpdateArticleCommand command) {
        Long id = command.id();
        Article article = articleQueryRepository.findByIdAndAuthor(id, command.author())
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));

        Long menuId = command.menuId();
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, menuId)
                ));

        article.update(command.title()
                , command.subTitle()
                , command.content()
                , Thumbnail.of(command.thumbnailUrl(), command.thumbnailLogicalFileName())
                , menu
                , command.visibility()
        );

        return articleCommandMapper.toArticleResult(article);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id, String author) {
        Article article = articleQueryRepository.findByIdAndAuthor(id, author)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));
        articleCommandRepository.deleteBy(article.getId());
    }

    @Override
    public ArticleResult findById(long id) {
        Article article = articleQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));
        return articleCommandMapper.toArticleResult(article);
    }

    @Override
    public Page<ArticleSummaryResult> findAllPagination(ArticleSearchQuery query) {
        Page<Article> articles = Optional.ofNullable(query.menuName())
                .map(name -> articleQueryRepository.findByMenuNameAndTitleContainingAndVisibility(query, Visibility.PUBLIC))
                .orElseGet(() -> Optional.ofNullable(query.categoryId())
                        .map(id -> articleQueryRepository.findByCategoryIdAndTitleContainingAndVisibility(query, Visibility.PUBLIC))
                        .orElseGet(() -> articleQueryRepository.findByTitleContainingAndVisibility(query, Visibility.PUBLIC))
                );

        return articles.map(articleCommandMapper::toArticleSummaryResult);
    }

    @Override
    public ArticleResult findByIdCredentialCheck(long id) {
        Article article = articleQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));
        if (article.isPrivate() && !authorizeArticleAuthor(article.getAuthor())) {
            throw new ReadAccessDeniedException();
        }
        return articleCommandMapper.toArticleResult(article);
    }

    @Override
    public List<ArticleSummaryResult> getLatestArticles() {
        List<Article> articles = isAdmin() ? articleQueryRepository.findTop8ByOrderByCreatedAtDesc()
                : articleQueryRepository.findTop8ByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC);

        return articles.stream()
                .map(articleCommandMapper::toArticleSummaryResult)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleSummaryResult> getArticlesByTitle(String titleParam) {
        List<Article> articles = isAdmin()
                ? articleQueryRepository.findByTitleContains(titleParam)
                : articleQueryRepository.findByTitleContainsAndVisibility(titleParam, Visibility.PUBLIC);
        return articles.stream()
                .map(articleCommandMapper::toArticleSummaryResult)
                .collect(Collectors.toList());
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof PrincipalDetail principalDetail) {
            return adminProperties.getEmail().equals(principalDetail.getEmail());
        }
        return false;
    }

    private boolean authorizeArticleAuthor(String author) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof PrincipalDetail principalDetail)) {
            return false;
        }

        return author.equals(principalDetail.getName());
    }
}
