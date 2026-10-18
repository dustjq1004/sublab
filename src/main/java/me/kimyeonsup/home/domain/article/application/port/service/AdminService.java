package me.kimyeonsup.home.domain.article.application.port.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.article.adaptor.in.web.blog.mapper.ArticleWebMapper;
import me.kimyeonsup.home.domain.article.application.port.in.AdminArticleUseCase;
import me.kimyeonsup.home.domain.article.application.port.in.command.ArticleSearchQuery;
import me.kimyeonsup.home.domain.article.application.port.in.result.ArticleResult;
import me.kimyeonsup.home.domain.article.application.port.mapper.ArticleCommandMapper;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleCommandRepository;
import me.kimyeonsup.home.domain.article.application.port.out.ArticleQueryRepository;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.domain.article.domain.model.Visibility;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.menu.repository.MenuRepository;
import me.kimyeonsup.home.global.exception.business.RequiredValueException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.business.UnexpectedResultSizeException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.AdminExceptionMessage;
import me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage;
import me.kimyeonsup.home.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService implements AdminArticleUseCase {

    private final ArticleQueryRepository articleQueryRepository;
    private final ArticleCommandRepository articleCommandRepository;
    private final MenuRepository menuRepository;
    private final ArticleWebMapper articleWebMapper;
    private final ArticleCommandMapper articleCommandMapper;
    private final MessageSourceResolver messageSourceResolver;

    /**
     * visibility 일괄 변경
     *
     * @param articleIds 변경할 게시글 ID 목록
     * @param visibility 변경할 visibility 값
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @Override
    @Transactional
    public int updateArticlesVisibilityByIds(List<Long> articleIds, String visibility, String author) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE));
        }
        if (StringUtils.isNull(visibility)) {
            throw RequiredValueException.forBlank("visibility");
        }
        Visibility.from(visibility);

        List<Article> articles = articleQueryRepository.findByIdInAndAuthor(articleIds, author);
        if (articles.size() != articleIds.size()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE));
        }

        articles.forEach(article -> article.changeVisibility(visibility));
        return articles.size();
    }

    /**
     * 메뉴 일괄 변경
     *
     * @param articleIds 변경할 게시글 ID 목록
     * @param menuId 변경할 메뉴 ID
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @Override
    @Transactional
    public int updateArticlesMenuByIds(List<Long> articleIds, Long menuId) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_MENU_UPDATE));
        }
        if (menuId == null) {
            throw RequiredValueException.forNull("targetMenuId");
        }

        List<Article> articles = articleQueryRepository.findAllById(articleIds);
        if (articles.size() != articleIds.size()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_MENU_UPDATE));
        }

        Menu targetMenu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSourceResolver.resolve(MenuExceptionMessage.MENU_NOT_FOUND, menuId)));

        articles.forEach(article -> article.changeMenu(targetMenu));
        return articles.size();
    }

    /**
     * 게시글 일괄 삭제
     *
     * @param articleIds 삭제할 게시글 ID 목록
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @Override
    @Transactional
    public int deleteArticlesByIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_DELETE));
        }

        List<Article> articles = articleQueryRepository.findAllById(articleIds);

        if (articles.size() != articleIds.size()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_DELETE));
        }

        return articleCommandRepository.deleteByIds(articleIds);
    }

    /*
     * 필터 - 카테고리, 메뉴
     * 정렬 - 제목, 시간 순으로 정렬 가능
     * Paging 처리
     * -> PageRequest 빌더패턴 변경 예정
     * */
    @Override
    public Page<ArticleResult> findArticlesBy(ArticleSearchQuery query) {
        final Long categoryId = query.categoryId();
        final Long menuId = query.menuId();
        final String requestedVisibility = query.visibility();
        final Visibility visibility = requestedVisibility == null ? null : Visibility.from(requestedVisibility);

        // 필터 구현 - 메뉴
        if (menuId != null) {
            if (visibility != null) {
                return articleQueryRepository.findByMenu_IdAndTitleContainingAndVisibility(query)
                        .map((article -> articleCommandMapper.toArticleResult(article)));
            }
            return articleQueryRepository.findByMenu_IdAndTitleContaining(query)
                    .map((article -> articleCommandMapper.toArticleResult(article)));
        }
        // 필터 구현 - 카테고리
        if (Objects.nonNull(categoryId)) {
            if (visibility != null) {
                return articleQueryRepository.findByMenu_CategoryIdAndVisibilityAndTitleContaining(query)
                        .map((article -> articleCommandMapper.toArticleResult(article)));
            }
            return articleQueryRepository.findByMenu_CategoryIdAndTitleContaining(query)
                    .map((article -> articleCommandMapper.toArticleResult(article)));
        }

        if (visibility != null) {
            return articleQueryRepository.findByTitleContainsAndVisibility(query)
                    .map((article -> articleCommandMapper.toArticleResult(article)));
        }

        return articleQueryRepository.findByTitleContaining(query)
                .map((article -> articleCommandMapper.toArticleResult(article)));
    }
}
