package me.kimyeonsup.home.domain.blog.admin.article.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.AdminArticlesGetRequest;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.AdminArticlesPaginationResponse;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.AdminArticlesResponse;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.ArticleBatchDeleteRequest;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.ArticleBatchDeleteResponse;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.ArticleBatchUpdateRequest;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.ArticleBatchUpdateResponse;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.ArticleSelectCondition;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.MenuBatchUpdateRequest;
import me.kimyeonsup.home.domain.blog.admin.article.domain.dto.MenuBatchUpdateResponse;
import me.kimyeonsup.home.domain.blog.admin.article.domain.entity.AdminArticle;
import me.kimyeonsup.home.domain.blog.admin.article.service.AdminArticleAuthorizationService;
import me.kimyeonsup.home.domain.blog.admin.article.service.AdminArticleService;
import me.kimyeonsup.home.domain.blog.admin.pagination.PageRequestBuilder;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import me.kimyeonsup.home.global.exception.business.UnexpectedResultSizeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminArticleApiController {

    private final AdminArticleAuthorizationService adminArticleAuthorizationService;
    private final AdminArticleService adminArticleService;

    @GetMapping("/api/admin/articles")
    public ApiResponse<AdminArticlesPaginationResponse<AdminArticlesResponse>> findArticles(
            @Validated @ModelAttribute AdminArticlesGetRequest request) {
        adminArticleAuthorizationService.validateAdminReadAccess();

        PageRequest articlePageRequest = PageRequestBuilder.builder()
                .pageSize(request.getPageSize())
                .pageNumber(request.getPageNumber())
                .sortBy(request.getSortBy())
                .direction(request.getDirection())
                .build()
                .createPageRequest();

        ArticleSelectCondition params = ArticleSelectCondition.builder()
                .categoryId(request.getCategoryId())
                .menuId(request.getMenuId())
                .title(request.getTitle())
                .visibility(request.getVisibility())
                .build();

        Page<AdminArticle> articles = adminArticleService.findArticlesBy(articlePageRequest, params);

        AdminArticlesPaginationResponse<AdminArticlesResponse> articlesDto = new AdminArticlesPaginationResponse<>(
                articles.getNumber(),
                articles.getSize(),
                articles.getNumberOfElements(),
                articles.isFirst(),
                articles.isLast(),
                articles.getTotalPages(),
                articles.getTotalElements(),
                articles.stream().map(AdminArticlesResponse::new).toList());

        return ApiResponse.success(articlesDto);
    }

    /**
     * 게시글 일괄 삭제
     *
     * @param params 삭제할 게시글 ID 목록
     * @return ResponseEntity<ArticleBatchDeleteResponse>
     */
    @DeleteMapping("/api/admin/articles/batch")
    public ApiResponse<ArticleBatchDeleteResponse> deleteArticlesByIds(
            @Validated @RequestBody ArticleBatchDeleteRequest params) {
        adminArticleAuthorizationService.validateAdminWriteAccess();
        int deletedCount = adminArticleService.deleteArticlesByIds(List.of(params.getArticleIds()));
        return ApiResponse.success(ArticleBatchDeleteResponse.builder()
                        .deletedCount(deletedCount)
                        .build());
    }

    /**
     * 메뉴 일괄 변경
     *
     * @param params 변경할 게시글 ID 목록과 대상 메뉴 ID
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns MenuBatchUpdateResponse
     */
    @PutMapping("/api/admin/articles/batch/menu")
    public ApiResponse<MenuBatchUpdateResponse> changeArticlesMenuByIds(
            @Validated @RequestBody MenuBatchUpdateRequest params) {
        adminArticleAuthorizationService.validateAdminWriteAccess();
        int updatedCount = adminArticleService.updateArticlesMenuByIds(params.getArticleIds(), params.getTargetMenuId());
        return ApiResponse.success(MenuBatchUpdateResponse.builder()
                .updatedCount(updatedCount)
                .build());
    }

    /**
     * visibility 일괄 변경
     *
     * @param params 변경할 게시글 ID 목록과 대상 visibility 값
     * @throws UnexpectedResultSizeException if articleIds is null or empty
     * @Returns int
     */
    @PutMapping("/api/admin/articles/batch/visibility")
    public ApiResponse<ArticleBatchUpdateResponse> changeArticlesVisibilityByIds(
            @Validated @RequestBody ArticleBatchUpdateRequest params) {
        adminArticleAuthorizationService.validateAdminWriteAccess();
        int updatedCount = adminArticleService.updateArticlesVisibilityByIds(params.getArticleIds(), params.getVisibility());
        return ApiResponse.success(ArticleBatchUpdateResponse.builder()
                .updatedCount(updatedCount)
                .build());
    }

}
