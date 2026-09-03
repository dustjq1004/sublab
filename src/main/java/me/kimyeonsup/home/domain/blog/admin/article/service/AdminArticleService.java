package me.kimyeonsup.home.domain.blog.admin.article.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.ArticleSelectCondition;
import me.kimyeonsup.home.domain.blog.admin.article.domain.entity.AdminArticle;
import me.kimyeonsup.home.domain.blog.admin.article.repository.AdminArticleRepository;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.admin.menu.repository.MenuRepository;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Visibility;
import me.kimyeonsup.home.global.exception.business.RequiredValueException;
import me.kimyeonsup.home.global.exception.business.ResourceNotFoundException;
import me.kimyeonsup.home.global.exception.business.UnexpectedResultSizeException;
import me.kimyeonsup.home.global.exception.message.MessageSourceResolver;
import me.kimyeonsup.home.global.exception.message.constants.AdminExceptionMessage;
import me.kimyeonsup.home.global.exception.message.constants.MenuExceptionMessage;
import me.kimyeonsup.home.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private final AdminArticleRepository adminArticleRepository;
    private final MenuRepository menuRepository;
    private final MessageSourceResolver messageSourceResolver;

    /*
     * 필터 - 카테고리, 메뉴
     * 정렬 - 제목, 시간 순으로 정렬 가능
     * Paging 처리
     * -> PageRequest 빌더패턴 변경 예정
     * */
    public Page<AdminArticle> findArticlesBy(PageRequest pageRequest, ArticleSelectCondition params) {
        final Long categoryId = params.getCategoryId();
        final Long menuId = params.getMenuId();
        final String title = StringUtils.nvl(params.getTitle(), "");
        final String requestedVisibility = params.getVisibility();
        final Visibility visibility = StringUtils.isNull(requestedVisibility) ? null : Visibility.from(
                requestedVisibility);

        // 필터 구현 - 메뉴
        if (Objects.nonNull(menuId)) {
            if (visibility != null) {
                return adminArticleRepository.findByMenu_IdAndVisibilityAndTitleContaining(pageRequest, menuId,
                        visibility, title);
            }
            return adminArticleRepository.findByMenu_IdAndTitleContaining(pageRequest, menuId, title);
        }
        // 필터 구현 - 카테고리
        if (Objects.nonNull(categoryId)) {
            if (visibility != null) {
                return adminArticleRepository.findByMenu_CategoryIdAndVisibilityAndTitleContaining(pageRequest,
                        categoryId, visibility, title);
            }
            return adminArticleRepository.findByMenu_CategoryIdAndTitleContaining(pageRequest, categoryId, title);
        }

        if (visibility != null) {
            return adminArticleRepository.findByVisibilityAndTitleContaining(pageRequest, visibility, title);
        }

        return adminArticleRepository.findByTitleContaining(pageRequest, title);
    }

    /**
     * 게시글 일괄 삭제
     *
     * @param articleIds 삭제할 게시글 ID 목록
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @Transactional
    public int deleteArticlesByIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_DELETE));
        }

        List<AdminArticle> articles = adminArticleRepository.findAllById(articleIds);

        if (articles.size() != articleIds.size()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_DELETE));
        }

        return adminArticleRepository.deleteByIds(articleIds);
    }

    /**
     * 메뉴 일괄 변경
     *
     * @param articleIds 변경할 게시글 ID 목록
     * @param menuId 변경할 메뉴 ID
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @Transactional
    public int updateArticlesMenuByIds(List<Long> articleIds, Long menuId) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_MENU_UPDATE));
        }
        if (menuId == null) {
            throw RequiredValueException.forNull("targetMenuId");
        }

        List<AdminArticle> articles = adminArticleRepository.findAllById(articleIds);
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
     * visibility 일괄 변경
     *
     * @param articleIds 변경할 게시글 ID 목록
     * @param visibility 변경할 visibility 값
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @Transactional
    public int updateArticlesVisibilityByIds(List<Long> articleIds, String visibility) {
        if (articleIds == null || articleIds.isEmpty()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE));
        }
        if (StringUtils.isNull(visibility)) {
            throw RequiredValueException.forBlank("visibility");
        }
        Visibility.from(visibility);

        List<AdminArticle> articles = adminArticleRepository.findAllById(articleIds);
        if (articles.size() != articleIds.size()) {
            throw new UnexpectedResultSizeException(
                    messageSourceResolver.resolve(AdminExceptionMessage.ADMIN_BATCH_VISIBILITY_UPDATE));
        }

        articles.forEach(article -> article.changeVisibility(visibility));
        return articles.size();
    }
}
