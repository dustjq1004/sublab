package me.kimyeonsup.home.domain.blog.article.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.config.oauth.PrincipalDetail;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.domain.dto.AddArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticlePrevNextDto;
import me.kimyeonsup.home.domain.blog.article.domain.dto.ArticlePrevNextResponse;
import me.kimyeonsup.home.domain.blog.article.domain.dto.UpdateArticleRequest;
import me.kimyeonsup.home.domain.blog.article.domain.entity.Article;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Thumbnail;
import me.kimyeonsup.home.domain.blog.article.repository.ArticleRepository;
import me.kimyeonsup.home.global.exception.business.CredentialsMismatchException;
import me.kimyeonsup.home.global.exception.business.ReadAccessDeniedException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.business.UpdateAccessDeniedException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.ArticleExceptionMessage;
import me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage;
import me.kimyeonsup.home.util.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class ArticleService {

    private static final int PAGE_SIZE = 12;
    private static final String ORDER_CRITERIA = "createdAt";

    private final ArticleRepository articleRepository;
    private final MenuRepository menuRepository;
    private final MessageSourceResolver messageSourceResolver;

    @CacheEvict(value = "categories", allEntries = true)
    public Article save(AddArticleRequest request, String userName) {
        return articleRepository.save(request.toEntity(userName));
    }

    public Page<Article> findAllPagination(int pageNumber, Long categoryId, String menuName, String searchKeyword) {
        Pageable pageRequest = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Direction.DESC, ORDER_CRITERIA));

        Page<Article> articles = Optional.ofNullable(menuName)
                .map(name -> articleRepository.findByMenuNameAndTitleContaining(pageRequest, name, searchKeyword))
                .orElseGet(() -> Optional.ofNullable(categoryId)
                        .map(id -> articleRepository.findByCategoryIdAndTitleContaining(pageRequest, id,
                                searchKeyword))
                        .orElseGet(() -> articleRepository.findByTitleContaining(searchKeyword, pageRequest))
                );

        return articles;
    }

    public Article findById(long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));
        return article;
    }

    public Article findByIdCredentialCheck(long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));
        if (article.isPrivate() && !authorizeArticleAuthor(article.getAuthor())) {
            throw new ReadAccessDeniedException();
        }
        return article;
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));

        if (!authorizeArticleAuthor(article.getAuthor())) {
            throw new CredentialsMismatchException(messageSourceResolver.resolve(
                    ArticleExceptionMessage.CREDENTIALS_MIS_MATCH, id)
            );
        }
        articleRepository.delete(article);
    }


    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public Article update(long id, UpdateArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        ArticleExceptionMessage.ARTICLE_NOT_FOUND, id)
                ));

        if (!authorizeArticleAuthor(article.getAuthor())) {
            throw new UpdateAccessDeniedException(messageSourceResolver.resolve(ArticleExceptionMessage.CREDENTIALS_MIS_MATCH, id));
        }

        Long menuId = request.getMenuId();
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSourceResolver.resolve(
                        MenuExceptionMessage.MENU_NOT_FOUND, menuId)
                ));

        Thumbnail thumbnail = Thumbnail.of("", "");
        if (!StringUtils.isNull(request.getThumbnailUrl())) {
            thumbnail = new Thumbnail(request.getThumbnailUrl(), request.getThumbnailLogicalFileName());
        }
        
        article.update(request.getTitle(), request.getSubTitle(), request.getContent(),
                thumbnail, menu, request.getVisibility());

        return article;
    }

    public ArticlePrevNextResponse findPrevNextArticle(long id, String menuName) {
        ArticlePrevNextDto prevNextArticle = articleRepository.findPrevNextArticle(id, menuName);
        return ArticlePrevNextResponse.builder()
                .articlePrevNextDto(prevNextArticle)
                .build();
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
